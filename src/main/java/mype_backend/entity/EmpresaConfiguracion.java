package mype_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "empresa_configuracion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaConfiguracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false, unique = true)
    private Long usuarioId;

    @Column(length = 50)
    private String telefono;

    @Column(name = "email_contacto", length = 150)
    private String emailContacto;

    @Column(name = "logo_url", columnDefinition = "TEXT")
    private String logoUrl;
}