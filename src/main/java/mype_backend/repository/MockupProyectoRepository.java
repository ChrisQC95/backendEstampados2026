package mype_backend.repository;

import mype_backend.entity.MockupProyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockupProyectoRepository extends JpaRepository<MockupProyecto, Long> {

    List<MockupProyecto> findAllByOrderByCreadoEnDesc();

    List<MockupProyecto> findByCreadoPorUsuarioIdOrderByCreadoEnDesc(Long usuarioId);
}
