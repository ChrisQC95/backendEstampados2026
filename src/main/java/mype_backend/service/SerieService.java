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

    public List<Serie> listarPorUsuario(Long usuarioId) {
        return serieRepository.findByUsuarioId(usuarioId);
    }

    public List<Serie> listarPorComprobante(Long usuarioId, Long tipoComprobanteId) {
        return serieRepository.findByUsuarioIdAndTipoComprobanteId(usuarioId, tipoComprobanteId);
    }

    public Serie guardar(Serie serie) {
        return serieRepository.save(serie);
    }

    public void eliminar(Long id) {
        serieRepository.deleteById(id);
    }
}