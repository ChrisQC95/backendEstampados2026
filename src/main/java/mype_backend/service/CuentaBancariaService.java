package mype_backend.service;

import mype_backend.entity.CuentaBancaria;
import mype_backend.repository.CuentaBancariaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CuentaBancariaService {

    @Autowired
    private CuentaBancariaRepository repository;

    @Autowired
    private EmpresaCompartidaService empresaCompartidaService;

    public List<CuentaBancaria> listarPorUsuario(Long usuarioId) {
        return repository.findByUsuarioId(empresaCompartidaService.getUsuarioEmpresaId());
    }

    @Transactional
    public CuentaBancaria guardar(CuentaBancaria cuenta) {
        Long usuarioEmpresaId = empresaCompartidaService.getUsuarioEmpresaId();
        cuenta.setUsuarioId(usuarioEmpresaId);
        List<CuentaBancaria> existentes = repository.findByUsuarioId(usuarioEmpresaId);

        if (existentes.isEmpty()) {
            cuenta.setActivo(true);
        }

        if (Boolean.TRUE.equals(cuenta.getActivo())) {
            repository.desactivarTodasPorUsuario(usuarioEmpresaId);
        } else {
            boolean otraActiva = existentes.stream()
                    .anyMatch(c -> Boolean.TRUE.equals(c.getActivo()) && !c.getId().equals(cuenta.getId()));
            if (!otraActiva) {
                cuenta.setActivo(true);
            }
        }

        return repository.save(cuenta);
    }

    @Transactional
    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}

