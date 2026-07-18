package com.dtll.backend.service;

import com.dtll.backend.dto.planificacion.*;
import com.dtll.backend.model.entity.*;
import com.dtll.backend.model.enums.EstadoVehiculo;
import com.dtll.backend.model.enums.EstadoViaje;
import com.dtll.backend.model.enums.TipoTrayecto;
import com.dtll.backend.model.enums.UsoTransporte;
import com.dtll.backend.repository.*;
import com.dtll.backend.util.Normalizador;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * Etapa 3: genera la propuesta de recorridos de una fecha desde la nómina
 * semanal (agrupando pasajeros por ruta vía ruta habitual → sector → comuna) y
 * gestiona la asignación de conductor/vehículo con las validaciones de la
 * sección 7/9 del requerimiento.
 */
@Service
@RequiredArgsConstructor
public class PlanificacionService {

    private static final String CARACTERES_CODIGO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ViajeRepository viajeRepository;
    private final NominaTurnoRepository nominaTurnoRepository;
    private final RutaRepository rutaRepository;
    private final TurnoRepository turnoRepository;
    private final ComunaRepository comunaRepository;
    private final AsistenciaChecklistRepository asistenciaRepository;
    private final ConductorRepository conductorRepository;
    private final VehiculoRepository vehiculoRepository;
    private final EmpresaClienteRepository empresaRepository;
    private final ConfiguracionService configuracionService;
    private final ViajeCambioRepository viajeCambioRepository;
    private final ViajeSnapshotService viajeSnapshotService;
    private final AuditoriaService auditoriaService;

    // ------------------------------------------------------------ generación

    @Transactional
    public PropuestaResponse generarPropuesta(GenerarPropuestaRequest request) {
        if (request.empresaId() == null || request.fecha() == null
                || request.anio() == null || request.semana() == null) {
            throw new IllegalArgumentException("Empresa, fecha, año y semana son obligatorios");
        }
        EmpresaCliente empresa = empresaRepository.findById(request.empresaId())
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada: " + request.empresaId()));

        List<NominaTurno> nomina = nominaTurnoRepository.buscarConPasajero(
                request.empresaId(), request.anio(), request.semana());
        if (nomina.isEmpty()) {
            throw new IllegalArgumentException("No hay nómina importada para la semana "
                    + request.semana() + " del " + request.anio() + ". Importa y confirma primero.");
        }

        List<Ruta> rutas = rutaRepository.findByEmpresaClienteId(request.empresaId()).stream()
                .filter(r -> Boolean.TRUE.equals(r.getActivo()))
                .toList();
        if (rutas.isEmpty()) {
            throw new IllegalArgumentException(
                    "La empresa no tiene rutas activas. Crea las rutas en Maestros antes de generar.");
        }
        Map<UUID, Ruta> rutaPorSector = new HashMap<>();
        Map<String, Ruta> rutaPorComuna = new HashMap<>();
        for (Ruta ruta : rutas) {
            for (Sector sector : ruta.getSectores()) {
                rutaPorSector.putIfAbsent(sector.getId(), ruta);
                for (Comuna comuna : sector.getComunas()) {
                    rutaPorComuna.putIfAbsent(Normalizador.nombre(comuna.getNombre()), ruta);
                }
            }
        }

        List<Turno> turnosMaestro = turnoRepository.findAll().stream()
                .filter(t -> Boolean.TRUE.equals(t.getActivo()))
                .toList();

        List<ViajeResumenResponse> creados = new ArrayList<>();
        List<PasajeroSinRutaResponse> sinRuta = new ArrayList<>();
        List<String> avisos = new ArrayList<>();

        // Agrupar nómina por jornada (MANANA/TARDE/NOCHE).
        Map<String, List<NominaTurno>> porJornada = new LinkedHashMap<>();
        for (NominaTurno n : nomina) {
            porJornada.computeIfAbsent(n.getTurno(), k -> new ArrayList<>()).add(n);
        }

        for (Map.Entry<String, List<NominaTurno>> entrada : porJornada.entrySet()) {
            String jornada = entrada.getKey();

            // Clasificar pasajeros de la jornada por ruta.
            Map<UUID, List<Pasajero>> porRuta = new LinkedHashMap<>();
            Map<UUID, Ruta> rutasUsadas = new HashMap<>();
            Set<UUID> yaClasificados = new HashSet<>();
            for (NominaTurno n : entrada.getValue()) {
                Pasajero p = n.getPasajero();
                if (p == null) {
                    sinRuta.add(new PasajeroSinRutaResponse(null, n.getNombreOriginal(), null,
                            jornada, "Sin pasajero vinculado en la nómina"));
                    continue;
                }
                if (!yaClasificados.add(p.getId())) {
                    continue;
                }
                if (p.getUtilizaTransporte() == UsoTransporte.NO) {
                    sinRuta.add(new PasajeroSinRutaResponse(p.getId(), p.getNombreCompleto(),
                            p.getComuna(), jornada, "No utiliza transporte (excluido)"));
                    continue;
                }
                Ruta ruta = resolverRuta(p, rutaPorSector, rutaPorComuna);
                if (ruta == null) {
                    sinRuta.add(new PasajeroSinRutaResponse(p.getId(), p.getNombreCompleto(),
                            p.getComuna(), jornada, "Sin ruta: comuna/sector no asignado a ninguna ruta"));
                    continue;
                }
                porRuta.computeIfAbsent(ruta.getId(), k -> new ArrayList<>()).add(p);
                rutasUsadas.putIfAbsent(ruta.getId(), ruta);
            }

            // Un viaje por ruta y tipo de trayecto (entrada y salida del turno).
            for (TipoTrayecto tipo : TipoTrayecto.values()) {
                for (Map.Entry<UUID, List<Pasajero>> grupo : porRuta.entrySet()) {
                    Ruta ruta = rutasUsadas.get(grupo.getKey());
                    boolean existe = viajeRepository
                            .existsByEmpresaClienteIdAndFechaOperacionAndJornadaTurnoAndTipoTrayectoAndRutaIdAndEstadoNot(
                                    request.empresaId(), request.fecha(), jornada, tipo.name(),
                                    ruta.getId(), EstadoViaje.CANCELADO);
                    if (existe) {
                        avisos.add("Ya existe el viaje " + tipo + " " + jornada + " " + ruta.getNombre()
                                + " para esa fecha (omitido)");
                        continue;
                    }

                    Turno turnoMaestro = buscarTurno(turnosMaestro, jornada, tipo);
                    Conductor conductor = ruta.getConductorHabitual() != null
                            && Boolean.TRUE.equals(ruta.getConductorHabitual().getActivo())
                            ? ruta.getConductorHabitual() : null;
                    Vehiculo vehiculo = ruta.getVehiculoHabitual() != null
                            && Boolean.TRUE.equals(ruta.getVehiculoHabitual().getActivo())
                            ? ruta.getVehiculoHabitual() : null;

                    Viaje viaje = viajeRepository.save(Viaje.builder()
                            .codigoRutaLogin(generarCodigo())
                            .empresaCliente(empresa)
                            .fechaOperacion(request.fecha())
                            .jornadaTurno(jornada)
                            .tipoTrayecto(tipo.name())
                            .ruta(ruta)
                            .turno(turnoMaestro)
                            .conductor(conductor)
                            .vehiculo(vehiculo)
                            .horaProgramadaInicio(turnoMaestro != null ? turnoMaestro.getHoraInicio() : null)
                            .horaProgramadaTermino(turnoMaestro != null ? turnoMaestro.getHoraLlegadaEstimada() : null)
                            .estado(conductor != null && vehiculo != null
                                    ? EstadoViaje.ASIGNADO : EstadoViaje.BORRADOR)
                            .build());

                    for (Pasajero p : grupo.getValue()) {
                        asistenciaRepository.save(AsistenciaChecklist.builder()
                                .viaje(viaje)
                                .pasajero(p)
                                .build());
                    }
                    creados.add(aResumen(viaje));
                }
            }
        }

        auditoriaService.registrar("GENERACION", "PLANIFICACION", null,
                "Generó " + creados.size() + " recorrido(s) para el " + request.fecha()
                        + " (semana " + request.semana() + "/" + request.anio() + ")");
        return new PropuestaResponse(creados, sinRuta, avisos);
    }

    /** Ruta habitual del pasajero → sector del pasajero → comuna (texto) contra el maestro. */
    private Ruta resolverRuta(Pasajero p, Map<UUID, Ruta> rutaPorSector, Map<String, Ruta> rutaPorComuna) {
        if (p.getRutaHabitual() != null && Boolean.TRUE.equals(p.getRutaHabitual().getActivo())) {
            return p.getRutaHabitual();
        }
        if (p.getSector() != null) {
            Ruta ruta = rutaPorSector.get(p.getSector().getId());
            if (ruta != null) {
                return ruta;
            }
        }
        if (p.getComuna() != null && !p.getComuna().isBlank()) {
            // "Lampa/Santiago" → intenta cada parte.
            for (String parte : p.getComuna().split("/")) {
                Ruta ruta = rutaPorComuna.get(Normalizador.nombre(parte));
                if (ruta != null) {
                    return ruta;
                }
            }
        }
        return null;
    }

    /** Turno maestro cuyo nombre calza con la jornada (MANANA acepta "Mañana"/"Día") y el tipo. */
    private Turno buscarTurno(List<Turno> turnos, String jornada, TipoTrayecto tipo) {
        for (Turno t : turnos) {
            if (t.getTipoServicio() != tipo) {
                continue;
            }
            String nombre = Normalizador.nombre(t.getNombre());
            boolean calza = switch (jornada) {
                case "MANANA" -> nombre.contains("MANANA") || nombre.contains("DIA");
                case "TARDE" -> nombre.contains("TARDE");
                case "NOCHE" -> nombre.contains("NOCHE");
                default -> false;
            };
            if (calza) {
                return t;
            }
        }
        return null;
    }

    // ------------------------------------------------------------ consulta

    @Transactional(readOnly = true)
    public List<ViajeResumenResponse> listarPorFecha(UUID empresaId, LocalDate fecha) {
        return viajeRepository
                .findByEmpresaClienteIdAndFechaOperacionOrderByJornadaTurnoAscTipoTrayectoAsc(empresaId, fecha)
                .stream()
                .map(this::aResumen)
                .toList();
    }

    // ------------------------------------------------------------ asignación

    @Transactional
    public ViajeResumenResponse asignar(UUID viajeId, AsignacionRequest request) {
        Viaje viaje = obtener(viajeId);
        exigirEditable(viaje);

        Conductor conductor = request.conductorId() == null ? null
                : conductorRepository.findById(request.conductorId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Conductor no encontrado: " + request.conductorId()));
        Vehiculo vehiculo = request.vehiculoId() == null ? null
                : vehiculoRepository.findById(request.vehiculoId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Vehículo no encontrado: " + request.vehiculoId()));

        long pasajeros = asistenciaRepository.countByViajeId(viajeId);

        if (vehiculo != null) {
            validarVehiculo(vehiculo, pasajeros);
            validarSinSolapamiento(viaje,
                    viajeRepository.findByVehiculoIdAndFechaOperacion(vehiculo.getId(), viaje.getFechaOperacion()),
                    "El vehículo " + vehiculo.getPatente());
        }
        if (conductor != null) {
            if (!Boolean.TRUE.equals(conductor.getActivo())) {
                throw new IllegalStateException("El conductor " + conductor.getNombreCompleto() + " está inactivo");
            }
            validarSinSolapamiento(viaje,
                    viajeRepository.findByConductorIdAndFechaOperacion(conductor.getId(), viaje.getFechaOperacion()),
                    "El conductor " + conductor.getNombreCompleto());
        }

        // Reemplazar una asignación existente exige motivo y queda en viaje_cambios (sección 8/14).
        Conductor conductorAnterior = viaje.getConductor();
        Vehiculo vehiculoAnterior = viaje.getVehiculo();
        boolean cambioConductor = conductorAnterior != null
                && (conductor == null || !conductorAnterior.getId().equals(conductor.getId()));
        boolean cambioVehiculo = vehiculoAnterior != null
                && (vehiculo == null || !vehiculoAnterior.getId().equals(vehiculo.getId()));
        if ((cambioConductor || cambioVehiculo)
                && (request.motivo() == null || request.motivo().isBlank())) {
            throw new IllegalArgumentException("Indica el motivo del cambio de "
                    + (cambioConductor && cambioVehiculo ? "conductor y vehículo"
                        : cambioConductor ? "conductor" : "vehículo"));
        }
        if (cambioConductor) {
            registrarCambio(viaje, ViajeCambio.CAMPO_CONDUCTOR,
                    conductorAnterior.getNombreCompleto(),
                    conductor != null ? conductor.getNombreCompleto() : null,
                    request.motivo());
        }
        if (cambioVehiculo) {
            registrarCambio(viaje, ViajeCambio.CAMPO_VEHICULO,
                    vehiculoAnterior.getPatente(),
                    vehiculo != null ? vehiculo.getPatente() : null,
                    request.motivo());
        }

        viaje.setConductor(conductor);
        viaje.setVehiculo(vehiculo);
        if (viaje.getEstado() == EstadoViaje.BORRADOR || viaje.getEstado() == EstadoViaje.PROGRAMADO
                || viaje.getEstado() == EstadoViaje.ASIGNADO) {
            viaje.setEstado(conductor != null && vehiculo != null
                    ? EstadoViaje.ASIGNADO
                    : EstadoViaje.BORRADOR);
        }
        return aResumen(viajeRepository.save(viaje));
    }

    @Transactional
    public ViajeResumenResponse cambiarEstado(UUID viajeId, EstadoViaje nuevo) {
        Viaje viaje = obtener(viajeId);
        if (nuevo == null) {
            throw new IllegalArgumentException("Indica el estado");
        }
        // CONFIRMADO/EN_CURSO/FINALIZADO los registra el conductor desde su vista (Etapa 4).
        Set<EstadoViaje> permitidos = Set.of(EstadoViaje.BORRADOR, EstadoViaje.PROGRAMADO,
                EstadoViaje.ASIGNADO, EstadoViaje.CANCELADO, EstadoViaje.REPROGRAMADO);
        if (!permitidos.contains(nuevo)) {
            throw new IllegalArgumentException(
                    "El estado " + nuevo + " lo registra el conductor durante el recorrido");
        }
        if (nuevo == EstadoViaje.ASIGNADO && (viaje.getConductor() == null || viaje.getVehiculo() == null)) {
            throw new IllegalStateException("Para marcar ASIGNADO el viaje necesita conductor y vehículo");
        }
        if (nuevo == EstadoViaje.CANCELADO) {
            // Un recorrido cancelado también queda congelado en el historial.
            viajeSnapshotService.congelar(viaje);
            auditoriaService.registrar("CANCELACION", "VIAJES", viaje.getId(),
                    "Canceló el recorrido " + descripcionViaje(viaje));
        }
        viaje.setEstado(nuevo);
        return aResumen(viajeRepository.save(viaje));
    }

    @Transactional
    public void eliminarBorrador(UUID viajeId) {
        Viaje viaje = obtener(viajeId);
        if (viaje.getEstado() != EstadoViaje.BORRADOR) {
            throw new IllegalStateException(
                    "Solo se pueden eliminar viajes en borrador; usa Cancelar para el resto");
        }
        // Las paradas, checklist y tracking se eliminan por cascada en la BD.
        auditoriaService.registrar("ELIMINACION", "VIAJES", viaje.getId(),
                "Eliminó el borrador " + descripcionViaje(viaje));
        viajeRepository.delete(viaje);
    }

    private void registrarCambio(Viaje viaje, String campo, String anterior, String nuevo, String motivo) {
        viajeCambioRepository.save(ViajeCambio.builder()
                .viaje(viaje)
                .campo(campo)
                .valorAnterior(anterior)
                .valorNuevo(nuevo)
                .motivo(motivo.trim())
                .usuarioId(com.dtll.backend.security.AuthenticatedUser.subjectId())
                .usuarioRol(com.dtll.backend.security.AuthenticatedUser.rol())
                .build());
        auditoriaService.registrar("CAMBIO_" + campo, "VIAJES", viaje.getId(),
                "Cambio de " + campo.toLowerCase() + " en " + descripcionViaje(viaje)
                        + " — motivo: " + motivo.trim(),
                anterior, nuevo);
    }

    private String descripcionViaje(Viaje v) {
        return v.getTipoTrayecto() + " " + v.getJornadaTurno()
                + (v.getRuta() != null ? " " + v.getRuta().getNombre() : "")
                + " del " + v.getFechaOperacion();
    }

    // ------------------------------------------------------------ validaciones

    /** Sección 9: impedir vehículos en mantención/fuera de servicio, con documentos vencidos o chicos. */
    private void validarVehiculo(Vehiculo vehiculo, long pasajeros) {
        if (!Boolean.TRUE.equals(vehiculo.getActivo())) {
            throw new IllegalStateException("El vehículo " + vehiculo.getPatente() + " está inactivo");
        }
        if (vehiculo.getEstado() != EstadoVehiculo.DISPONIBLE) {
            throw new IllegalStateException("El vehículo " + vehiculo.getPatente() + " está "
                    + (vehiculo.getEstado() == EstadoVehiculo.EN_MANTENCION ? "en mantención" : "fuera de servicio"));
        }
        if (vehiculo.documentosVencidos(LocalDate.now())) {
            throw new IllegalStateException("El vehículo " + vehiculo.getPatente()
                    + " tiene documentos vencidos (revisión técnica, permiso o seguro)");
        }
        if (vehiculo.getCapacidadPasajeros() != null && pasajeros > vehiculo.getCapacidadPasajeros()) {
            throw new IllegalStateException("El vehículo " + vehiculo.getPatente() + " tiene capacidad para "
                    + vehiculo.getCapacidadPasajeros() + " y el viaje lleva " + pasajeros + " pasajeros");
        }
    }

    /** Bloquea horarios superpuestos con otro viaje del mismo día (mismo conductor o vehículo). */
    private void validarSinSolapamiento(Viaje viaje, List<Viaje> delDia, String sujeto) {
        for (Viaje otro : delDia) {
            if (otro.getId().equals(viaje.getId()) || otro.getEstado() == EstadoViaje.CANCELADO) {
                continue;
            }
            if (seSuperponen(viaje, otro)) {
                throw new IllegalStateException(sujeto + " ya está asignado a otro recorrido que se superpone ("
                        + otro.getTipoTrayecto() + " " + otro.getJornadaTurno()
                        + (otro.getRuta() != null ? " " + otro.getRuta().getNombre() : "") + ")");
            }
        }
    }

    private boolean seSuperponen(Viaje a, Viaje b) {
        LocalTime aIni = a.getHoraProgramadaInicio();
        LocalTime aFin = a.getHoraProgramadaTermino();
        LocalTime bIni = b.getHoraProgramadaInicio();
        LocalTime bFin = b.getHoraProgramadaTermino();
        if (aIni == null || bIni == null) {
            // Sin horas: conservador — conflicto si es el mismo turno y trayecto.
            return Objects.equals(a.getJornadaTurno(), b.getJornadaTurno())
                    && Objects.equals(a.getTipoTrayecto(), b.getTipoTrayecto());
        }
        LocalTime aFinReal = aFin != null ? aFin : aIni.plusHours(2);
        LocalTime bFinReal = bFin != null ? bFin : bIni.plusHours(2);
        return aIni.isBefore(bFinReal) && bIni.isBefore(aFinReal);
    }

    /** Alerta (no bloquea) si el descanso entre recorridos es menor al configurado. */
    private void alertaTiempoInsuficiente(Viaje viaje, List<Viaje> delDia, String sujeto, List<String> alertas) {
        LocalTime ini = viaje.getHoraProgramadaInicio();
        LocalTime fin = viaje.getHoraProgramadaTermino();
        if (ini == null) {
            return;
        }
        int minimo = configuracionService.minutosMinimosEntreRecorridos();
        LocalTime finReal = fin != null ? fin : ini.plusHours(2);
        for (Viaje otro : delDia) {
            if (otro.getId().equals(viaje.getId()) || otro.getEstado() == EstadoViaje.CANCELADO
                    || otro.getHoraProgramadaInicio() == null) {
                continue;
            }
            LocalTime otroIni = otro.getHoraProgramadaInicio();
            LocalTime otroFin = otro.getHoraProgramadaTermino() != null
                    ? otro.getHoraProgramadaTermino() : otroIni.plusHours(2);
            long gap = Math.min(
                    Math.abs(Duration.between(finReal, otroIni).toMinutes()),
                    Math.abs(Duration.between(otroFin, ini).toMinutes()));
            if (!seSuperponen(viaje, otro) && gap < minimo) {
                alertas.add(sujeto + ": solo " + gap + " min entre este recorrido y "
                        + otro.getTipoTrayecto() + " " + otro.getJornadaTurno() + " (mínimo " + minimo + ")");
            }
        }
    }

    // ------------------------------------------------------------ helpers

    private Viaje obtener(UUID viajeId) {
        return viajeRepository.findById(viajeId)
                .orElseThrow(() -> new IllegalArgumentException("Viaje no encontrado: " + viajeId));
    }

    private void exigirEditable(Viaje viaje) {
        if (viaje.getEstado() == EstadoViaje.EN_CURSO || viaje.getEstado() == EstadoViaje.FINALIZADO
                || viaje.getEstado() == EstadoViaje.CANCELADO) {
            throw new IllegalStateException("El viaje está " + viaje.getEstado()
                    + " y ya no acepta cambios de asignación");
        }
    }

    private String generarCodigo() {
        String codigo;
        do {
            StringBuilder sb = new StringBuilder("R");
            for (int i = 0; i < 5; i++) {
                sb.append(CARACTERES_CODIGO.charAt(RANDOM.nextInt(CARACTERES_CODIGO.length())));
            }
            codigo = sb.toString();
        } while (viajeRepository.existsByCodigoRutaLogin(codigo));
        return codigo;
    }

    private ViajeResumenResponse aResumen(Viaje v) {
        long pasajeros = asistenciaRepository.countByViajeId(v.getId());
        List<String> alertas = new ArrayList<>();

        if (v.getConductor() == null) {
            alertas.add("Sin conductor asignado");
        }
        if (v.getVehiculo() == null) {
            alertas.add("Sin vehículo asignado");
        } else {
            Vehiculo veh = v.getVehiculo();
            if (veh.getCapacidadPasajeros() != null && pasajeros > veh.getCapacidadPasajeros()) {
                alertas.add("Capacidad insuficiente: " + pasajeros + " pasajeros en un vehículo de "
                        + veh.getCapacidadPasajeros());
            }
            if (veh.documentosVencidos(LocalDate.now())) {
                alertas.add("Vehículo con documentos vencidos");
            }
            if (veh.getEstado() != EstadoVehiculo.DISPONIBLE) {
                alertas.add("Vehículo no disponible (" + veh.getEstado() + ")");
            }
        }
        long sinTelefono = asistenciaRepository.findByViajeId(v.getId()).stream()
                .filter(a -> a.getPasajero().getTelefono() == null || a.getPasajero().getTelefono().isBlank())
                .count();
        long sinDireccion = asistenciaRepository.findByViajeId(v.getId()).stream()
                .filter(a -> a.getPasajero().getDireccionReferencia() == null
                        || a.getPasajero().getDireccionReferencia().isBlank())
                .count();
        if (sinTelefono > 0) {
            alertas.add(sinTelefono + " pasajero(s) sin teléfono");
        }
        if (sinDireccion > 0) {
            alertas.add(sinDireccion + " pasajero(s) sin dirección");
        }
        if (v.getConductor() != null) {
            alertaTiempoInsuficiente(v, viajeRepository.findByConductorIdAndFechaOperacion(
                    v.getConductor().getId(), v.getFechaOperacion()),
                    "Conductor " + v.getConductor().getNombreCompleto(), alertas);
        }
        if (v.getVehiculo() != null) {
            alertaTiempoInsuficiente(v, viajeRepository.findByVehiculoIdAndFechaOperacion(
                    v.getVehiculo().getId(), v.getFechaOperacion()),
                    "Vehículo " + v.getVehiculo().getPatente(), alertas);
        }

        return new ViajeResumenResponse(
                v.getId(),
                v.getCodigoRutaLogin(),
                v.getFechaOperacion(),
                v.getJornadaTurno(),
                v.getTipoTrayecto(),
                v.getEstado(),
                v.getRuta() != null ? v.getRuta().getId() : null,
                v.getRuta() != null ? v.getRuta().getNombre() : null,
                v.getConductor() != null ? v.getConductor().getId() : null,
                v.getConductor() != null ? v.getConductor().getNombreCompleto() : null,
                v.getVehiculo() != null ? v.getVehiculo().getId() : null,
                v.getVehiculo() != null ? v.getVehiculo().getPatente() : null,
                v.getVehiculo() != null ? v.getVehiculo().getCapacidadPasajeros() : null,
                pasajeros,
                v.getHoraProgramadaInicio(),
                v.getHoraProgramadaTermino(),
                alertas);
    }
}
