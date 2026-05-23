package mype_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuarios")
@Data // Lombok: Genera Getters, Setters, toString automáticamente
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "firebase_uid", nullable = false, unique = true, length = 128)
    private String firebaseUid;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 100)
    private String nombre;

    @Column(unique = true, length = 11)
    private String ruc;

    @Column(name = "razon_social", length = 255)
    private String razonSocial;

    @Column(name = "nombre_comercial", length = 255)
    private String nombreComercial;

    @Column(name = "direccion_fiscal", length = 255)
    private String direccionFiscal;
}