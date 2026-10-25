package mype_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "tienda_productos",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_tienda_producto_mockup", columnNames = {"producto_servicio_id", "mockup_producto_base_id"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TiendaProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_servicio_id", nullable = false)
    private ProductoServicio productoServicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_estampado_id")
    private ProductoServicio servicioEstampado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mockup_producto_base_id", nullable = false)
    private MockupProductoBase mockupProductoBase;

    @Column(nullable = false, length = 150, unique = true)
    private String slug;

    @Column(name = "descripcion_tienda", columnDefinition = "text")
    private String descripcionTienda;

    @Column(nullable = false)
    @Builder.Default
    private Boolean publicado = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean destacado = false;

    @Column(name = "permite_personalizacion", nullable = false)
    @Builder.Default
    private Boolean permitePersonalizacion = true;

    @Column(name = "maneja_tallas")
    @Builder.Default
    private Boolean manejaTallas = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer orden = 0;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en")
    private OffsetDateTime actualizadoEn;
}
