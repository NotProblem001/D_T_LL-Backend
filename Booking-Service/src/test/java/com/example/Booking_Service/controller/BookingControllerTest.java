package com.example.Booking_Service.controller;

import com.example.Booking_Service.model.Booking;
import com.example.Booking_Service.model.BookingStatus;
import com.example.Booking_Service.model.Location;
import com.example.Booking_Service.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateBooking() throws Exception {
        Booking booking = new Booking();
        booking.setPassengerId("user123");
        booking.setOrigin(new Location("Origin A", 0.0, 0.0));
        booking.setDestination(new Location("Destination B", 0.0, 0.0));

        Booking savedBooking = new Booking();
        savedBooking.setId("booking1");
        savedBooking.setPassengerId("user123");
        savedBooking.setOrigin(new Location("Origin A", 0.0, 0.0));
        savedBooking.setDestination(new Location("Destination B", 0.0, 0.0));
        savedBooking.setStatus(BookingStatus.PENDING);
        savedBooking.setCreatedAt(LocalDateTime.now());

        Mockito.when(bookingService.createBooking(any(Booking.class))).thenReturn(savedBooking);

        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(booking)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(savedBooking)));
    }

    @Test
    public void testGetAllBookings() throws Exception {
        Booking b1 = new Booking();
        b1.setId("1");

        Mockito.when(bookingService.getAllBookings()).thenReturn(List.of(b1));

        mockMvc.perform(get("/bookings"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(b1))));
    }
}
