package com.dtll.backend.service;

import com.dtll.backend.dto.importacion.NominaImportResponse;
import com.dtll.backend.model.entity.EmpresaCliente;
import com.dtll.backend.model.entity.NominaTurno;
import com.dtll.backend.model.entity.Pasajero;
import com.dtll.backend.repository.EmpresaClienteRepository;
import com.dtll.backend.repository.NominaTurnoRepository;
import com.dtll.backend.repository.PasajeroRepository;
import com.dtll.backend.util.Normalizador;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Importa la nómina semanal de turnos que envía la empresa cliente
 * (ej: "SEM 29.xlsx" de Nutrición Balanceada), la cruza contra la BDD de
 * pasajeros y genera la Planilla de Horarios semanal por turno.
 *
 * También importa la hoja "BDD" de la planilla interna histórica
 * (Nombre | Teléfono | Dirección | Comuna) para poblar/actualizar pasajeros
 * sin crear duplicados (dedupe por nombre normalizado dentro de la empresa).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NominaImportService {

    public static final String TURNO_MANANA = "MANANA";
    public static final String TURNO_TARDE = "TARDE";
    public static final String TURNO_NOCHE = "NOCHE";

    private static final Pattern SEMANA_PATTERN = Pattern.compile("SEMANA\\s+(\\d{1,2})");
    private static final Set<String> NO_NOMBRES = Set.of(
            "12 HORAS", "HORAS", "VACACIONES", "LICENCIA MEDICA", "CENTRO DE COSTO", "SIN DATO", "0");
    private static final Set<String> SIN_SERVICIO = Set.of("NO UTILIZA EL SERVICIO", "NO UTILIZA", "NO LO UTILIZA",
            "NO UTILIZA TRANSPORTE", "SIN DATO");

    private final EmpresaClienteRepository empresaRepository;
    private final PasajeroRepository pasajeroRepository;
    private final NominaTurnoRepository nominaTurnoRepository;

    // ---------------------------------------------------------------------
    // 1) Importación de la BDD interna de pasajeros (hoja Nombre|Telefono|Dirección|Comuna)
    // ---------------------------------------------------------------------

    @Transactional
    public NominaImportResponse importarBddPasajeros(UUID empresaId, MultipartFile file) {
        EmpresaCliente empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada: " + empresaId));

        int creados = 0;
        int actualizados = 0;
        int ignorados = 0;
        List<String> observaciones = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = elegirHojaBdd(wb);
            for (Row row : sheet) {
                String nombre = celda(row, 0);
                String telefono = celda(row, 1);
                String direccion = celda(row, 2);
                String comuna = celda(row, 3);

                String norm = Normalizador.nombre(nombre);
                if (norm.isBlank() || NO_NOMBRES.contains(norm) || norm.equals("NOMBRE")) {
                    continue; // encabezado o fila vacía
                }

                Optional<Pasajero> existente =
                        pasajeroRepository.findFirstByEmpresaClienteIdAndNombreNormalizado(empresaId, norm);

                if (existente.isPresent()) {
                    boolean cambio = completarDatos(existente.get(), telefono, direccion, comuna);
                    if (cambio) {
                        pasajeroRepository.save(existente.get());
                        actualizados++;
                    } else {
                        ignorados++;
                    }
                } else {
                    Pasajero nuevo = Pasajero.builder()
                            .empresaCliente(empresa)
                            .identificadorInterno(generarIdentificador(norm))
                            .nombreCompleto(nombre.trim())
                            .nombreNormalizado(norm)
                            .build();
                    completarDatos(nuevo, telefono, direccion, comuna);
                    pasajeroRepository.save(nuevo);
                    creados++;
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error importando BDD de pasajeros", e);
            throw new IllegalArgumentException("No se pudo leer el archivo: " + e.getMessage());
        }

        return NominaImportResponse.deBdd(creados, actualizados, ignorados, observaciones);
    }

    private Sheet elegirHojaBdd(Workbook wb) {
        for (Sheet s : wb) {
            if (Normalizador.nombre(s.getSheetName()).contains("BDD")) {
                return s;
            }
        }
        return wb.getSheetAt(0);
    }

    /** Completa teléfono/dirección/comuna solo con datos útiles. Devuelve true si cambió algo. */
    private boolean completarDatos(Pasajero p, String telefono, String direccion, String comuna) {
        boolean cambio = false;
        if (p.getNombreNormalizado() == null || p.getNombreNormalizado().isBlank()) {
            p.setNombreNormalizado(Normalizador.nombre(p.getNombreCompleto()));
            cambio = true;
        }
        String telLimpio = limpiarDato(telefono);
        if (telLimpio != null && vacio(p.getTelefono())) {
            p.setTelefono(telLimpio);
            cambio = true;
        }
        String dirLimpia = limpiarDato(direccion);
        if (dirLimpia != null && vacio(p.getDireccionReferencia())) {
            p.setDireccionReferencia(dirLimpia);
            cambio = true;
        }
        String comLimpia = limpiarDato(comuna);
        if (comLimpia != null && vacio(p.getComuna())) {
            p.setComuna(comLimpia);
            cambio = true;
        }
        return cambio;
    }

    private boolean vacio(String v) {
        return v == null || v.isBlank();
    }

    /** null si el dato es basura ("0", "SIN DATO", "No utiliza el servicio", vacío). */
    private String limpiarDato(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        if (limpio.isBlank()) {
            return null;
        }
        String norm = Normalizador.nombre(limpio);
        if (norm.isBlank() || norm.equals("0") || SIN_SERVICIO.stream().anyMatch(norm::contains)) {
            return null;
        }
        return limpio;
    }

    private String generarIdentificador(String nombreNormalizado) {
        String slug = nombreNormalizado.replaceAll("[^A-Z0-9]", "");
        String base = slug.substring(0, Math.min(38, slug.length()));
        return "AUTO-" + base + "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
    }

    // ---------------------------------------------------------------------
    // 2) Importación de la nómina semanal (formato "SEM XX" de la empresa cliente)
    // ---------------------------------------------------------------------

    @Transactional
    public NominaImportResponse importarNominaSemanal(UUID empresaId, Integer anio, Integer semana,
                                                      MultipartFile file) {
        EmpresaCliente empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada: " + empresaId));

        List<PersonaTurno> personas = new ArrayList<>();
        Integer semanaDetectada = null;

        try (InputStream is = file.getInputStream(); Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            int colManana = -1;
            int colTarde = -1;
            int colNoche = -1;
            boolean enTurnos = false;
            String centroCostoActual = null;

            for (Row row : sheet) {
                List<String> celdas = leerCeldas(row, 8);
                if (celdas.stream().allMatch(String::isBlank)) {
                    continue;
                }
                String fila = Normalizador.nombre(String.join(" ", celdas));

                if (semanaDetectada == null) {
                    Matcher m = SEMANA_PATTERN.matcher(fila);
                    if (m.find()) {
                        semanaDetectada = Integer.parseInt(m.group(1));
                    }
                }

                // Fin de la nómina útil: bloque de vacaciones/licencias.
                if (fila.contains("LICENCIA MEDICA") || fila.startsWith("VACACIONES")) {
                    break;
                }

                // Encabezado de turnos: fila con MAÑANA/TARDE/NOCHE.
                if (!enTurnos || (fila.contains("MANANA") && fila.contains("TARDE") && fila.contains("NOCHE"))) {
                    if (fila.contains("MANANA") && fila.contains("TARDE") && fila.contains("NOCHE")) {
                        for (int i = 0; i < celdas.size(); i++) {
                            String c = Normalizador.nombre(celdas.get(i));
                            if (c.equals("MANANA")) colManana = i;
                            if (c.equals("TARDE")) colTarde = i;
                            if (c.equals("NOCHE")) colNoche = i;
                        }
                        enTurnos = true;
                    }
                    continue;
                }

                // Filas descriptivas de horarios (LUNES A VIERNES..., SABADO...).
                if (fila.contains("LUNES") || fila.contains("SABADO")) {
                    continue;
                }

                String colA = celdas.get(0);
                String colB = celdas.size() > 1 ? celdas.get(1) : "";
                if (Normalizador.nombre(colA).equals("CENTRO DE COSTO")) {
                    centroCostoActual = colB.isBlank() ? null : colB.trim();
                    continue;
                }
                String centroCosto = !colA.isBlank() ? colA.trim() : centroCostoActual;
                String cargo = colB.isBlank() ? null : colB.trim();

                agregarSiEsNombre(personas, celdas, colManana, TURNO_MANANA, centroCosto, cargo);
                agregarSiEsNombre(personas, celdas, colTarde, TURNO_TARDE, centroCosto, cargo);
                agregarSiEsNombre(personas, celdas, colNoche, TURNO_NOCHE, centroCosto, cargo);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error importando nómina semanal", e);
            throw new IllegalArgumentException("No se pudo leer el archivo: " + e.getMessage());
        }

        if (personas.isEmpty()) {
            throw new IllegalArgumentException(
                    "No se encontraron turnos en el archivo. ¿Es el formato semanal con columnas MAÑANA/TARDE/NOCHE?");
        }

        int anioFinal = anio != null ? anio : LocalDate.now().getYear();
        int semanaFinal = semana != null ? semana
                : (semanaDetectada != null ? semanaDetectada
                : LocalDate.now().getDayOfYear() / 7 + 1);

        return persistirNomina(empresa, anioFinal, semanaFinal, personas);
    }

    /**
     * Importa la nómina semanal desde texto pegado (formato correo):
     * secciones ("Mantención", "Calidad"), encabezados "Turno día/tarde/noche"
     * y líneas inline "Nombre Apellido turno noche.".
     */
    @Transactional
    public NominaImportResponse importarNominaTexto(UUID empresaId, Integer anio, Integer semana, String texto) {
        EmpresaCliente empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada: " + empresaId));
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El texto de la nómina está vacío");
        }

        List<PersonaTurno> personas = new ArrayList<>();
        String turnoActual = null;
        String seccionActual = null;

        for (String lineaRaw : texto.split("\\r?\\n")) {
            String linea = lineaRaw.trim().replaceAll("[.;,]+$", "");
            String norm = Normalizador.nombre(linea);
            if (norm.isBlank() || norm.startsWith("ESTIMAD") || norm.startsWith("ENVIO TURNOS")
                    || norm.startsWith("SALUDOS") || norm.startsWith("HOLA") || norm.startsWith("BUEN")) {
                continue;
            }

            String turnoEncabezado = turnoDesdeTexto(norm);
            if (turnoEncabezado != null && norm.startsWith("TURNO")) {
                turnoActual = turnoEncabezado;
                continue;
            }

            // Inline: "Alberto Bracho turno noche"
            Matcher inline = Pattern.compile("^(.*?)\\s+TURNO\\s+(DIA|MANANA|TARDE|NOCHE)$").matcher(norm);
            if (inline.matches()) {
                String nombre = inline.group(1).trim();
                String turno = turnoDesdeTexto("TURNO " + inline.group(2));
                if (nombre.contains(" ")) {
                    personas.add(new PersonaTurno(capitalizar(nombre), Normalizador.nombre(nombre),
                            turno, seccionActual, null));
                }
                continue;
            }

            // Línea de una sola palabra => probable sección (Mantención, Calidad).
            if (!norm.contains(" ")) {
                seccionActual = linea;
                continue;
            }

            // Nombre simple bajo un encabezado de turno.
            if (turnoActual != null && !norm.matches(".*\\d.*") && norm.length() >= 5) {
                personas.add(new PersonaTurno(linea, norm, turnoActual, seccionActual, null));
            }
        }

        if (personas.isEmpty()) {
            throw new IllegalArgumentException(
                    "No se reconocieron personas con turno en el texto pegado");
        }

        int anioFinal = anio != null ? anio : LocalDate.now().getYear();
        int semanaFinal = semana != null ? semana : LocalDate.now().getDayOfYear() / 7 + 1;
        return persistirNomina(empresa, anioFinal, semanaFinal, personas);
    }

    private String turnoDesdeTexto(String norm) {
        if (norm.contains("NOCHE")) return TURNO_NOCHE;
        if (norm.contains("TARDE")) return TURNO_TARDE;
        if (norm.contains("MANANA") || norm.contains("DIA")) return TURNO_MANANA;
        return null;
    }

    private String capitalizar(String nombre) {
        StringBuilder sb = new StringBuilder();
        for (String palabra : nombre.toLowerCase(Locale.ROOT).split(" ")) {
            if (!palabra.isBlank()) {
                sb.append(Character.toUpperCase(palabra.charAt(0))).append(palabra.substring(1)).append(' ');
            }
        }
        return sb.toString().trim();
    }

    private NominaImportResponse persistirNomina(EmpresaCliente empresa, int anioFinal, int semanaFinal,
                                                 List<PersonaTurno> personas) {
        UUID empresaId = empresa.getId();
        // Reimportar la misma semana reemplaza la nómina anterior (idempotente).
        nominaTurnoRepository.deleteByEmpresaClienteIdAndAnioAndSemana(empresaId, anioFinal, semanaFinal);

        int conDatos = 0;
        int sinDatos = 0;
        List<String> sinMatch = new ArrayList<>();
        Set<String> vistos = new HashSet<>();
        Map<String, Integer> porTurno = new LinkedHashMap<>();

        for (PersonaTurno pt : personas) {
            String claveUnica = pt.turno + "|" + pt.nombreNormalizado;
            if (!vistos.add(claveUnica)) {
                continue; // duplicado dentro del archivo
            }

            Pasajero pasajero = pasajeroRepository
                    .findFirstByEmpresaClienteIdAndNombreNormalizado(empresaId, pt.nombreNormalizado)
                    .orElseGet(() -> pasajeroRepository.save(Pasajero.builder()
                            .empresaCliente(empresa)
                            .identificadorInterno(generarIdentificador(pt.nombreNormalizado))
                            .nombreCompleto(pt.nombreOriginal)
                            .nombreNormalizado(pt.nombreNormalizado)
                            .build()));

            boolean tieneDatos = !vacio(pasajero.getDireccionReferencia()) || !vacio(pasajero.getTelefono());
            if (tieneDatos) {
                conDatos++;
            } else {
                sinDatos++;
                sinMatch.add(pt.nombreOriginal + " (" + pt.turno + ")");
            }
            porTurno.merge(pt.turno, 1, Integer::sum);

            nominaTurnoRepository.save(NominaTurno.builder()
                    .empresaCliente(empresa)
                    .anio(anioFinal)
                    .semana(semanaFinal)
                    .turno(pt.turno)
                    .pasajero(pasajero)
                    .nombreOriginal(pt.nombreOriginal)
                    .nombreNormalizado(pt.nombreNormalizado)
                    .centroCosto(pt.centroCosto)
                    .cargo(pt.cargo)
                    .build());
        }

        return NominaImportResponse.deNomina(anioFinal, semanaFinal, porTurno, conDatos, sinDatos, sinMatch);
    }

    private void agregarSiEsNombre(List<PersonaTurno> personas, List<String> celdas, int col,
                                   String turno, String centroCosto, String cargo) {
        if (col < 0 || col >= celdas.size()) {
            return;
        }
        String valor = celdas.get(col);
        String norm = Normalizador.nombre(valor);
        if (norm.isBlank() || norm.length() < 5 || !norm.contains(" ")
                || NO_NOMBRES.contains(norm) || norm.matches(".*\\d.*")) {
            return; // vacío, "12 HORAS", horarios, etc.
        }
        personas.add(new PersonaTurno(valor.trim(), norm, turno, centroCosto, cargo));
    }

    private record PersonaTurno(String nombreOriginal, String nombreNormalizado, String turno,
                                String centroCosto, String cargo) {
    }

    // ---------------------------------------------------------------------
    // 3) Generación de la Planilla de Horarios semanal (xlsx por turnos)
    // ---------------------------------------------------------------------

    @Transactional(readOnly = true)
    public byte[] generarPlanillaHorarios(UUID empresaId, int anio, int semana) {
        List<NominaTurno> nomina = nominaTurnoRepository
                .findByEmpresaClienteIdAndAnioAndSemanaOrderByTurnoAscNombreNormalizadoAsc(empresaId, anio, semana);
        if (nomina.isEmpty()) {
            throw new IllegalArgumentException(
                    "No hay nómina importada para la semana " + semana + " del " + anio);
        }

        Map<String, String> titulos = Map.of(
                TURNO_MANANA, "Mañana (Día) 08-16",
                TURNO_TARDE, "Tarde 16-00",
                TURNO_NOCHE, "Noche 00-08");

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CellStyle header = wb.createCellStyle();
            Font bold = wb.createFont();
            bold.setBold(true);
            header.setFont(bold);

            for (String turno : List.of(TURNO_MANANA, TURNO_TARDE, TURNO_NOCHE)) {
                List<NominaTurno> delTurno = nomina.stream()
                        .filter(n -> turno.equals(n.getTurno()))
                        .sorted(Comparator.comparing(
                                        (NominaTurno n) -> valorOrden(n.getPasajero()))
                                .thenComparing(NominaTurno::getNombreNormalizado))
                        .toList();
                Sheet sheet = wb.createSheet(titulos.get(turno));
                Row h = sheet.createRow(0);
                String[] cols = {"Nombre", "Telefono", "Dirección", "Comuna", "Centro de Costo", "Cargo", "Estado"};
                for (int i = 0; i < cols.length; i++) {
                    Cell c = h.createCell(i);
                    c.setCellValue(cols[i]);
                    c.setCellStyle(header);
                }

                int r = 1;
                for (NominaTurno n : delTurno) {
                    Pasajero p = n.getPasajero();
                    Row row = sheet.createRow(r++);
                    row.createCell(0).setCellValue(p != null ? p.getNombreCompleto() : n.getNombreOriginal());
                    row.createCell(1).setCellValue(p != null && !vacio(p.getTelefono()) ? p.getTelefono() : "SIN DATO");
                    row.createCell(2).setCellValue(p != null && !vacio(p.getDireccionReferencia())
                            ? p.getDireccionReferencia() : "SIN DATO");
                    row.createCell(3).setCellValue(p != null && !vacio(p.getComuna()) ? p.getComuna() : "SIN DATO");
                    row.createCell(4).setCellValue(n.getCentroCosto() != null ? n.getCentroCosto() : "");
                    row.createCell(5).setCellValue(n.getCargo() != null ? n.getCargo() : "");
                    row.createCell(6).setCellValue(p == null || vacio(p.getDireccionReferencia())
                            ? "COMPLETAR DATOS" : "OK");
                }
                for (int i = 0; i < cols.length; i++) {
                    sheet.setColumnWidth(i, Math.min(280, 14 + 8) * 256 / 8);
                }
                sheet.setColumnWidth(0, 30 * 256);
                sheet.setColumnWidth(2, 45 * 256);
                sheet.setColumnWidth(3, 18 * 256);
                sheet.setColumnWidth(4, 22 * 256);
                sheet.setColumnWidth(5, 25 * 256);
            }

            wb.write(out);
            return out.toByteArray();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error generando planilla de horarios", e);
            throw new IllegalArgumentException("No se pudo generar la planilla: " + e.getMessage());
        }
    }

    /** Orden por comuna para agrupar recogidas cercanas en la planilla. */
    private String valorOrden(Pasajero p) {
        if (p == null || vacio(p.getComuna())) {
            return "ZZZ-SIN-COMUNA";
        }
        return Normalizador.nombre(p.getComuna());
    }

    // ---------------------------------------------------------------------

    private List<String> leerCeldas(Row row, int max) {
        List<String> valores = new ArrayList<>();
        for (int i = 0; i < max; i++) {
            valores.add(celda(row, i));
        }
        return valores;
    }

    private String celda(Row row, int idx) {
        if (row == null) {
            return "";
        }
        Cell cell = row.getCell(idx);
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue() != null ? cell.getStringCellValue().trim() : "";
            case NUMERIC -> {
                double v = cell.getNumericCellValue();
                yield v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(v);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue() != null ? cell.getStringCellValue().trim() : "";
                } catch (Exception e) {
                    yield "";
                }
            }
            default -> "";
        };
    }
}
