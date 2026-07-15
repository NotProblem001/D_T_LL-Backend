package com.dtll.backend.repository;

import com.dtll.backend.model.entity.ReporteFacturacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReporteFacturacionRepository extends JpaRepository<ReporteFacturacion, UUID> {
    List<ReporteFacturacion> findByEmpresaClienteIdOrderByAnioFiscalDescMesFiscalDesc(UUID empresaId);
}
