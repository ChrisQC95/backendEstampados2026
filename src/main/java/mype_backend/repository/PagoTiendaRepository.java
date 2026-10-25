package mype_backend.repository;

import mype_backend.entity.PagoTienda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PagoTiendaRepository extends JpaRepository<PagoTienda, Long> {
    Optional<PagoTienda> findByPedido_Id(Long pedidoId);
}
