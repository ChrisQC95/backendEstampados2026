package mype_backend.repository;

import mype_backend.dto.ReporteVentaExcelDTO;
import mype_backend.dto.VentasPorComprobanteDTO;
import mype_backend.dto.VentasPorProductoDTO;
import mype_backend.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    /** Historial completo (incluye NC) para mostrar en el frontend. */
    List<Venta> findByUsuarioId(Long usuarioId);

    /** Conteo de NC emitidas por el usuario — usado para numeración automática. */
    long countByUsuarioIdAndTipoComprobanteId(Long usuarioId, Long tipoComprobanteId);

    /** Ventas vigentes agrupadas por tipo de comprobante, para el reporte analítico. */
    @Query("""
            SELECT new mype_backend.dto.VentasPorComprobanteDTO(
                v.tipoComprobanteId,
                tc.descripcion,
                COUNT(v.id),
                SUM(v.total)
            )
            FROM Venta v
            JOIN TipoComprobante tc ON tc.id = v.tipoComprobanteId
            WHERE v.usuarioId = :usuarioId
              AND v.tipoComprobanteId <> 4
              AND (v.estadoSunat IS NULL OR UPPER(v.estadoSunat) <> 'ANULADO')
              AND v.documentoOrigenId IS NULL
            GROUP BY v.tipoComprobanteId, tc.descripcion
            ORDER BY tc.descripcion
            """)
    List<VentasPorComprobanteDTO> findVentasPorComprobante(@Param("usuarioId") Long usuarioId);

    /** Líneas de ventas vigentes agrupadas por producto y categoría. */
    @Query("""
            SELECT new mype_backend.dto.VentasPorProductoDTO(
                d.productoId,
                d.productoNombre,
                COALESCE(c.nombre, 'Sin categoría'),
                SUM(d.cantidad),
                SUM(d.totalLinea),
                COUNT(d.id)
            )
            FROM Venta v
            JOIN v.detalles d
            LEFT JOIN ProductoServicio p ON p.id = d.productoId
            LEFT JOIN Categoria c ON c.id = p.categoriaId
            WHERE v.usuarioId = :usuarioId
              AND v.tipoComprobanteId <> 4
              AND (v.estadoSunat IS NULL OR UPPER(v.estadoSunat) <> 'ANULADO')
              AND v.documentoOrigenId IS NULL
            GROUP BY d.productoId, d.productoNombre, c.nombre
            ORDER BY SUM(d.totalLinea) DESC
            """)
    List<VentasPorProductoDTO> findVentasPorProducto(@Param("usuarioId") Long usuarioId);

    /**
     * Reporte Excel Maestro-Detalle.
     * <p>
     * Excluye:
     * <ul>
     *   <li>Las Notas de Crédito (tipoComprobanteId = 4).</li>
     *   <li>Las ventas que ya tienen documentoOrigenId != null
     *       (han sido anuladas por una NC).</li>
     * </ul>
     */
    @Query("""
            SELECT new mype_backend.dto.ReporteVentaExcelDTO(
                v.fechaEmision,
                CASE v.tipoComprobanteId
                    WHEN 1L THEN 'FACTURA ELECTRÓNICA'
                    WHEN 2L THEN 'BOLETA DE VENTA'
                    ELSE 'COMPROBANTE'
                END,
                v.serie,
                v.correlativo,
                s.numeroDocumento,
                s.nombreRazonSocial,
                d.productoNombre,
                d.cantidad,
                d.precioUnitario,
                d.totalLinea,
                v.estadoSunat
            )
            FROM Venta v
            JOIN v.detalles d
            JOIN SocioNegocio s ON s.id = v.socioNegocioId
            WHERE v.usuarioId = :usuarioId
              AND v.fechaEmision >= :fechaInicio
              AND v.fechaEmision <= :fechaFin
              AND v.tipoComprobanteId <> 4
              AND v.documentoOrigenId IS NULL
            ORDER BY v.fechaEmision DESC, v.id ASC, d.id ASC
            """)
    List<ReporteVentaExcelDTO> findReporteExcel(
            @Param("usuarioId") Long usuarioId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );
}
