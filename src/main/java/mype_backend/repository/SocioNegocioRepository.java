package mype_backend.repository;

import mype_backend.entity.SocioNegocio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SocioNegocioRepository extends JpaRepository<SocioNegocio, Long> {

    // Lista TODOS los socios (para la pantalla de mantenimiento/CRUD)
    List<SocioNegocio> findByUsuarioId(Long usuarioId);

    // Lista SOLO clientes o SOLO proveedores (útil para los selects de
    // ventas/compras)
    List<SocioNegocio> findByUsuarioIdAndTipoSocio(Long usuarioId, String tipoSocio);
}
