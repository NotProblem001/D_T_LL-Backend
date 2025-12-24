package com.example.Dispatch_Service;

import com.example.Dispatch_Service.model.Dispatch;
import com.example.Dispatch_Service.model.DispatchStatus;
import com.example.Dispatch_Service.model.Vehicle;
import com.example.Dispatch_Service.model.VehicleStatus;
import com.example.Dispatch_Service.repository.DispatchRepository;
import com.example.Dispatch_Service.repository.VehicleRepository;
import com.example.Dispatch_Service.service.DispatchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DispatchServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DispatchRepository dispatchRepository;

    @InjectMocks
    private DispatchService dispatchService;

    @Test
    public void testDispatchBooking_WhenVehicleAvailable() {
        // Arrange
        Vehicle mockVehicle = new Vehicle();
        mockVehicle.setId("v1");
        mockVehicle.setDriverId("d1");
        mockVehicle.setStatus(VehicleStatus.AVAILABLE);

        when(vehicleRepository.findFirstByStatus(VehicleStatus.AVAILABLE)).thenReturn(Optional.of(mockVehicle));
        when(dispatchRepository.save(any(Dispatch.class))).thenAnswer(i -> {
            Dispatch d = i.getArgument(0);
            d.setId("dispatch_123");
            return d;
        });

        // Act
        Dispatch result = dispatchService.dispatchBooking("booking_001");

        // Assert
        assertNotNull(result);
        assertEquals(DispatchStatus.ASSIGNED, result.getStatus());
        assertEquals("d1", result.getDriverId());
        assertEquals("v1", result.getVehicleId());

        // Verify vehicle was marked as BUSY
        assertEquals(VehicleStatus.BUSY, mockVehicle.getStatus());
        verify(vehicleRepository).save(mockVehicle);
    }

    @Test
    public void testDispatchBooking_WhenNoVehicleAvailable() {
        // Arrange
        when(vehicleRepository.findFirstByStatus(VehicleStatus.AVAILABLE)).thenReturn(Optional.empty());
        when(dispatchRepository.save(any(Dispatch.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Dispatch result = dispatchService.dispatchBooking("booking_002");

        // Assert
        assertNotNull(result);
        assertEquals(DispatchStatus.SEARCHING, result.getStatus());
        assertNull(result.getDriverId());
    }
}
