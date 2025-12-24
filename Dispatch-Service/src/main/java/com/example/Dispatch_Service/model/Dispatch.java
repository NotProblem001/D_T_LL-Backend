package com.example.Dispatch_Service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "dispatches")
public class Dispatch {
    @Id
    private String id;
    private String bookingId;
    private String vehicleId;
    private String driverId;
    private DispatchStatus status;
    private LocalDateTime dispatchedAt = LocalDateTime.now();
}
