package mype_backend.repository;

import mype_backend.entity.ProductoServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductoServicioRepository extends JpaRepository<ProductoServicio, Long> {
    // Devuelve todos los productos de un usuario específico
    List<ProductoServicio> findByUsuarioId(Long usuarioId);

    // Opcional: Para buscar productos por categoría dentro de un usuario
    List<ProductoServicio> findByUsuarioIdAndCategoriaId(Long usuarioId, Long categoriaId);
}