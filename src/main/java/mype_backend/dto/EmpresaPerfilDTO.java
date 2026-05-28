package mype_backend.dto;

import lombok.Data;

@Data
public class EmpresaPerfilDTO {
    private Long usuarioId;

    // Datos que pertenecen a la tabla 'usuarios'
    private String ruc;
    private String razonSocial;
    private String nombreComercial;
    private String direccionFiscal;

    // Datos que pertenecen a la tabla 'empresa_configuracion'
    private String telefono;
    private String emailContacto;
    private String logoUrl;
}