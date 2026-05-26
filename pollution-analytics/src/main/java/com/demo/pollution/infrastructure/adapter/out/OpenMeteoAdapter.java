package com.demo.pollution.infrastructure.adapter.out;

import com.demo.pollution.application.port.out.AirQualityClientPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Adaptador para Open-Meteo Air Quality API.
 * Gratuito, sin API key, datos en tiempo real (modelo CAMS Copernicus, actualización horaria).
 * Cobertura: principales ciudades de Colombia.
 *
 * Documentación: https://open-meteo.com/en/docs/air-quality-api
 */
public class OpenMeteoAdapter implements AirQualityClientPort {

    private static final Logger log = LoggerFactory.getLogger(OpenMeteoAdapter.class);
    private static final String BASE_URL =
            "https://air-quality-api.open-meteo.com/v1/air-quality";

    // { nombre, latitud, longitud }
    private static final String[][] STATIONS = {
        // Región Andina
        { "Bogotá",           "4.7110",  "-74.0721" },
        { "Medellín",         "6.2442",  "-75.5812" },
        { "Cali",             "3.4516",  "-76.5320" },
        { "Bucaramanga",      "7.1193",  "-73.1227" },
        { "Manizales",        "5.0689",  "-75.4864" },
        { "Armenia",          "4.5339",  "-75.6811" },
        { "Pereira",          "4.8133",  "-75.6950" },
        { "Ibagué",           "4.4378",  "-75.2324" },
        { "Pasto",            "1.2136",  "-77.2811" },
        { "Popayán",          "2.4448",  "-76.6147" },
        { "Tunja",            "5.5353",  "-73.3678" },
        { "Neiva",            "2.9273",  "-75.2819" },
        { "Villavicencio",    "4.1420",  "-73.6266" },
        { "Girardot",         "4.3122",  "-74.4132" },
        // Región Caribe
        { "Barranquilla",     "10.9685", "-74.7813" },
        { "Cartagena",        "10.3910", "-75.4794" },
        { "Santa Marta",      "11.2408", "-74.1990" },
        { "Montería",         "8.7479",  "-75.8814" },
        { "Valledupar",       "10.4631", "-73.2532" },
        { "Sincelejo",        "9.3047",  "-75.3978" },
        // Región Pacífica
        { "Quibdó",           "5.6919",  "-76.6583" },
        { "Buenaventura",     "3.8653",  "-77.0181" },
        { "Tumaco",           "1.8539",  "-78.7682" },
        // Región Nororiental
        { "Cúcuta",           "7.8939",  "-72.5078" },
        { "Barrancabermeja",  "7.0653",  "-73.8547" },
        // Región Cafetera / Antioquia
        { "Rionegro",         "6.1543",  "-75.3741" },
        { "Apartadó",         "7.8939",  "-76.6272" },
        // Región Llanos / Sur
        { "Yopal",            "5.3378",  "-72.3959" },
        { "Florencia",        "1.6144",  "-75.6062" },
        { "Leticia",          "-4.2153", "-69.9406" },
    };

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public OpenMeteoAdapter(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<StationReading> fetchColombiaReadings() {
        List<StationReading> readings = new ArrayList<>();
        try {
            StringBuilder lats = new StringBuilder();
            StringBuilder lons = new StringBuilder();
            for (int i = 0; i < STATIONS.length; i++) {
                if (i > 0) { lats.append(","); lons.append(","); }
                lats.append(STATIONS[i][1]);
                lons.append(STATIONS[i][2]);
            }

            String url = BASE_URL
                    + "?latitude="  + lats
                    + "&longitude=" + lons
                    + "&current=pm2_5,us_aqi"
                    + "&timezone=UTC";

            HttpHeaders headers = new HttpHeaders();
                headers.setAccept(Objects.requireNonNull(
                    Collections.singletonList(MediaType.APPLICATION_JSON),
                    "accept media types must be provided"));
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    Objects.requireNonNull(HttpMethod.GET, "http method must be provided"),
                    new HttpEntity<>(headers),
                    String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null)
                return readings;

            JsonNode root      = objectMapper.readTree(response.getBody());
            JsonNode locations = root.isArray() ? root : objectMapper.createArrayNode().add(root);

            for (int i = 0; i < locations.size() && i < STATIONS.length; i++) {
                try {
                    JsonNode loc     = locations.get(i);
                    JsonNode current = loc.path("current");

                    double pm25  = current.path("pm2_5").asDouble(-1);
                    double aqiUs = current.path("us_aqi").asDouble(-1);
                    if (pm25 < 0) continue;

                    double lat = loc.path("latitude").asDouble();
                    double lon = loc.path("longitude").asDouble();

                    readings.add(new StationReading(
                            "OPENMETEO-" + i,
                            STATIONS[i][0],
                            lat,
                            lon,
                            aqiUs,
                            pm25
                    ));
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            log.warn("[OpenMeteo] No se pudieron obtener datos de calidad del aire: {}", e.getMessage());
        }
        return readings;
    }
}
