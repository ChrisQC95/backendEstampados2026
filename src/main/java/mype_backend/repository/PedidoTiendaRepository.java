package mype_backend.repository;

import mype_backend.dto.PedidosTiendaPorEstadoDTO;
import mype_backend.dto.ProductosTiendaMasSolicitadosDTO;
import mype_backend.entity.PedidoTienda;
import java.util.Collection;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PedidoTiendaRepository extends JpaRepository<PedidoTienda, Long> {
    Optional<PedidoTienda> findByCodigo(String codigo);
    Optional<PedidoTienda> findByCodigoAndUsuarioId(String codigo, Long usuarioId);

    @Query("""
            SELECT DISTINCT p FROM PedidoTienda p
            LEFT JOIN FETCH p.items
            LEFT JOIN FETCH p.pago
            WHERE p.usuarioId = :usuarioId
            ORDER BY p.fechaCreacion DESC
            """)
    List<PedidoTienda> findAllForCliente(@Param("usuarioId") Long usuarioId);

    @Query("""
            SELECT DISTINCT p FROM PedidoTienda p
            LEFT JOIN FETCH p.items
            LEFT JOIN FETCH p.pago
            ORDER BY p.fechaCreacion DESC
            """)
    List<PedidoTienda> findAllForGestion();

    @Query("""
            SELECT DISTINCT p FROM PedidoTienda p
            JOIN FETCH p.items i
            JOIN FETCH p.pago pago
            WHERE COALESCE(p.estadoFacturacion, 'PENDIENTE') = 'PENDIENTE'
              AND pago.estado = 'APROBADO'
              AND p.numeroDocumentoFacturacion IS NOT NULL
              AND p.usuarioId IN (
                SELECT u.id FROM Usuario u WHERE u.rol.codigo = 'CLIENTE_TIENDA'
              )
            ORDER BY p.fechaCreacion DESC
            """)
    List<PedidoTienda> findPedidosPendientesFacturacion();

    @Query("""
            SELECT DISTINCT p FROM PedidoTienda p
            JOIN FETCH p.items i
            JOIN FETCH p.pago pago
            WHERE p.id IN :ids
              AND COALESCE(p.estadoFacturacion, 'PENDIENTE') = 'PENDIENTE'
              AND pago.estado = 'APROBADO'
              AND p.numeroDocumentoFacturacion IS NOT NULL
              AND p.usuarioId IN (
                SELECT u.id FROM Usuario u WHERE u.rol.codigo = 'CLIENTE_TIENDA'
              )
            """)
    List<PedidoTienda> findPedidosPendientesFacturacionPorIds(@Param("ids") Collection<Long> ids);

    // Keep the lock query rooted only at PedidoTienda. Combining a pessimistic lock
    // with fetch joins to the payment and item tables breaks Hibernate follow-on locking.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT p FROM PedidoTienda p
            WHERE p.id IN :ids
              AND COALESCE(p.estadoFacturacion, 'PENDIENTE') = 'PENDIENTE'
              AND EXISTS (
                SELECT pago.id FROM PagoTienda pago
                WHERE pago.pedido = p AND pago.estado = 'APROBADO'
              )
              AND EXISTS (
                SELECT item.id FROM PedidoTiendaItem item WHERE item.pedido = p
              )
              AND p.numeroDocumentoFacturacion IS NOT NULL
              AND p.usuarioId IN (
                SELECT u.id FROM Usuario u WHERE u.rol.codigo = 'CLIENTE_TIENDA'
              )
            """)
    List<PedidoTienda> findPedidosPendientesFacturacionPorIdsForUpdate(@Param("ids") Collection<Long> ids);

    /** Totales y cantidades de pedidos agrupados por estado. */
    @Query("""
            SELECT new mype_backend.dto.PedidosTiendaPorEstadoDTO(
                p.estado,
                COUNT(p.id),
                SUM(p.total),
                SUM(p.subtotal),
                SUM(p.costoEnvio)
            )
            FROM PedidoTienda p
            WHERE p.usuarioId IN (
                SELECT u.id FROM Usuario u WHERE u.rol.codigo = 'CLIENTE_TIENDA'
            )
            GROUP BY p.estado
            ORDER BY p.estado
            """)
    List<PedidosTiendaPorEstadoDTO> resumirPedidosPorEstado();

    /** Productos solicitados agrupados por producto de tienda; excluye pedidos cancelados. */
    @Query("""
            SELECT new mype_backend.dto.ProductosTiendaMasSolicitadosDTO(
                i.tiendaProductoId,
                i.nombreProducto,
                i.nombreServicioEstampado,
                SUM(i.cantidad),
                SUM(i.subtotal),
                COUNT(DISTINCT p.id)
            )
            FROM PedidoTienda p
            JOIN p.items i
            WHERE p.usuarioId IN (
                SELECT u.id FROM Usuario u WHERE u.rol.codigo = 'CLIENTE_TIENDA'
            )
              AND UPPER(p.estado) <> 'CANCELADO'
            GROUP BY i.tiendaProductoId, i.nombreProducto, i.nombreServicioEstampado
            ORDER BY SUM(i.subtotal) DESC
            """)
    List<ProductosTiendaMasSolicitadosDTO> resumirProductosMasSolicitados();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PedidoTienda p where p.id = :pedidoId")
    Optional<PedidoTienda> findByIdForUpdate(@Param("pedidoId") Long pedidoId);
}
