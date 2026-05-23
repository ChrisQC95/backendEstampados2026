package mype_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "series")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Serie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "tipo_comprobante_id", nullable = false)
    private Long tipoComprobanteId;

    @Column(nullable = false, length = 4)
    private String serie; // Ej: 'F001' para Facturas, 'B001' para Boletas

    @Column(name = "correlativo_actual")
    @Builder.Default
    private Integer correlativoActual = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}