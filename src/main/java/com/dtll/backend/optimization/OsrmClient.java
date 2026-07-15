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

/** Cliente del servicio OSRM /table para obtener matrices reales de distancia/tiempo por calles. */
@Component
@Slf4j
public class OsrmClient {

    private final RestTemplate restTemplate;
    private final String osrmUrl;

    public OsrmClient(RestTemplate restTemplate, @Value("${osrm.url}") String osrmUrl) {
        this.restTemplate = restTemplate;
        this.osrmUrl = osrmUrl;
    }

    /**
     * @param ids         identificadores (mismo orden que coordenadas) usados como claves de la matriz resultante.
     * @param coordenadas puntos a consultar, mismo orden que {@code ids}.
     */
    @SuppressWarnings("unchecked")
    public MatrizDistancias matrizDistancias(List<String> ids, List<Coordenada> coordenadas) {
        if (ids.size() != coordenadas.size()) {
            throw new IllegalArgumentException("ids y coordenadas deben tener el mismo tamaño");
        }

        String coordsParam = coordenadas.stream()
                .map(c -> String.format(Locale.US, "%f,%f", c.lng(), c.lat()))
                .collect(Collectors.joining(";"));

        String url = String.format("%s/table/v1/driving/%s?annotations=distance,duration", osrmUrl, coordsParam);

        Map<String, Object> response;
        try {
            response = restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            log.error("Error al consultar OSRM en {}: {}", osrmUrl, e.getMessage());
            throw new IllegalStateException("No se pudo calcular la matriz de distancias con OSRM", e);
        }

        if (response == null || !"Ok".equals(response.get("code"))) {
            throw new IllegalStateException("OSRM devolvió una respuesta inválida: " + response);
        }

        double[][] distancias = aMatriz((List<List<Number>>) response.get("distances"));
        double[][] duraciones = aMatriz((List<List<Number>>) response.get("durations"));

        return new MatrizDistancias(ids, distancias, duraciones);
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
