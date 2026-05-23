package mype_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "socio_negocio_id", nullable = false)
    private Long socioNegocioId;

    @Column(name = "tipo_comprobante_id", nullable = false)
    private Long tipoComprobanteId;

    @Column(name = "tipo_operacion_id", nullable = false)
    private Long tipoOperacionId;

    @Column(nullable = false, length = 4)
    private String serie;

    @Column(nullable = false)
    private Integer correlativo;

    @Column(name = "fecha_emision")
    @Builder.Default
    private LocalDateTime fechaEmision = LocalDateTime.now();

    @Column(name = "moneda_id", nullable = false)
    private Long monedaId;

    @Column(name = "tipo_pago_id", nullable = false)
    private Long tipoPagoId;

    @Column(name = "op_gravadas", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal opGravadas = BigDecimal.ZERO;

    @Column(name = "op_exoneradas", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal opExoneradas = BigDecimal.ZERO;

    @Column(name = "op_inafectas", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal opInafectas = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal igv = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "estado_sunat", length = 20)
    @Builder.Default
    private String estadoSunat = "PENDIENTE";

    @Column(name = "sunat_hash_cdr", columnDefinition = "TEXT")
    private String sunatHashCdr;

    @Column(name = "documento_modificado_id")
    private Long documentoModificadoId;

    @Column(name = "motivo_nc_codigo", length = 2)
    private String motivoNcCodigo;

    @Column(name = "sustento_nota", length = 255)
    private String sustentoNota;

    @Column(name = "documento_origen_id")
    private Long documentoOrigenId;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    // LA RELACIÓN DE CASCADA CON LOS DETALLES
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<VentaDetalle> detalles = new ArrayList<>();

    // Método de utilidad para sincronizar ambos lados de la relación bidireccional
    public void agregarDetalle(VentaDetalle detalle) {
        if (detalles == null) {
            detalles = new ArrayList<>();
        }
        detalles.add(detalle);
        detalle.setVenta(this);
    }
}