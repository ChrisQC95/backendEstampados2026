package mype_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "productos_servicios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "categoria_id")
    private Long categoriaId;

    @Column(length = 50)
    private String codigo;

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(nullable = false, length = 1)
    private String tipo; // 'B' para Bien, 'S' para Servicio

    @Column(name = "unidad_medida", nullable = false, length = 3)
    private String unidadMedida; // Ej: 'NIU' (Bienes), 'ZZ' (Servicios)

    @Column(name = "precio_venta", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "precio_compra", precision = 10, scale = 2)
    private BigDecimal precioCompra;

    @Column(name = "afecto_igv")
    private Boolean afectoIgv;

    @Column(name = "stock_actual", precision = 10, scale = 2)
    private BigDecimal stockActual;

    @Column(name = "stock_minimo", precision = 10, scale = 2)
    private BigDecimal stockMinimo;
}
