package mype_backend.service;

import mype_backend.entity.Serie;
import mype_backend.repository.SerieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SerieService {

    @Autowired
    private SerieRepository serieRepository;

    @Autowired
    private EmpresaCompartidaService empresaCompartidaService;

    public List<Serie> listarPorUsuario(Long usuarioId) {
        return serieRepository.findByUsuarioId(empresaCompartidaService.getUsuarioEmpresaId());
    }

    public List<Serie> listarPorComprobante(Long usuarioId, Long tipoComprobanteId) {
        return serieRepository.findByUsuarioIdAndTipoComprobanteId(
                empresaCompartidaService.getUsuarioEmpresaId(), tipoComprobanteId);
    }

    public Serie guardar(Serie serie) {
        serie.setUsuarioId(empresaCompartidaService.getUsuarioEmpresaId());
        return serieRepository.save(serie);
    }

    public void eliminar(Long id) {
        serieRepository.deleteById(id);
    }
}

