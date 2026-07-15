package com.dtll.backend.optimization;

import com.dtll.backend.model.entity.AsistenciaChecklist;
import com.dtll.backend.model.entity.Pasajero;
import com.dtll.backend.model.entity.Viaje;
import com.dtll.backend.model.entity.ViajeParada;
import com.dtll.backend.optimization.dto.Coordenada;
import com.dtll.backend.optimization.dto.MatrizDistancias;
import com.dtll.backend.optimization.dto.OptimizarRutaRequest;
import com.dtll.backend.optimization.dto.ParadaOptimizadaResponse;
import com.dtll.backend.repository.AsistenciaChecklistRepository;
import com.dtll.backend.repository.ViajeParadaRepository;
import com.dtll.backend.repository.ViajeRepository;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Optimiza el orden de las paradas de un viaje con Jsprit (motor VRP), usando distancias/tiempos
 * reales por calle obtenidos de OSRM en vez de línea recta.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RouteOptimizationService {

    private static final String ID_INICIO = "INICIO";
    private static final String ID_DESTINO = "DESTINO";
    private static final int MAX_ITERACIONES = 200;

    private final AsistenciaChecklistRepository asistenciaChecklistRepository;
    private final ViajeRepository viajeRepository;
    private final ViajeParadaRepository viajeParadaRepository;
    private final OsrmClient osrmClient;

    @Transactional
    public List<ParadaOptimizadaResponse> optimizar(UUID viajeId, OptimizarRutaRequest request) {
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new IllegalArgumentException("Viaje no encontrado"));

        List<Pasajero> pasajeros = asistenciaChecklistRepository.findByViajeId(viajeId).stream()
                .map(AsistenciaChecklist::getPasajero)
                .filter(p -> p.getLatitud() != null && p.getLongitud() != null)
                .toList();

        if (pasajeros.isEmpty()) {
            throw new IllegalStateException("El viaje no tiene pasajeros con coordenadas registradas");
        }

        boolean tieneDestino = request.destinoFinal() != null;

        List<String> ids = new ArrayList<>();
        List<Coordenada> coordenadas = new ArrayList<>();
        ids.add(ID_INICIO);
        coordenadas.add(request.puntoInicio());
        for (Pasajero p : pasajeros) {
            ids.add(p.getId().toString());
            coordenadas.add(new Coordenada(p.getLatitud(), p.getLongitud()));
        }
        if (tieneDestino) {
            ids.add(ID_DESTINO);
            coordenadas.add(request.destinoFinal());
        }

        MatrizDistancias matriz = osrmClient.matrizDistancias(ids, coordenadas);

        VehicleRoutingProblem vrp = construirProblema(pasajeros, matriz, tieneDestino);

        VehicleRoutingAlgorithm algoritmo = Jsprit.Builder.newInstance(vrp).buildAlgorithm();
        algoritmo.setMaxIterations(MAX_ITERACIONES);
        Collection<VehicleRoutingProblemSolution> soluciones = algoritmo.searchSolutions();
        VehicleRoutingProblemSolution mejorSolucion = Solutions.bestOf(soluciones);

        return persistirResultado(viaje, mejorSolucion, matriz, pasajeros);
    }

    public List<ParadaOptimizadaResponse> obtenerParadas(UUID viajeId) {
        return viajeParadaRepository.findByViajeIdOrderByOrdenParadaAsc(viajeId).stream()
                .map(vp -> new ParadaOptimizadaResponse(
                        vp.getPasajero().getId(),
                        vp.getPasajero().getNombreCompleto(),
                        vp.getPasajero().getPuntoParadaAsignado(),
                        vp.getOrdenParada(),
                        vp.getDistanciaAcumuladaM() == null ? 0 : vp.getDistanciaAcumuladaM(),
                        vp.getTiempoEstimadoSeg() == null ? 0 : vp.getTiempoEstimadoSeg(),
                        vp.getLatitudSnapshot() == null ? 0 : vp.getLatitudSnapshot(),
                        vp.getLongitudSnapshot() == null ? 0 : vp.getLongitudSnapshot()))
                .toList();
    }

    private VehicleRoutingProblem construirProblema(List<Pasajero> pasajeros, MatrizDistancias matriz, boolean tieneDestino) {
        VehicleTypeImpl tipoVehiculo = VehicleTypeImpl.Builder.newInstance("tipo-van")
                .addCapacityDimension(0, pasajeros.size())
                .build();

        VehicleImpl.Builder vehiculoBuilder = VehicleImpl.Builder.newInstance("conductor")
                .setStartLocation(Location.newInstance(ID_INICIO))
                .setType(tipoVehiculo);

        if (tieneDestino) {
            vehiculoBuilder.setEndLocation(Location.newInstance(ID_DESTINO)).setReturnToDepot(true);
        } else {
            vehiculoBuilder.setReturnToDepot(false);
        }

        VehicleImpl vehiculo = vehiculoBuilder.build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance()
                .addVehicle(vehiculo)
                .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);

        for (Pasajero p : pasajeros) {
            com.graphhopper.jsprit.core.problem.job.Service servicio =
                    com.graphhopper.jsprit.core.problem.job.Service.Builder.newInstance(p.getId().toString())
                            .setLocation(Location.newInstance(p.getId().toString()))
                            .addSizeDimension(0, 1)
                            .build();
            vrpBuilder.addJob(servicio);
        }

        VehicleRoutingTransportCostsMatrix.Builder matrizBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
        List<String> ids = matriz.ids();
        for (int i = 0; i < ids.size(); i++) {
            for (int j = 0; j < ids.size(); j++) {
                matrizBuilder.addTransportDistance(ids.get(i), ids.get(j), matriz.distanciasM()[i][j]);
                matrizBuilder.addTransportTime(ids.get(i), ids.get(j), matriz.duracionesSeg()[i][j]);
            }
        }
        vrpBuilder.setRoutingCost(matrizBuilder.build());

        return vrpBuilder.build();
    }

    private List<ParadaOptimizadaResponse> persistirResultado(Viaje viaje,
                                                                VehicleRoutingProblemSolution solucion,
                                                                MatrizDistancias matriz,
                                                                List<Pasajero> pasajeros) {
        Map<String, Pasajero> pasajeroPorId = new HashMap<>();
        for (Pasajero p : pasajeros) {
            pasajeroPorId.put(p.getId().toString(), p);
        }

        Map<String, Integer> indicePorId = new HashMap<>();
        for (int i = 0; i < matriz.ids().size(); i++) {
            indicePorId.put(matriz.ids().get(i), i);
        }

        viajeParadaRepository.deleteByViajeId(viaje.getId());

        List<ParadaOptimizadaResponse> resultado = new ArrayList<>();
        List<ViajeParada> paradasAPersistir = new ArrayList<>();

        if (solucion.getRoutes().isEmpty()) {
            throw new IllegalStateException("Jsprit no encontró una ruta factible para este viaje");
        }
        VehicleRoute ruta = solucion.getRoutes().iterator().next();

        double distanciaAcumulada = 0;
        double tiempoAcumulado = 0;
        int orden = 1;
        String idAnterior = ID_INICIO;

        for (TourActivity actividad : ruta.getActivities()) {
            if (!(actividad instanceof TourActivity.JobActivity jobActivity)) {
                continue;
            }
            String pasajeroId = jobActivity.getJob().getId();
            Pasajero pasajero = pasajeroPorId.get(pasajeroId);
            if (pasajero == null) {
                continue;
            }

            int iAnterior = indicePorId.get(idAnterior);
            int iActual = indicePorId.get(pasajeroId);
            distanciaAcumulada += matriz.distanciasM()[iAnterior][iActual];
            tiempoAcumulado += matriz.duracionesSeg()[iAnterior][iActual];

            ViajeParada parada = ViajeParada.builder()
                    .viaje(viaje)
                    .pasajero(pasajero)
                    .ordenParada(orden)
                    .distanciaAcumuladaM(distanciaAcumulada)
                    .tiempoEstimadoSeg((int) tiempoAcumulado)
                    .latitudSnapshot(pasajero.getLatitud())
                    .longitudSnapshot(pasajero.getLongitud())
                    .build();
            paradasAPersistir.add(parada);

            resultado.add(new ParadaOptimizadaResponse(
                    pasajero.getId(),
                    pasajero.getNombreCompleto(),
                    pasajero.getPuntoParadaAsignado(),
                    orden,
                    distanciaAcumulada,
                    (int) tiempoAcumulado,
                    pasajero.getLatitud(),
                    pasajero.getLongitud()));

            idAnterior = pasajeroId;
            orden++;
        }

        viajeParadaRepository.saveAll(paradasAPersistir);
        log.info("Ruta optimizada para viaje {}: {} paradas, {} m totales", viaje.getId(), resultado.size(), distanciaAcumulada);

        return resultado;
    }
}
