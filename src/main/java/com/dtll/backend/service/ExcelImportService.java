package com.dtll.backend.service;

import com.dtll.backend.model.entity.*;
import com.dtll.backend.repository.*;
import com.monitorjbl.xlsx.StreamingReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelImportService {

    private final EmpresaClienteRepository empresaRepository;
    private final ConductorRepository conductorRepository;
    private final PasajeroRepository pasajeroRepository;
    private final ViajeRepository viajeRepository;
    private final AsistenciaChecklistRepository asistenciaRepository;
    private final NotificationService notificationService;

    @Transactional
    public String procesarExcel(MultipartFile file) {
        log.info("Iniciando procesamiento de archivo Excel: {}", file.getOriginalFilename());
        int viajesCreados = 0;
        int pasajerosRegistrados = 0;

        try (InputStream is = file.getInputStream();
             Workbook workbook = StreamingReader.builder()
                     .rowCacheSize(100)
                     .bufferSize(4096)
                     .open(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            boolean isFirstRow = true;

            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue; // Saltar encabezados
                }

                if (row.getCell(0) == null || row.getCell(0).getStringCellValue().trim().isEmpty()) {
                    continue; // Fila vacía
                }

                // Asumimos formato de columnas:
                // 0: RUT Empresa, 1: RUT Conductor, 2: Fecha (dd/MM/yyyy), 3: Jornada, 4: Trayecto,
                // 5: ID Pasajero, 6: Nombre Pasajero, 7: Punto Parada, 8: Email (opcional)
                String rutEmpresa = row.getCell(0).getStringCellValue().trim();
                String rutConductor = row.getCell(1).getStringCellValue().trim();
                String fechaStr = row.getCell(2).getStringCellValue().trim();
                String jornada = row.getCell(3).getStringCellValue().trim();
                String trayecto = row.getCell(4).getStringCellValue().trim();
                String idPasajero = row.getCell(5).getStringCellValue().trim();
                String nombrePasajero = row.getCell(6).getStringCellValue().trim();
                String puntoParada = row.getCell(7) != null ? row.getCell(7).getStringCellValue().trim() : "";
                String emailPasajero = row.getCell(8) != null ? row.getCell(8).getStringCellValue().trim() : null;

                // Validaciones de negocio (si no existen, throw Exception para Rollback)
                EmpresaCliente empresa = empresaRepository.findByRutFiscal(rutEmpresa)
                        .orElseThrow(() -> new RuntimeException("Empresa no encontrada con RUT: " + rutEmpresa));

                Conductor conductor = conductorRepository.findByRutConductor(rutConductor)
                        .orElseThrow(() -> new RuntimeException("Conductor no encontrado con RUT: " + rutConductor));

                Pasajero pasajero = pasajeroRepository.findByIdentificadorInterno(idPasajero)
                        .orElseGet(() -> pasajeroRepository.save(Pasajero.builder()
                                .empresaCliente(empresa)
                                .identificadorInterno(idPasajero)
                                .nombreCompleto(nombrePasajero)
                                .nombreNormalizado(com.dtll.backend.util.Normalizador.nombre(nombrePasajero))
                                .puntoParadaAsignado(puntoParada)
                                .email(emailPasajero)
                                .build()));

                // Aislamiento B2B: si el pasajero ya existía, debe pertenecer a la empresa de la fila.
                if (!pasajero.getEmpresaCliente().getId().equals(empresa.getId())) {
                    throw new RuntimeException("El pasajero " + idPasajero
                            + " pertenece a otra empresa; fila rechazada por aislamiento de datos");
                }
                if (emailPasajero != null && !emailPasajero.isBlank()
                        && (pasajero.getEmail() == null || pasajero.getEmail().isBlank())) {
                    pasajero.setEmail(emailPasajero);
                    pasajeroRepository.save(pasajero);
                }

                LocalDate fechaOperacion = LocalDate.parse(fechaStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                // Agrupación: un mismo viaje (conductor+empresa+fecha+jornada+trayecto)
                // acumula pasajeros en su checklist en lugar de crear un viaje por fila.
                boolean[] viajeNuevo = {false};
                Viaje viaje = viajeRepository
                        .findByEmpresaClienteIdAndConductorIdAndFechaOperacionAndJornadaTurnoAndTipoTrayecto(
                                empresa.getId(), conductor.getId(), fechaOperacion, jornada, trayecto)
                        .orElseGet(() -> {
                            viajeNuevo[0] = true;
                            return viajeRepository.save(Viaje.builder()
                                    .codigoRutaLogin(generarCodigoRuta())
                                    .conductor(conductor)
                                    .empresaCliente(empresa)
                                    .fechaOperacion(fechaOperacion)
                                    .jornadaTurno(jornada)
                                    .tipoTrayecto(trayecto)
                                    .tarifaHistorica(empresa.getTarifaBaseViaje())
                                    .build());
                        });
                if (viajeNuevo[0]) {
                    viajesCreados++;
                    notificationService.notificarAsignacionConductor(conductor, viaje);
                }

                // Idempotencia: re-subir el mismo Excel no duplica asistencias.
                if (!asistenciaRepository.existsByViajeIdAndPasajeroId(viaje.getId(), pasajero.getId())) {
                    asistenciaRepository.save(AsistenciaChecklist.builder()
                            .viaje(viaje)
                            .pasajero(pasajero)
                            .build());
                    pasajerosRegistrados++;
                    notificationService.notificarAsignacionPasajero(pasajero, viaje);
                }
            }

            log.info("Procesamiento finalizado. Viajes creados: {}, Pasajeros registrados: {}", viajesCreados, pasajerosRegistrados);
            return String.format("Procesamiento exitoso. Se han generado %d Viajes y registrado %d Pasajeros.", viajesCreados, pasajerosRegistrados);

        } catch (Exception e) {
            log.error("Error al procesar el archivo Excel: ", e);
            throw new RuntimeException("Error en validación de negocio: " + e.getMessage());
        }
    }

    private String generarCodigoRuta() {
        // Reintenta hasta obtener un código no usado (colisión improbable pero posible).
        String codigo;
        do {
            codigo = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        } while (viajeRepository.existsByCodigoRutaLogin(codigo));
        return codigo;
    }
}
