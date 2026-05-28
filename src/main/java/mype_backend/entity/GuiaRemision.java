package mype_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "guias_remision")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuiaRemision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "venta_id", nullable = false, unique = true)
    private Long ventaId;

    @Column(name = "motivo_traslado_codigo", nullable = false, length = 2)
    private String motivoTrasladoCodigo;

    @Column(name = "conductor_id", nullable = false)
    private Long conductorId;

    @Column(name = "vehiculo_id", nullable = false)
    private Long vehiculoId;

    @Column(name = "peso_bruto_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal pesoBrutoTotal;

    @Column(name = "ubigeo_partida", nullable = false, length = 6)
    private String ubigeoPartida;

    @Column(name = "direccion_partida", nullable = false, length = 255)
    private String direccionPartida;

    @Column(name = "ubigeo_llegada", nullable = false, length = 6)
    private String ubigeoLlegada;

    @Column(name = "direccion_llegada", nullable = false, length = 255)
    private String direccionLlegada;
}