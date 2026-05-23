package mype_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "monedas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Moneda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_sunat", nullable = false, unique = true, length = 3)
    private String codigoSunat; // Ej: PEN, USD

    @Column(nullable = false, length = 50)
    private String descripcion; // Ej: Soles, Dólares Américanos

    @Column(nullable = false, length = 5)
    private String simbolo; // Ej: S/, $

    @Column(name = "tipo_cambio", precision = 10, scale = 4)
    private BigDecimal tipoCambio;
}