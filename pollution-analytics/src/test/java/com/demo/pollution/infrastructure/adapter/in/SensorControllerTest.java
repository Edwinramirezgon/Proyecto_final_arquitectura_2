package com.demo.pollution.infrastructure.adapter.in;

import com.demo.pollution.application.port.in.ProcessReadingUseCase;
import com.demo.pollution.application.port.in.SensorQueryUseCase;
import com.demo.pollution.domain.exception.InconsistentReadingException;
import com.demo.pollution.infrastructure.adapter.in.dto.SensorReadingRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SensorController.class)
class SensorControllerTest {

    @Autowired MockMvc     mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean ProcessReadingUseCase processReadingUseCase;
    @MockBean SensorQueryUseCase    sensorQueryUseCase;

    private SensorReadingRequest requestValido() {
        SensorReadingRequest r = new SensorReadingRequest();
        r.setSensorId("S-01");
        r.setZoneId("ZONA-1");
        r.setLatitude(4.71);
        r.setLongitude(-74.07);
        r.setCo2Level(120.0);
        return r;
    }

    @Test
    void postReadings_lecturaValida_retorna202() throws Exception {
        doNothing().when(processReadingUseCase).process(any());

        mockMvc.perform(post("/sensors/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void postReadings_lecturaInconsistente_retorna422() throws Exception {
        doThrow(new InconsistentReadingException("S-01", 100.0, 350.0))
                .when(processReadingUseCase).process(any());

        mockMvc.perform(post("/sensors/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void postReadings_argumentoInvalido_retorna400() throws Exception {
        doThrow(new IllegalArgumentException("El nivel de CO2 no puede ser negativo."))
                .when(processReadingUseCase).process(any());

        mockMvc.perform(post("/sensors/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getReadings_retornaListaVacia_cuando_noHayLecturas() throws Exception {
        when(sensorQueryUseCase.findRecentBySensor("S-01", 60)).thenReturn(List.of());

        mockMvc.perform(get("/sensors/S-01/readings"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
