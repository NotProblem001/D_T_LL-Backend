package com.dtll.backend.dto.mensajeria;

import java.util.UUID;

/** Teléfono de un pasajero del recorrido, para enlaces wa.me y descarga de lista. */
public record TelefonoPasajeroResponse(UUID pasajeroId, String nombre, String telefono) {
}
