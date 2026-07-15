package com.dtll.backend.repository;

import com.dtll.backend.model.entity.EmpresaCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmpresaClienteRepository extends JpaRepository<EmpresaCliente, UUID> {
    Optional<EmpresaCliente> findByRutFiscal(String rutFiscal);
}
