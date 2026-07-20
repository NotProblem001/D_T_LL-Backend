package com.dtll.backend.service;

import com.dtll.backend.dto.informes.DashboardResponse;
import com.dtll.backend.dto.informes.ResumenInternoResponse;
import com.dtll.backend.model.entity.AsistenciaChecklist;
import com.dtll.backend.model.entity.EstadoAsistenciaConfig;
import com.dtll.backend.model.entity.Viaje;
import com.dtll.backend.model.enums.EstadoIncidencia;
import com.dtll.backend.model.enums.EstadoVehiculo;
import com.dtll.backend.model.enums.EstadoViaje;
import com.dtll.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Etapa 7: informe semanal para el cliente (Excel, formato acordado en la
 * respuesta 5), informes internos agregados (sección 17) y dashboard (sección 18).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InformeService {

    private final ViajeRepository viajeRepository;
    private final AsistenciaChecklistRepository asistenciaRepository;
    private final EstadoAsistenciaConfigRepository estadoRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final ConductorRepository conductorRepository;
    private final VehiculoRepository vehiculoRepository;

    // ------------------------------------------------------------ informe semanal cliente

    @Transactional(readOnly = true)
    public byte[] informeSemanalExcel(UUID empresaId, LocalDate desde, LocalDate hasta) {
        List<Viaje> viajes = viajesDelRango(empresaId, desde, hasta);
        if (viajes.isEmpty()) {
            throw new IllegalArgumentException(
                    "No hay recorridos entre " + desde + " y " + hasta + " para esa empresa");
        }
        Map<String, String> nombresEstado = catalogoEstados();

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CellStyle bold = wb.createCellStyle();
            Font f = wb.createFont();
            f.setBold(true);
            bold.setFont(f);

            // ---------------- Hoja Detalle: una fila por pasajero × recorrido
            Sheet det = wb.createSheet("Detalle");
            String[] cols = {"Fecha", "Pasajero", "Turno", "Servicio", "Ruta",
                    "Asistencia", "Conductor", "Vehículo", "Observaciones"};
            Row h = det.createRow(0);
            for (int i = 0; i < cols.length; i++) {
                Cell c = h.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(bold);
            }

            int fila = 1;
            for (Viaje v : viajes) {
                for (AsistenciaChecklist a : asistenciaRepository.findByViajeId(v.getId())) {
                    Row r = det.createRow(fila++);
                    r.createCell(0).setCellValue(v.getFechaOperacion().toString());
                    r.createCell(1).setCellValue(nombrePasajero(a));
                    r.createCell(2).setCellValue(jornadaLegible(v.getJornadaTurno()));
                    r.createCell(3).setCellValue(tipoLegible(v.getTipoTrayecto()));
                    r.createCell(4).setCellValue(nombreRuta(v));
                    r.createCell(5).setCellValue(nombresEstado.getOrDefault(a.getEstado(),
                            "PENDIENTE".equals(a.getEstado()) ? "Sin marcar" : a.getEstado()));
                    r.createCell(6).setCellValue(nombreConductor(v));
                    r.createCell(7).setCellValue(patenteVehiculo(v));
                    r.createCell(8).setCellValue(a.getObservaciones() != null ? a.getObservaciones() : "");
                }
            }
            int[] anchos = {12, 32, 10, 10, 22, 26, 28, 12, 35};
            for (int i = 0; i < anchos.length; i++) {
                det.setColumnWidth(i, anchos[i] * 256);
            }

            // ---------------- Hoja Resumen: totales generales + por día/turno/ruta
            Sheet res = wb.createSheet("Resumen");
            int rr = 0;
            rr = titulo(res, rr, bold, "Informe semanal " + desde + " al " + hasta);

            Totales total = new Totales();
            Map<String, Totales> porDia = new TreeMap<>();
            Map<String, Totales> porTurno = new LinkedHashMap<>();
            Map<String, Totales> porRuta = new TreeMap<>();
            long recorridosRealizados = 0;
            for (Viaje v : viajes) {
                if (v.getEstado() == EstadoViaje.FINALIZADO) {
                    recorridosRealizados++;
                }
                for (AsistenciaChecklist a : asistenciaRepository.findByViajeId(v.getId())) {
                    total.sumar(a.getEstado());
                    porDia.computeIfAbsent(v.getFechaOperacion().toString(), k -> new Totales())
                            .sumar(a.getEstado());
                    porTurno.computeIfAbsent(jornadaLegible(v.getJornadaTurno()), k -> new Totales())
                            .sumar(a.getEstado());
                    porRuta.computeIfAbsent(nombreRuta(v), k -> new Totales())
                            .sumar(a.getEstado());
                }
            }

            rr = filaDato(res, rr, "Total pasajeros programados", total.programados);
            rr = filaDato(res, rr, "Total pasajeros transportados", total.transportados);
            rr = filaDato(res, rr, "Total ausencias", total.ausentes);
            rr = filaDato(res, rr, "Total cancelaciones (avisadas)", total.cancelaciones);
            rr = filaDato(res, rr, "Recorridos realizados", recorridosRealizados);
            rr = filaDato(res, rr, "Recorridos en el período", viajes.size());
            rr++;

            rr = tablaTotales(res, rr, bold, "Totales por día", "Día", porDia);
            rr = tablaTotales(res, rr, bold, "Totales por turno", "Turno", porTurno);
            tablaTotales(res, rr, bold, "Totales por ruta", "Ruta", porRuta);
            res.setColumnWidth(0, 32 * 256);
            for (int i = 1; i <= 3; i++) {
                res.setColumnWidth(i, 15 * 256);
            }

            wb.write(out);
            return out.toByteArray();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error generando informe semanal", e);
            throw new IllegalArgumentException("No se pudo generar el informe: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------ informes internos

    @Transactional(readOnly = true)
    public ResumenInternoResponse resumenInterno(UUID empresaId, LocalDate desde, LocalDate hasta) {
        List<Viaje> viajes = viajesDelRango(empresaId, desde, hasta);

        Map<String, long[]> conductores = new TreeMap<>();  // recorridos, finalizados, transportados, incidencias
        Map<String, long[]> vehiculos = new TreeMap<>();    // recorridos, transportados
        Map<String, long[]> pasajeros = new TreeMap<>();    // asistencias, ausencias, cancelaciones

        for (Viaje v : viajes) {
            long transportadosViaje = 0;
            for (AsistenciaChecklist a : asistenciaRepository.findByViajeId(v.getId())) {
                String nombre = nombrePasajero(a);
                long[] p = pasajeros.computeIfAbsent(nombre, k -> new long[3]);
                switch (a.getEstado() == null ? "" : a.getEstado()) {
                    case "ASISTIO" -> { p[0]++; transportadosViaje++; }
                    case "NO_ASISTIO", "NO_FUE_ENCONTRADO" -> p[1]++;
                    case "AVISO_PREVIO" -> p[2]++;
                    default -> { }
                }
            }
            String conductor = nombreConductor(v);
            if (!conductor.isBlank()) {
                long[] c = conductores.computeIfAbsent(conductor, k -> new long[4]);
                c[0]++;
                if (v.getEstado() == EstadoViaje.FINALIZADO) c[1]++;
                c[2] += transportadosViaje;
                c[3] += incidenciaRepository.findByViajeIdOrderByCreatedAtDesc(v.getId()).size();
            }
            String patente = patenteVehiculo(v);
            if (!patente.isBlank()) {
                long[] ve = vehiculos.computeIfAbsent(patente, k -> new long[2]);
                ve[0]++;
                ve[1] += transportadosViaje;
            }
        }

        return new ResumenInternoResponse(
                conductores.entrySet().stream()
                        .map(e -> new ResumenInternoResponse.FilaConductor(e.getKey(),
                                e.getValue()[0], e.getValue()[1], e.getValue()[2], e.getValue()[3]))
                        .toList(),
                vehiculos.entrySet().stream()
                        .map(e -> new ResumenInternoResponse.FilaVehiculo(e.getKey(),
                                e.getValue()[0], e.getValue()[1]))
                        .toList(),
                pasajeros.entrySet().stream()
                        .map(e -> new ResumenInternoResponse.FilaPasajero(e.getKey(),
                                e.getValue()[0], e.getValue()[1], e.getValue()[2]))
                        .toList());
    }

    // ------------------------------------------------------------ dashboard

    @Transactional(readOnly = true)
    public DashboardResponse dashboard(UUID empresaId, LocalDate fecha) {
        LocalDate dia = fecha != null ? fecha : LocalDate.now();
        LocalDate lunes = dia.with(DayOfWeek.MONDAY);
        LocalDate domingo = lunes.plusDays(6);

        DashboardResponse.Indicadores hoy = indicadoresDia(viajesDelRango(empresaId, dia, dia));

        List<Viaje> semana = viajesDelRango(empresaId, lunes, domingo);
        Totales totalSemana = new Totales();
        long finalizadosSemana = 0;
        for (Viaje v : semana) {
            if (v.getEstado() == EstadoViaje.FINALIZADO) finalizadosSemana++;
            for (AsistenciaChecklist a : asistenciaRepository.findByViajeId(v.getId())) {
                totalSemana.sumar(a.getEstado());
            }
        }
        long marcados = totalSemana.transportados + totalSemana.ausentes;
        double porcentaje = marcados == 0 ? 0
                : Math.round(totalSemana.transportados * 1000.0 / marcados) / 10.0;

        return new DashboardResponse(
                hoy,
                new DashboardResponse.SemanaResumen(semana.size(), finalizadosSemana,
                        totalSemana.programados, totalSemana.transportados,
                        totalSemana.ausentes, porcentaje),
                conductorRepository.countByActivoTrue(),
                vehiculoRepository.countByActivoTrueAndEstado(EstadoVehiculo.DISPONIBLE),
                incidenciaRepository.countByEstado(EstadoIncidencia.ABIERTA));
    }

    private DashboardResponse.Indicadores indicadoresDia(List<Viaje> viajes) {
        long programados = 0;
        long enCurso = 0;
        long finalizados = 0;
        long cancelados = 0;
        Totales pasajeros = new Totales();
        for (Viaje v : viajes) {
            switch (v.getEstado()) {
                case EN_CURSO -> enCurso++;
                case FINALIZADO -> finalizados++;
                case CANCELADO -> cancelados++;
                default -> programados++;
            }
            if (v.getEstado() != EstadoViaje.CANCELADO) {
                for (AsistenciaChecklist a : asistenciaRepository.findByViajeId(v.getId())) {
                    pasajeros.sumar(a.getEstado());
                }
            }
        }
        return new DashboardResponse.Indicadores(pasajeros.programados, pasajeros.transportados,
                pasajeros.ausentes, programados, enCurso, finalizados, cancelados);
    }

    // ------------------------------------------------------------ helpers

    /** Todos los recorridos del rango excepto borradores (aún no son operación real). */
    private List<Viaje> viajesDelRango(UUID empresaId, LocalDate desde, LocalDate hasta) {
        if (empresaId == null || desde == null || hasta == null) {
            throw new IllegalArgumentException("Empresa, desde y hasta son obligatorios");
        }
        return viajeRepository
                .findByEmpresaClienteIdAndFechaOperacionBetweenOrderByFechaOperacionAscJornadaTurnoAsc(
                        empresaId, desde, hasta).stream()
                .filter(v -> v.getEstado() != EstadoViaje.BORRADOR)
                .toList();
    }

    private Map<String, String> catalogoEstados() {
        return estadoRepository.findAll().stream()
                .collect(Collectors.toMap(EstadoAsistenciaConfig::getCodigo,
                        EstadoAsistenciaConfig::getNombre, (a, b) -> a));
    }

    private static class Totales {
        long programados;
        long transportados;
        long ausentes;
        long cancelaciones;

        void sumar(String estado) {
            programados++;
            switch (estado == null ? "" : estado) {
                case "ASISTIO" -> transportados++;
                case "NO_ASISTIO", "NO_FUE_ENCONTRADO" -> ausentes++;
                case "AVISO_PREVIO" -> cancelaciones++;
                default -> { }
            }
        }
    }

    private int titulo(Sheet sheet, int fila, CellStyle bold, String texto) {
        Row r = sheet.createRow(fila);
        Cell c = r.createCell(0);
        c.setCellValue(texto);
        c.setCellStyle(bold);
        return fila + 2;
    }

    private int filaDato(Sheet sheet, int fila, String etiqueta, long valor) {
        Row r = sheet.createRow(fila);
        r.createCell(0).setCellValue(etiqueta);
        r.createCell(1).setCellValue(valor);
        return fila + 1;
    }

    private int tablaTotales(Sheet sheet, int fila, CellStyle bold, String titulo,
                             String etiquetaClave, Map<String, Totales> datos) {
        Row t = sheet.createRow(fila++);
        Cell ct = t.createCell(0);
        ct.setCellValue(titulo);
        ct.setCellStyle(bold);

        Row h = sheet.createRow(fila++);
        String[] cols = {etiquetaClave, "Programados", "Transportados", "Ausentes"};
        for (int i = 0; i < cols.length; i++) {
            Cell c = h.createCell(i);
            c.setCellValue(cols[i]);
            c.setCellStyle(bold);
        }
        for (Map.Entry<String, Totales> e : datos.entrySet()) {
            Row r = sheet.createRow(fila++);
            r.createCell(0).setCellValue(e.getKey());
            r.createCell(1).setCellValue(e.getValue().programados);
            r.createCell(2).setCellValue(e.getValue().transportados);
            r.createCell(3).setCellValue(e.getValue().ausentes);
        }
        return fila + 1;
    }

    private String nombrePasajero(AsistenciaChecklist a) {
        return a.getPasajeroNombreSnapshot() != null
                ? a.getPasajeroNombreSnapshot()
                : a.getPasajero().getNombreCompleto();
    }

    private String nombreConductor(Viaje v) {
        if (v.getConductorNombreSnapshot() != null) return v.getConductorNombreSnapshot();
        return v.getConductor() != null ? v.getConductor().getNombreCompleto() : "";
    }

    private String patenteVehiculo(Viaje v) {
        if (v.getVehiculoPatenteSnapshot() != null) return v.getVehiculoPatenteSnapshot();
        return v.getVehiculo() != null ? v.getVehiculo().getPatente() : "";
    }

    private String nombreRuta(Viaje v) {
        if (v.getRutaNombreSnapshot() != null) return v.getRutaNombreSnapshot();
        return v.getRuta() != null ? v.getRuta().getNombre() : "Sin ruta";
    }

    private String jornadaLegible(String jornada) {
        return switch (jornada == null ? "" : jornada) {
            case "MANANA" -> "Mañana";
            case "TARDE" -> "Tarde";
            case "NOCHE" -> "Noche";
            default -> jornada;
        };
    }

    private String tipoLegible(String tipo) {
        return switch (tipo == null ? "" : tipo) {
            case "ENTRADA" -> "Entrada";
            case "SALIDA" -> "Salida";
            default -> tipo;
        };
    }
}
