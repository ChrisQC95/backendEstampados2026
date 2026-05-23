package mype_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "conductores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conductor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "tipo_documento", nullable = false, length = 2)
    private String tipoDocumento;

    @Column(name = "numero_documento", nullable = false, length = 15)
    private String numeroDocumento;

    @Column(name = "nombre_completo", nullable = false, length = 200)
    private String nombreCompleto;

    @Column(name = "licencia_conducir", nullable = false, length = 20)
    private String licenciaConducir;

    @Column(nullable = false)
    private Boolean activo = true;
}