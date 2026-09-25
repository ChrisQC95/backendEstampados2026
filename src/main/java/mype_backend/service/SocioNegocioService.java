package mype_backend.service;

import mype_backend.entity.SocioNegocio;
import mype_backend.entity.Ubigeo;
import mype_backend.repository.SocioNegocioRepository;
import mype_backend.repository.UbigeoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SocioNegocioService {

    @Autowired
    private SocioNegocioRepository socioNegocioRepository;
    @Autowired
    private UbigeoRepository ubigeoRepository;
    @Autowired
    private EmpresaCompartidaService empresaCompartidaService;

    public List<SocioNegocio> listarPorUsuario(Long usuarioId) {
        return socioNegocioRepository.findByUsuarioId(empresaCompartidaService.getUsuarioEmpresaId());
    }

    public List<SocioNegocio> listarClientes(Long usuarioId) {
        List<SocioNegocio> todos = socioNegocioRepository.findByUsuarioId(empresaCompartidaService.getUsuarioEmpresaId());
        return todos.stream()
                .filter(s -> s.getTipoSocio().equals("C") || s.getTipoSocio().equals("A"))
                .toList();
    }

    public SocioNegocio guardar(SocioNegocio socio, String codigoUbigeo) {
        socio.setUsuarioId(empresaCompartidaService.getUsuarioEmpresaId());
        if (codigoUbigeo != null && !codigoUbigeo.isBlank()) {
            Ubigeo ubigeo = ubigeoRepository
                    .findById(codigoUbigeo)
                    .orElseThrow(() -> new RuntimeException("Ubigeo no encontrado: " + codigoUbigeo));
            socio.setUbigeo(ubigeo);
        } else {
            socio.setUbigeo(null);
        }
        return socioNegocioRepository.save(socio);
    }

    public void eliminar(Long id) {
        socioNegocioRepository.deleteById(id);
    }
}

