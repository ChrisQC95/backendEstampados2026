package mype_backend.repository;

import mype_backend.entity.EmpresaConfiguracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmpresaConfiguracionRepository extends JpaRepository<EmpresaConfiguracion, Long> {
    Optional<EmpresaConfiguracion> findByUsuarioId(Long usuarioId);
}
