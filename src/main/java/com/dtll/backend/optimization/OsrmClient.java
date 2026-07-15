package com.dtll.backend.optimization;

import com.dtll.backend.optimization.dto.Coordenada;
import com.dtll.backend.optimization.dto.MatrizDistancias;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Cliente del servicio OSRM /table para obtener matrices reales de distancia/tiempo por calles.
 * Si {@code osrm.url} no está configurada, o el servicio no responde, cae a una estimación por
 * línea recta (Haversine) para que la optimización de rutas siga funcionando sin depender de
 * infraestructura de pago (OSRM como servicio privado requiere plan pago + disco en Render).
 */
@Component
@Slf4j
public class OsrmClient {

    private static final double VELOCIDAD_PROMEDIO_MS = 8.33; // ~30 km/h, estimación urbana

    private final RestTemplate restTemplate;
    private final String osrmUrl;

    public OsrmClient(RestTemplate restTemplate, @Value("${osrm.url:}") String osrmUrl) {
        this.restTemplate = restTemplate;
        this.osrmUrl = osrmUrl;
    }

    /**
     * @param ids         identificadores (mismo orden que coordenadas) usados como claves de la matriz resultante.
     * @param coordenadas puntos a consultar, mismo orden que {@code ids}.
     */
    public MatrizDistancias matrizDistancias(List<String> ids, List<Coordenada> coordenadas) {
        if (ids.size() != coordenadas.size()) {
            throw new IllegalArgumentException("ids y coordenadas deben tener el mismo tamaño");
        }

        if (osrmUrl == null || osrmUrl.isBlank()) {
            log.info("OSRM_URL no está configurada; usando distancias en línea recta (Haversine).");
            return matrizHaversine(ids, coordenadas);
        }

        try {
            return matrizViaOsrm(ids, coordenadas);
        } catch (Exception e) {
            log.warn("OSRM no disponible ({}), usando distancias en línea recta (Haversine) como respaldo.", e.getMessage());
            return matrizHaversine(ids, coordenadas);
        }
    }

    @SuppressWarnings("unchecked")
    private MatrizDistancias matrizViaOsrm(List<String> ids, List<Coordenada> coordenadas) {
        String coordsParam = coordenadas.stream()
                .map(c -> String.format(Locale.US, "%f,%f", c.lng(), c.lat()))
                .collect(Collectors.joining(";"));

        String url = String.format("%s/table/v1/driving/%s?annotations=distance,duration", osrmUrl, coordsParam);

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response == null || !"Ok".equals(response.get("code"))) {
            throw new IllegalStateException("OSRM devolvió una respuesta inválida: " + response);
        }

        double[][] distancias = aMatriz((List<List<Number>>) response.get("distances"));
        double[][] duraciones = aMatriz((List<List<Number>>) response.get("durations"));

        return new MatrizDistancias(ids, distancias, duraciones);
    }

    private MatrizDistancias matrizHaversine(List<String> ids, List<Coordenada> coordenadas) {
        int n = coordenadas.size();
        double[][] distancias = new double[n][n];
        double[][] duraciones = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double metros = i == j ? 0.0 : distanciaHaversineMetros(coordenadas.get(i), coordenadas.get(j));
                distancias[i][j] = metros;
                duraciones[i][j] = metros / VELOCIDAD_PROMEDIO_MS;
            }
        }

        return new MatrizDistancias(ids, distancias, duraciones);
    }

    private double distanciaHaversineMetros(Coordenada a, Coordenada b) {
        final double radioTierraM = 6_371_000;
        double dLat = Math.toRadians(b.lat() - a.lat());
        double dLng = Math.toRadians(b.lng() - a.lng());
        double sinLat = Math.sin(dLat / 2);
        double sinLng = Math.sin(dLng / 2);
        double h = sinLat * sinLat
                + Math.cos(Math.toRadians(a.lat())) * Math.cos(Math.toRadians(b.lat())) * sinLng * sinLng;
        double c = 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
        return radioTierraM * c;
    }

    private double[][] aMatriz(List<List<Number>> filas) {
        int n = filas.size();
        double[][] matriz = new double[n][n];
        for (int i = 0; i < n; i++) {
            List<Number> fila = filas.get(i);
            for (int j = 0; j < n; j++) {
                Number valor = fila.get(j);
                matriz[i][j] = valor == null ? 0.0 : valor.doubleValue();
            }
        }
        return matriz;
    }
}
