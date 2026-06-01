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

    // Para mostrar el historial de ventas en el Dashboard de la MYPE
    List<Venta> findByUsuarioId(Long usuarioId);

    /**
     * Reporte Excel Maestro-Detalle.
     * Hace JOIN entre ventas, ventas_detalle y socios_negocio filtrado por usuario y rango de fechas.
     * Cada fila representa un ítem de detalle con sus datos de cabecera.
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
            ORDER BY v.fechaEmision DESC, v.id ASC, d.id ASC
            """)
    List<ReporteVentaExcelDTO> findReporteExcel(
            @Param("usuarioId") Long usuarioId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );
}