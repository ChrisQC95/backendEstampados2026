package mype_backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos_tienda", uniqueConstraints = {
        @UniqueConstraint(name = "uq_pedidos_tienda_codigo", columnNames = "codigo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoTienda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "carrito_id")
    private Long carritoId;

    @Column(nullable = false, length = 40, unique = true)
    private String codigo;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String estado = "PENDIENTE_PAGO";

    @Column(name = "nombre_cliente", nullable = false, length = 150)
    private String nombreCliente;

    @Column(name = "email_cliente", nullable = false, length = 150)
    private String emailCliente;

    @Column(name = "telefono_cliente", length = 30)
    private String telefonoCliente;

    @Column(name = "direccion_entrega", length = 255)
    private String direccionEntrega;

    @Column(name = "ubigeo_entrega", length = 6)
    private String ubigeoEntrega;

    @Column(name = "referencia_entrega", length = 255)
    private String referenciaEntrega;

    @Column(name = "tipo_comprobante_facturacion", length = 10)
    private String tipoComprobanteFacturacion;

    @Column(name = "tipo_documento_facturacion", length = 2)
    private String tipoDocumentoFacturacion;

    @Column(name = "numero_documento_facturacion", length = 20)
    private String numeroDocumentoFacturacion;

    @Column(name = "nombre_facturacion", length = 255)
    private String nombreFacturacion;

    @Column(name = "direccion_fiscal_facturacion", length = 255)
    private String direccionFiscalFacturacion;

    @Column(name = "ubigeo_fiscal_facturacion", length = 6)
    private String ubigeoFiscalFacturacion;

    @Column(name = "email_facturacion", length = 150)
    private String emailFacturacion;

    // Nullable for rows created before the invoicing queue existed.
    @Column(name = "estado_facturacion", length = 20)
    @Builder.Default
    private String estadoFacturacion = "PENDIENTE";

    @Column(name = "venta_id")
    private Long ventaId;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "costo_envio", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal costoEnvio = BigDecimal.ZERO;

    @Column(name = "producto_servicio_envio_id")
    private Long productoServicioEnvioId;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @Column(columnDefinition = "text")
    private String observaciones;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "actualizado_en")
    private OffsetDateTime actualizadoEn;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<PedidoTiendaItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PagoTienda pago;

    public void agregarItem(PedidoTiendaItem item) {
        items.add(item);
        item.setPedido(this);
    }

    public void asignarPago(PagoTienda pago) {
        this.pago = pago;
        pago.setPedido(this);
    }

    @PrePersist
    void prePersist() {
        if (fechaCreacion == null) fechaCreacion = OffsetDateTime.now();
    }
}
