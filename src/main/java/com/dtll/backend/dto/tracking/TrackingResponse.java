package com.dtll.backend.dto.tracking;

import java.time.LocalDateTime;
import java.util.UUID;

public record TrackingResponse(UUID viajeId, double lat, double lng, Double heading, LocalDateTime updatedAt) {
}
