package mype_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "socios_negocio")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocioNegocio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "tipo_socio", nullable = false, length = 1)
    private String tipoSocio; // 'C' = Cliente, 'P' = Proveedor, 'A' = Ambos

    @Column(name = "tipo_documento", nullable = false, length = 2)
    private String tipoDocumento; // Ej: '1' para DNI, '6' para RUC

    @Column(name = "numero_documento", nullable = false, length = 20)
    private String numeroDocumento;

    @Column(name = "tipo_persona", length = 10)
    private String tipoPersona;

    @Column(name = "nombre_razon_social", nullable = false, length = 255)
    private String nombreRazonSocial;

    @Column(name = "direccion_fiscal", length = 255)
    private String direccionFiscal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubigeo")
    private Ubigeo ubigeo;

    @Column(name = "telefono_movil", length = 20)
    private String telefonoMovil;

    @Column(name = "telefono_fijo", length = 20)
    private String telefonoFijo;

    @Column(name = "email_facturacion", length = 150)
    private String emailFacturacion;
}