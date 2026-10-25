package mype_backend.repository;

import mype_backend.entity.PedidoTiendaItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoTiendaItemRepository extends JpaRepository<PedidoTiendaItem, Long> {
    List<PedidoTiendaItem> findByPedido_Id(Long pedidoId);
}
