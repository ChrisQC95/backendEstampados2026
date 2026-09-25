package mype_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipos_comprobante")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoComprobante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_sunat", nullable = true, unique = true, length = 2)
    private String codigoSunat; // Ej: 01 (Factura), 03 (Boleta). null para documentos no oficiales (Nota de Venta)

    @Column(nullable = false, length = 100)
    private String descripcion;

    @Column(name = "requiere_cliente_ruc")
    private Boolean requiereClienteRuc = false;
}
