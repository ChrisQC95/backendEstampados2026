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

    @Autowired
    private EmpresaCompartidaService empresaCompartidaService;

    public List<Conductor> listarPorUsuario(Long usuarioId) {
        return conductorRepository.findByUsuarioId(empresaCompartidaService.getUsuarioEmpresaId());
    }

    public Conductor guardar(Conductor conductor) {
        conductor.setUsuarioId(empresaCompartidaService.getUsuarioEmpresaId());
        return conductorRepository.save(conductor);
    }

    public void eliminar(Long id) {
        conductorRepository.deleteById(id);
    }
}

