package com.example.Dispatch_Service.controller;

import com.example.Dispatch_Service.model.Dispatch;
import com.example.Dispatch_Service.model.DispatchStatus;
import com.example.Dispatch_Service.service.DispatchService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class DispatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DispatchService dispatchService;

    @Test
    public void testRequestDispatch() throws Exception {
        Dispatch dispatch = new Dispatch();
        dispatch.setBookingId("booking1");
        dispatch.setStatus(DispatchStatus.SEARCHING);

        Mockito.when(dispatchService.dispatchBooking("booking1")).thenReturn(dispatch);

        mockMvc.perform(post("/dispatch/request")
                .param("bookingId", "booking1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateVehicleStatus() throws Exception {
        mockMvc.perform(put("/dispatch/vehicle/driver1/status")
                .param("status", "AVAILABLE"))
                .andExpect(status().isOk());

        Mockito.verify(dispatchService).updateVehicleStatus("driver1", "AVAILABLE");
    }
}
