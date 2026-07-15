package com.dtll.backend.service;

import com.dtll.backend.dto.empresa.PasajeroEmpresaResponse;
import com.dtll.backend.dto.empresa.ReporteFacturacionResponse;
import com.dtll.backend.dto.empresa.ResumenEmpresaResponse;
import com.dtll.backend.repository.PasajeroRepository;
import com.dtll.backend.repository.ReporteFacturacionRepository;
import com.dtll.backend.repository.ViajeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmpresaPortalService {

    private final ViajeRepository viajeRepository;
    private final PasajeroRepository pasajeroRepository;
    private final ReporteFacturacionRepository reporteFacturacionRepository;

    public ResumenEmpresaResponse resumen(UUID empresaId) {
        YearMonth mesActual = YearMonth.now();
        LocalDate desde = mesActual.atDay(1);
        LocalDate hasta = mesActual.atEndOfMonth();

        long viajesEsteMes = viajeRepository.countByEmpresaClienteIdAndFechaOperacionBetween(empresaId, desde, hasta);
        long pasajerosActivos = pasajeroRepository.findByEmpresaClienteIdOrderByNombreCompletoAsc(empresaId).stream()
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .count();

        return new ResumenEmpresaResponse(viajesEsteMes, pasajerosActivos, mesActual.getMonthValue(), mesActual.getYear());
    }

    public List<ReporteFacturacionResponse> reportesFacturacion(UUID empresaId) {
        return reporteFacturacionRepository.findByEmpresaClienteIdOrderByAnioFiscalDescMesFiscalDesc(empresaId).stream()
                .map(r -> new ReporteFacturacionResponse(
                        r.getId(), r.getMesFiscal(), r.getAnioFiscal(),
                        r.getTotalViajesEjecutados(), r.getMontoExentoTotal(), r.getEstadoDocumento()))
                .toList();
    }

    public List<PasajeroEmpresaResponse> pasajeros(UUID empresaId) {
        return pasajeroRepository.findByEmpresaClienteIdOrderByNombreCompletoAsc(empresaId).stream()
                .map(p -> new PasajeroEmpresaResponse(
                        p.getId(), p.getIdentificadorInterno(), p.getNombreCompleto(),
                        p.getComuna(), p.getPuntoParadaAsignado(), Boolean.TRUE.equals(p.getActivo())))
                .toList();
    }
}
