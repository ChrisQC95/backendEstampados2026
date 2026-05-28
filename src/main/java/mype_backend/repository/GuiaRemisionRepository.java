package mype_backend.repository;

import mype_backend.entity.GuiaRemision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface GuiaRemisionRepository extends JpaRepository<GuiaRemision, Long> {
    Optional<GuiaRemision> findByVentaId(Long ventaId);
}