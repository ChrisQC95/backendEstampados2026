package mype_backend.repository;

import mype_backend.entity.TiendaProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TiendaProductoRepository extends JpaRepository<TiendaProducto, Long> {

    List<TiendaProducto> findAllByOrderByOrdenAscIdDesc();

    List<TiendaProducto> findByPublicadoTrueOrderByOrdenAscIdDesc();

    @Query("""
            select tp
            from TiendaProducto tp
            join fetch tp.productoServicio ps
            join fetch tp.mockupProductoBase mpb
            left join fetch tp.servicioEstampado se
            where tp.publicado = true
              and mpb.activo = true
            order by tp.orden asc, tp.id desc
            """)
    List<TiendaProducto> findPublicadosConDetalle();

    Optional<TiendaProducto> findBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}


