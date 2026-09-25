package mype_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "mockup_productos_base")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockupProductoBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(name = "tipo_producto", nullable = false, length = 50)
    private String tipoProducto;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String color = "VARIABLE";

    @Column(name = "colores_disponibles", columnDefinition = "text")
    private String coloresDisponibles;

    @Column(name = "imagen_base_path", nullable = false, length = 500)
    private String imagenBasePath;

    @Column(name = "area_x", nullable = false, precision = 10, scale = 2)
    private BigDecimal areaX;

    @Column(name = "area_y", nullable = false, precision = 10, scale = 2)
    private BigDecimal areaY;

    @Column(name = "area_width", nullable = false, precision = 10, scale = 2)
    private BigDecimal areaWidth;

    @Column(name = "area_height", nullable = false, precision = 10, scale = 2)
    private BigDecimal areaHeight;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private OffsetDateTime creadoEn;
}

