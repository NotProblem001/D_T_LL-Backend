package com.dtll.backend.service;

import com.dtll.backend.dto.empresa.EmpresaClienteRequest;
import com.dtll.backend.dto.empresa.EmpresaClienteResponse;
import com.dtll.backend.model.entity.EmpresaCliente;
import com.dtll.backend.repository.EmpresaClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** CRUD de empresas clientes para el módulo de Clientes del panel de administración. */
@Service
@RequiredArgsConstructor
public class EmpresaClienteService {

    private final EmpresaClienteRepository empresaClienteRepository;

    public List<EmpresaClienteResponse> listar() {
        return empresaClienteRepository.findAll().stream()
                .sorted(Comparator.comparing(
                        e -> nombreVisible(e).toLowerCase()))
                .map(EmpresaClienteResponse::desde)
                .toList();
    }

    @Transactional
    public EmpresaClienteResponse crear(EmpresaClienteRequest request) {
        String rut = normalizarRut(request.rutFiscal());
        validar(rut, request);
        empresaClienteRepository.findByRutFiscal(rut).ifPresent(e -> {
            throw new IllegalStateException("Ya existe una empresa con el RUT " + rut);
        });

        EmpresaCliente empresa = EmpresaCliente.builder()
                .rutFiscal(rut)
                .build();
        aplicarCampos(empresa, request);
        return EmpresaClienteResponse.desde(empresaClienteRepository.save(empresa));
    }

    @Transactional
    public EmpresaClienteResponse actualizar(UUID id, EmpresaClienteRequest request) {
        EmpresaCliente empresa = empresaClienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada: " + id));

        String rut = normalizarRut(request.rutFiscal());
        validar(rut, request);
        empresaClienteRepository.findByRutFiscal(rut)
                .filter(otra -> !otra.getId().equals(id))
                .ifPresent(otra -> {
                    throw new IllegalStateException("Ya existe otra empresa con el RUT " + rut);
                });

        empresa.setRutFiscal(rut);
        aplicarCampos(empresa, request);
        return EmpresaClienteResponse.desde(empresaClienteRepository.save(empresa));
    }

    @Transactional
    public void eliminar(UUID id) {
        EmpresaCliente empresa = empresaClienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada: " + id));
        try {
            empresaClienteRepository.delete(empresa);
            empresaClienteRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException(
                    "No se puede eliminar la empresa porque tiene datos asociados (pasajeros, viajes o nóminas)");
        }
    }

    private void aplicarCampos(EmpresaCliente empresa, EmpresaClienteRequest request) {
        empresa.setRazonSocial(limpiar(request.razonSocial()));
        empresa.setNombreFantasia(limpiar(request.nombreFantasia()));
        empresa.setContactoNombre(limpiar(request.contactoNombre()));
        empresa.setContactoEmail(limpiar(request.contactoEmail()));
        empresa.setContactoTelefono(limpiar(request.contactoTelefono()));
        empresa.setTarifaBaseViaje(request.tarifaBaseViaje());
    }

    private void validar(String rutFiscal, EmpresaClienteRequest request) {
        if (rutFiscal == null || rutFiscal.isBlank()) {
            throw new IllegalArgumentException("El RUT fiscal es obligatorio");
        }
        boolean sinNombre = (request.razonSocial() == null || request.razonSocial().isBlank())
                && (request.nombreFantasia() == null || request.nombreFantasia().isBlank());
        if (sinNombre) {
            throw new IllegalArgumentException("Debe indicar la razón social o el nombre de fantasía");
        }
    }

    /** Normaliza el RUT a formato sin puntos y con guión, ej: 76123456-7. */
    private String normalizarRut(String rut) {
        if (rut == null) return null;
        String limpio = rut.replace(".", "").replace(" ", "").toUpperCase();
        if (!limpio.contains("-") && limpio.length() > 1) {
            limpio = limpio.substring(0, limpio.length() - 1) + "-" + limpio.charAt(limpio.length() - 1);
        }
        return limpio;
    }

    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private String nombreVisible(EmpresaCliente e) {
        if (e.getNombreFantasia() != null && !e.getNombreFantasia().isBlank()) {
            return e.getNombreFantasia();
        }
        return e.getRazonSocial() != null ? e.getRazonSocial() : "";
    }
}
