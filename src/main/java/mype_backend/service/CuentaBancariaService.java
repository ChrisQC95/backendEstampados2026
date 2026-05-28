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

    public List<CuentaBancaria> listarPorUsuario(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }

    @Transactional
    public CuentaBancaria guardar(CuentaBancaria cuenta) {
        List<CuentaBancaria> existentes = repository.findByUsuarioId(cuenta.getUsuarioId());

        // Regla de negocio 1: Si es la primera cuenta del usuario, obligatoriamente es
        // activa
        if (existentes.isEmpty()) {
            cuenta.setActivo(true);
        }

        // Regla de negocio 2: Si esta cuenta se marca como activa, desactivamos el
        // resto en la BD
        if (cuenta.getActivo()) {
            repository.desactivarTodasPorUsuario(cuenta.getUsuarioId());
        } else {
            // Validamos que no intente desactivar la única cuenta activa que tiene
            boolean otraActiva = existentes.stream()
                    .anyMatch(c -> c.getActivo() && !c.getId().equals(cuenta.getId()));
            if (!otraActiva) {
                cuenta.setActivo(true); // Forzamos activo si no hay otra opción elegible
            }
        }

        return repository.save(cuenta);
    }

    @Transactional
    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}