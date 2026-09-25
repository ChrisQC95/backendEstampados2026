package mype_backend.service;

import mype_backend.entity.Vehiculo;
import mype_backend.repository.VehiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class VehiculoService {

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Autowired
    private EmpresaCompartidaService empresaCompartidaService;

    public List<Vehiculo> listarPorUsuario(Long usuarioId) {
        return vehiculoRepository.findByUsuarioId(empresaCompartidaService.getUsuarioEmpresaId());
    }

    public Vehiculo guardar(Vehiculo vehiculo) {
        vehiculo.setUsuarioId(empresaCompartidaService.getUsuarioEmpresaId());
        return vehiculoRepository.save(vehiculo);
    }

    public void eliminar(Long id) {
        vehiculoRepository.deleteById(id);
    }
}

