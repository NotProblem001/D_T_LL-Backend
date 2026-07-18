package com.dtll.backend.service;

import com.dtll.backend.dto.importacion.*;
import com.dtll.backend.model.entity.*;
import com.dtll.backend.model.enums.*;
import com.dtll.backend.repository.*;
import com.dtll.backend.util.LimpiadorCampos;
import com.dtll.backend.util.NombreMatcher;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Flujo de importación con revisión (sección 22 del requerimiento): los archivos
 * se parsean a staging, el sistema cruza contra la BDD de pasajeros con matching
 * difuso, el operador resuelve las sugerencias en pantalla y recién al confirmar
 * se escriben pasajeros y nómina. Nada se guarda directo.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImportacionRevisionService {

    private static final int MAX_CANDIDATOS = 3;

    private final NominaImportService nominaImportService;
    private final ImportacionRepository importacionRepository;
    private final ImportacionRegistroRepository registroRepository;
    private final EmpresaClienteRepository empresaRepository;
    private final PasajeroRepository pasajeroRepository;
    private final NominaTurnoRepository nominaTurnoRepository;
    private final ObjectMapper objectMapper;
    private final AuditoriaService auditoriaService;

    // ------------------------------------------------------------------ preview

    @Transactional
    public ImportacionDetalleResponse previewArchivo(UUID empresaId, TipoImportacion tipo,
                                                     MultipartFile file, Integer anio, Integer semana,
                                                     UUID usuarioId) {
        List<RegistroParseado> registros;
        Integer semanaDetectada = null;

        if (tipo == TipoImportacion.NOMINA) {
            ResultadoParseo resultado = nominaImportService.parsearNominaSemanal(file);
            registros = resultado.registros();
            semanaDetectada = resultado.semanaDetectada();
        } else if (tipo == TipoImportacion.PLANILLA) {
            registros = nominaImportService.parsearPlanilla(file);
            semanaDetectada = nominaImportService.detectarSemana(file.getOriginalFilename());
        } else {
            throw new IllegalArgumentException("Para texto pegado use el endpoint de texto");
        }

        int anioFinal = anio != null ? anio : nominaImportService.detectarAnio(file.getOriginalFilename());
        int semanaFinal = semana != null ? semana
                : (semanaDetectada != null ? semanaDetectada : LocalDate.now().getDayOfYear() / 7 + 1);

        return crearStaging(empresaId, tipo, file.getOriginalFilename(), anioFinal, semanaFinal,
                registros, usuarioId);
    }

    @Transactional
    public ImportacionDetalleResponse previewTexto(PreviewTextoRequest request, UUID usuarioId) {
        List<RegistroParseado> registros = nominaImportService.parsearTexto(request.texto());
        int anioFinal = request.anio() != null ? request.anio() : LocalDate.now().getYear();
        int semanaFinal = request.semana() != null ? request.semana()
                : LocalDate.now().getDayOfYear() / 7 + 1;
        return crearStaging(request.empresaId(), TipoImportacion.TEXTO, "texto pegado",
                anioFinal, semanaFinal, registros, usuarioId);
    }

    private ImportacionDetalleResponse crearStaging(UUID empresaId, TipoImportacion tipo,
                                                    String nombreArchivo, int anio, int semana,
                                                    List<RegistroParseado> parseados, UUID usuarioId) {
        EmpresaCliente empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada: " + empresaId));

        List<Pasajero> pasajeros = pasajeroRepository.findByEmpresaClienteIdOrderByNombreCompletoAsc(empresaId);
        Map<String, Pasajero> porNorm = new HashMap<>();
        for (Pasajero p : pasajeros) {
            if (p.getNombreNormalizado() != null) {
                porNorm.putIfAbsent(p.getNombreNormalizado(), p);
            }
        }

        Importacion importacion = importacionRepository.save(Importacion.builder()
                .empresaCliente(empresa)
                .tipo(tipo)
                .nombreArchivo(nombreArchivo)
                .anio(anio)
                .semana(semana)
                .usuarioId(usuarioId)
                .build());

        Set<String> vistos = new HashSet<>();
        List<ImportacionRegistro> guardados = new ArrayList<>();

        for (RegistroParseado r : parseados) {
            ImportacionRegistro registro = clasificar(importacion, r, porNorm, pasajeros, vistos);
            guardados.add(registroRepository.save(registro));
        }

        actualizarTotales(importacion, guardados);
        return detalleDesde(importacion, guardados);
    }

    /** Limpieza de campos + matching en cascada contra la BDD de pasajeros. */
    private ImportacionRegistro clasificar(Importacion importacion, RegistroParseado r,
                                           Map<String, Pasajero> porNorm, List<Pasajero> pasajeros,
                                           Set<String> vistos) {
        ImportacionRegistro.ImportacionRegistroBuilder b = ImportacionRegistro.builder()
                .importacion(importacion)
                .hojaOrigen(r.hoja())
                .filaOrigen(r.fila())
                .nombreOriginal(r.nombreOriginal())
                .nombreNormalizado(r.nombreNormalizado())
                .turno(r.turno())
                .centroCosto(r.centroCosto())
                .cargo(r.cargo())
                .telefono(LimpiadorCampos.telefono(r.telefono()))
                .direccion(LimpiadorCampos.direccion(r.direccion()))
                .comuna(LimpiadorCampos.comuna(r.comuna()))
                .usoTransporteDetectado(
                        LimpiadorCampos.usoDetectado(r.telefono(), r.direccion(), r.comuna()));

        // Valores que no parecen nombre ("12 HORAS") vienen marcados desde el parser.
        if (r.error() != null) {
            return b.tipoMatch(TipoMatch.ERROR)
                    .resolucion(ResolucionRegistro.DESCARTADO)
                    .mensajeError(r.error())
                    .build();
        }

        // Repetido dentro del mismo archivo (mismo turno + nombre).
        if (!vistos.add(r.turno() + "|" + r.nombreNormalizado())) {
            return b.tipoMatch(TipoMatch.DUPLICADO)
                    .resolucion(ResolucionRegistro.DESCARTADO)
                    .mensajeError("Repetido dentro del archivo para el mismo turno")
                    .build();
        }

        // (a) Nombre normalizado idéntico.
        Pasajero exacto = porNorm.get(r.nombreNormalizado());
        if (exacto != null) {
            return b.tipoMatch(TipoMatch.EXACTO)
                    .resolucion(ResolucionRegistro.ACEPTADO)
                    .pasajero(exacto)
                    .build();
        }

        // (b) Mismas palabras en otro orden (RUIZ ARCE LUIS = LUIS RUIZ ARCE).
        for (Pasajero p : pasajeros) {
            if (p.getNombreNormalizado() != null
                    && NombreMatcher.mismosTokens(r.nombreNormalizado(), p.getNombreNormalizado())) {
                return b.tipoMatch(TipoMatch.TOKENS)
                        .resolucion(ResolucionRegistro.ACEPTADO)
                        .pasajero(p)
                        .build();
            }
        }

        // (c) Parecidos (typos, abreviaciones, nombre parcial) → sugerencias.
        List<CandidatoMatch> candidatos = pasajeros.stream()
                .filter(p -> p.getNombreNormalizado() != null)
                .map(p -> new CandidatoMatch(p.getId(), p.getNombreCompleto(),
                        redondear(NombreMatcher.similitud(r.nombreNormalizado(), p.getNombreNormalizado()))))
                .filter(c -> c.score() >= NombreMatcher.UMBRAL_SUGERENCIA)
                .sorted(Comparator.comparingDouble(CandidatoMatch::score).reversed())
                .limit(MAX_CANDIDATOS)
                .toList();
        if (!candidatos.isEmpty()) {
            return b.tipoMatch(TipoMatch.SUGERENCIA)
                    .resolucion(ResolucionRegistro.PENDIENTE)
                    .candidatosJson(escribirJson(candidatos))
                    .build();
        }

        // (d) Sin candidato: propuesta de pasajero nuevo.
        return b.tipoMatch(TipoMatch.NUEVO)
                .resolucion(ResolucionRegistro.NUEVO)
                .build();
    }

    // ------------------------------------------------------------------ consulta

    @Transactional(readOnly = true)
    public List<ImportacionResponse> listar(UUID empresaId) {
        return importacionRepository.findByEmpresaClienteIdOrderByCreatedAtDesc(empresaId).stream()
                .map(ImportacionResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public ImportacionDetalleResponse detalle(UUID importacionId) {
        Importacion importacion = obtener(importacionId);
        return detalleDesde(importacion,
                registroRepository.findByImportacionIdOrderByHojaOrigenAscFilaOrigenAsc(importacionId));
    }

    // ------------------------------------------------------------------ resolución

    @Transactional
    public RegistroImportacionResponse resolverRegistro(UUID importacionId, UUID registroId,
                                                        ResolverRegistroRequest request) {
        Importacion importacion = obtener(importacionId);
        exigirBorrador(importacion);

        ImportacionRegistro registro = registroRepository.findById(registroId)
                .filter(x -> x.getImportacion().getId().equals(importacionId))
                .orElseThrow(() -> new IllegalArgumentException("Registro no encontrado: " + registroId));

        if (request.resolucion() == null || request.resolucion() == ResolucionRegistro.PENDIENTE) {
            throw new IllegalArgumentException("Indique una resolución: ACEPTADO, NUEVO o DESCARTADO");
        }
        if (registro.getTipoMatch() == TipoMatch.ERROR && request.resolucion() != ResolucionRegistro.DESCARTADO) {
            throw new IllegalArgumentException("Un registro con error solo puede quedar descartado");
        }

        if (request.resolucion() == ResolucionRegistro.ACEPTADO) {
            if (request.pasajeroId() == null) {
                throw new IllegalArgumentException("Para aceptar debe indicar el pasajero");
            }
            Pasajero pasajero = pasajeroRepository.findById(request.pasajeroId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Pasajero no encontrado: " + request.pasajeroId()));
            if (!pasajero.getEmpresaCliente().getId().equals(importacion.getEmpresaCliente().getId())) {
                throw new IllegalArgumentException("El pasajero pertenece a otra empresa");
            }
            registro.setPasajero(pasajero);
        } else {
            registro.setPasajero(null);
        }
        registro.setResolucion(request.resolucion());
        registroRepository.save(registro);

        actualizarTotales(importacion,
                registroRepository.findByImportacionIdOrderByHojaOrigenAscFilaOrigenAsc(importacionId));
        return aResponse(registro);
    }

    // ------------------------------------------------------------------ confirmación

    @Transactional
    public ImportacionResponse confirmar(UUID importacionId) {
        Importacion importacion = obtener(importacionId);
        exigirBorrador(importacion);

        List<ImportacionRegistro> registros =
                registroRepository.findByImportacionIdOrderByHojaOrigenAscFilaOrigenAsc(importacionId);
        long pendientes = registros.stream()
                .filter(r -> r.getResolucion() == ResolucionRegistro.PENDIENTE)
                .count();
        if (pendientes > 0) {
            throw new IllegalStateException(
                    "Hay " + pendientes + " sugerencias sin resolver. Resuélvelas antes de confirmar.");
        }

        EmpresaCliente empresa = importacion.getEmpresaCliente();
        // Reimportar la misma semana reemplaza la nómina anterior (idempotente).
        nominaTurnoRepository.deleteByEmpresaClienteIdAndAnioAndSemana(
                empresa.getId(), importacion.getAnio(), importacion.getSemana());

        Set<String> vistos = new HashSet<>();
        for (ImportacionRegistro r : registros) {
            if (r.getResolucion() == ResolucionRegistro.DESCARTADO) {
                continue;
            }
            if (!vistos.add(r.getTurno() + "|" + r.getNombreNormalizado())) {
                continue;
            }

            Pasajero pasajero = r.getResolucion() == ResolucionRegistro.ACEPTADO
                    ? r.getPasajero()
                    : crearPasajero(empresa, r);

            // Completa datos faltantes con lo que trajo el archivo y aplica el
            // flag de uso detectado ("No utiliza el servicio").
            nominaImportService.completarDatos(pasajero, r.getTelefono(), r.getDireccion(), r.getComuna());
            if (r.getUsoTransporteDetectado() != null
                    && (pasajero.getUtilizaTransporte() == null
                        || pasajero.getUtilizaTransporte() == UsoTransporte.PENDIENTE)) {
                pasajero.setUtilizaTransporte(r.getUsoTransporteDetectado());
            }
            pasajero = pasajeroRepository.save(pasajero);
            r.setPasajero(pasajero);
            registroRepository.save(r);

            nominaTurnoRepository.save(NominaTurno.builder()
                    .empresaCliente(empresa)
                    .anio(importacion.getAnio())
                    .semana(importacion.getSemana())
                    .turno(r.getTurno())
                    .pasajero(pasajero)
                    .nombreOriginal(r.getNombreOriginal())
                    .nombreNormalizado(r.getNombreNormalizado())
                    .centroCosto(r.getCentroCosto())
                    .cargo(r.getCargo())
                    .build());
        }

        importacion.setEstado(EstadoImportacion.CONFIRMADA);
        importacion.setConfirmadaAt(LocalDateTime.now());
        auditoriaService.registrar("CONFIRMACION", "IMPORTACIONES", importacion.getId(),
                "Confirmó la importación " + importacion.getTipo() + " de "
                        + importacion.getNombreArchivo() + " (semana " + importacion.getSemana()
                        + "/" + importacion.getAnio() + ", " + importacion.getTotalRegistros()
                        + " registros)");
        return ImportacionResponse.desde(importacionRepository.save(importacion));
    }

    @Transactional
    public ImportacionResponse descartar(UUID importacionId) {
        Importacion importacion = obtener(importacionId);
        exigirBorrador(importacion);
        importacion.setEstado(EstadoImportacion.DESCARTADA);
        return ImportacionResponse.desde(importacionRepository.save(importacion));
    }

    // ------------------------------------------------------------------ helpers

    private Pasajero crearPasajero(EmpresaCliente empresa, ImportacionRegistro r) {
        return Pasajero.builder()
                .empresaCliente(empresa)
                .identificadorInterno(nominaImportService.generarIdentificador(r.getNombreNormalizado()))
                .nombreCompleto(r.getNombreOriginal())
                .nombreNormalizado(r.getNombreNormalizado())
                .build();
    }

    private Importacion obtener(UUID id) {
        return importacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Importación no encontrada: " + id));
    }

    private void exigirBorrador(Importacion importacion) {
        if (importacion.getEstado() != EstadoImportacion.BORRADOR) {
            throw new IllegalStateException(
                    "La importación ya está " + importacion.getEstado().name().toLowerCase());
        }
    }

    private void actualizarTotales(Importacion importacion, List<ImportacionRegistro> registros) {
        importacion.setTotalRegistros(registros.size());
        importacion.setTotalEncontrados((int) registros.stream()
                .filter(r -> r.getTipoMatch() == TipoMatch.EXACTO || r.getTipoMatch() == TipoMatch.TOKENS)
                .count());
        importacion.setTotalSugerencias((int) registros.stream()
                .filter(r -> r.getTipoMatch() == TipoMatch.SUGERENCIA)
                .count());
        importacion.setTotalNuevos((int) registros.stream()
                .filter(r -> r.getTipoMatch() == TipoMatch.NUEVO)
                .count());
        importacion.setTotalDuplicados((int) registros.stream()
                .filter(r -> r.getTipoMatch() == TipoMatch.DUPLICADO)
                .count());
        importacion.setTotalErrores((int) registros.stream()
                .filter(r -> r.getTipoMatch() == TipoMatch.ERROR)
                .count());
        importacionRepository.save(importacion);
    }

    private ImportacionDetalleResponse detalleDesde(Importacion importacion,
                                                    List<ImportacionRegistro> registros) {
        return new ImportacionDetalleResponse(
                ImportacionResponse.desde(importacion),
                registros.stream().map(this::aResponse).toList());
    }

    private RegistroImportacionResponse aResponse(ImportacionRegistro r) {
        Pasajero p = r.getPasajero();
        return new RegistroImportacionResponse(
                r.getId(),
                r.getHojaOrigen(),
                r.getFilaOrigen(),
                r.getNombreOriginal(),
                r.getTurno(),
                r.getCentroCosto(),
                r.getCargo(),
                r.getTelefono(),
                r.getDireccion(),
                r.getComuna(),
                r.getUsoTransporteDetectado(),
                r.getTipoMatch(),
                r.getResolucion(),
                p != null ? p.getId() : null,
                p != null ? p.getNombreCompleto() : null,
                leerJson(r.getCandidatosJson()),
                r.getMensajeError());
    }

    private String escribirJson(List<CandidatoMatch> candidatos) {
        try {
            return objectMapper.writeValueAsString(candidatos);
        } catch (Exception e) {
            log.warn("No se pudieron serializar candidatos", e);
            return null;
        }
    }

    private List<CandidatoMatch> leerJson(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<CandidatoMatch>>() {
            });
        } catch (Exception e) {
            log.warn("No se pudieron leer candidatos", e);
            return List.of();
        }
    }

    private double redondear(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
