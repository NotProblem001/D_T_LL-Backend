package com.example.Booking_Service;

import com.example.Booking_Service.client.DispatchClient;
import com.example.Booking_Service.client.DispatchResponse;
import com.example.Booking_Service.model.Booking;
import com.example.Booking_Service.model.BookingStatus;
import com.example.Booking_Service.repository.BookingRepository;
import com.example.Booking_Service.service.BookingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private DispatchClient dispatchClient;

    @InjectMocks
    private BookingService bookingService;

    @Test
    public void testCreateBooking_AndAssignDriver() {
        // Arrange
        Booking booking = new Booking();
        booking.setId("b1");

        DispatchResponse mockResponse = new DispatchResponse();
        mockResponse.setStatus("ASSIGNED");
        mockResponse.setDriverId("driver_55");

        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));
        when(dispatchClient.requestDispatch(any())).thenReturn(mockResponse);

        // Act
        Booking result = bookingService.createBooking(booking);

        // Assert
        assertEquals(BookingStatus.ASSIGNED, result.getStatus());
        assertEquals("driver_55", result.getDriverId());

        verify(dispatchClient).requestDispatch("b1");
    }

    @Test
    public void testCreateBooking_NoDriverFound() {
        // Arrange
        Booking booking = new Booking();
        booking.setId("b2");

        DispatchResponse mockResponse = new DispatchResponse();
        mockResponse.setStatus("SEARCHING");

        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));
        when(dispatchClient.requestDispatch(any())).thenReturn(mockResponse);

        // Act
        Booking result = bookingService.createBooking(booking);

        // Assert
        assertEquals(BookingStatus.PENDING, result.getStatus());
        assertNull(result.getDriverId());
    }
}
