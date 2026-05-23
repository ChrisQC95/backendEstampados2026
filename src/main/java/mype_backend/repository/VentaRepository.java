package mype_backend.repository;

import mype_backend.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    // Para mostrar el historial de ventas en el Dashboard de la MYPE
    List<Venta> findByUsuarioId(Long usuarioId);
}