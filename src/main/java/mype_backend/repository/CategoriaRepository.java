package mype_backend.repository;

import mype_backend.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    // Magia de Spring: Esto crea "SELECT * FROM categorias WHERE usuario_id = ?"
    List<Categoria> findByUsuarioId(Long usuarioId);
}