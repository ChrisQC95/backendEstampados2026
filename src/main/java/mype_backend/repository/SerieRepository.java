package mype_backend.repository;

import mype_backend.entity.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SerieRepository extends JpaRepository<Serie, Long> {

    // Lista todas las series configuradas por una MYPE
    List<Serie> findByUsuarioId(Long usuarioId);

    // Lista las series de un usuario filtradas por el tipo de comprobante (Ej: Solo
    // facturas)
    List<Serie> findByUsuarioIdAndTipoComprobanteId(Long usuarioId, Long tipoComprobanteId);
}
