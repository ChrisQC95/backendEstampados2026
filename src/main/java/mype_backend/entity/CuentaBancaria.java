package mype_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cuentas_bancarias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaBancaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false, length = 50)
    private String banco; // Ej: BCP, BBVA, Interbank, Yape

    @Column(nullable = false, length = 10)
    private String moneda; // SOLES o DOLARES

    @Column(name = "numero_cuenta", length = 50)
    private String numeroCuenta;

    @Column(length = 50)
    private String cci;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}