package mype_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipos_operacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoOperacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_sunat", nullable = false, unique = true, length = 4)
    private String codigoSunat; // Ej: 0101 (Venta interna)

    @Column(nullable = false, length = 255)
    private String descripcion;
}