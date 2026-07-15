-- SRS 2.4: para notificar recogidas/cambios por Gmail, el pasajero necesita email.
ALTER TABLE pasajeros ADD COLUMN email VARCHAR(255);
