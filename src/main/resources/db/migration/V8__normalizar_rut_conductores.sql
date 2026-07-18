-- Los RUT de conductores quedan en formato canónico (sin puntos ni espacios,
-- con guión) para que el login RUT+PIN encuentre al conductor sin importar
-- cómo se haya escrito el RUT al crearlo.
UPDATE conductores
SET rut_conductor = upper(replace(replace(rut_conductor, '.', ''), ' ', ''))
WHERE rut_conductor <> upper(replace(replace(rut_conductor, '.', ''), ' ', ''));
