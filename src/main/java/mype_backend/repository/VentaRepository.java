package mype_backend.repository;

import mype_backend.dto.ReporteVentaExcelDTO;
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
