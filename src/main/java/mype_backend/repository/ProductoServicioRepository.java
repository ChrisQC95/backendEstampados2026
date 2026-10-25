package mype_backend.repository;

import mype_backend.entity.ProductoServicio;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoServicioRepository extends JpaRepository<ProductoServicio, Long> {
    // Devuelve todos los productos de un usuario específico
    List<ProductoServicio> findByUsuarioId(Long usuarioId);
    List<ProductoServicio> findByUsuarioIdAndCodigoIn(Long usuarioId, List<String> codigos);

    // Opcional: Para buscar productos por categoría dentro de un usuario
    List<ProductoServicio> findByUsuarioIdAndCategoriaId(Long usuarioId, Long categoriaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProductoServicio p where p.id = :id")
    Optional<ProductoServicio> findByIdForUpdate(@Param("id") Long id);
}
