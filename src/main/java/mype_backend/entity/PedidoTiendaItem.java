package mype_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "pedido_tienda_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoTiendaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_tienda_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PedidoTienda pedido;

    @Column(name = "tienda_producto_id", nullable = false)
    private Long tiendaProductoId;

    @Column(name = "producto_servicio_id")
    private Long productoServicioId;

    @Column(name = "servicio_estampado_id")
    private Long servicioEstampadoId;

    @Column(name = "nombre_producto", nullable = false, length = 255)
    private String nombreProducto;

    @Column(name = "nombre_servicio_estampado", length = 255)
    private String nombreServicioEstampado;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "color_seleccionado", length = 50)
    private String colorSeleccionado;

    @Column(length = 30)
    private String talla;

    @Column(name = "precio_producto", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioProducto;

    @Column(name = "precio_estampado", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal precioEstampado = BigDecimal.ZERO;

    @Column(name = "precio_unitario_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitarioTotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "imagen_cliente_path", length = 500)
    private String imagenClientePath;

    @Column(name = "resultado_mockup_path", length = 500)
    private String resultadoMockupPath;

    // El esquema almacena la posición/tamaño del diseño como coordenadas numéricas.
    @Column(name = "area_x", precision = 10, scale = 2)
    private BigDecimal areaX;

    @Column(name = "area_y", precision = 10, scale = 2)
    private BigDecimal areaY;

    @Column(name = "area_width", precision = 10, scale = 2)
    private BigDecimal areaWidth;

    @Column(name = "area_height", precision = 10, scale = 2)
    private BigDecimal areaHeight;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    @PrePersist
    void prePersist() {
        if (fechaCreacion == null) fechaCreacion = OffsetDateTime.now();
    }
}
