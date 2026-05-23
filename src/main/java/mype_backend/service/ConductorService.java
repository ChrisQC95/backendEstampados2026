package mype_backend.service;

import mype_backend.entity.Conductor;
import mype_backend.repository.ConductorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ConductorService {

    @Autowired
    private ConductorRepository conductorRepository;

    public List<Conductor> listarPorUsuario(Long usuarioId) {
        return conductorRepository.findByUsuarioId(usuarioId);
    }

    public Conductor guardar(Conductor conductor) {
        return conductorRepository.save(conductor);
    }

    public void eliminar(Long id) {
        conductorRepository.deleteById(id);
    }
}