package com.example.Trip_Service.controller;

import com.example.Trip_Service.model.Trip;
import com.example.Trip_Service.model.TripStatus;
import com.example.Trip_Service.service.TripService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class TripControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TripService tripService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testStartTrip() throws Exception {
        Trip trip = new Trip();
        trip.setBookingId("booking1");

        Trip startedTrip = new Trip();
        startedTrip.setId("trip1");
        startedTrip.setBookingId("booking1");
        startedTrip.setStatus(TripStatus.STARTED);

        Mockito.when(tripService.startTrip(any(Trip.class))).thenReturn(startedTrip);

        mockMvc.perform(post("/trips/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trip)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(startedTrip)));
    }

    @Test
    public void testEndTrip() throws Exception {
        Trip endedTrip = new Trip();
        endedTrip.setId("trip1");
        endedTrip.setStatus(TripStatus.ENDED);

        Mockito.when(tripService.endTrip(eq("trip1"))).thenReturn(endedTrip);

        mockMvc.perform(post("/trips/trip1/end"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(endedTrip)));
    }
}
