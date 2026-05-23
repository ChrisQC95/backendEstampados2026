package mype_backend.dto;

import lombok.Data;

@Data
public class SocioNegocioDTO {

    private Long usuarioId;

    private String tipoSocio;

    private String tipoDocumento;

    private String numeroDocumento;

    private String tipoPersona;

    private String nombreRazonSocial;

    private String direccionFiscal;

    private String ubigeo;

    private String telefonoMovil;

    private String telefonoFijo;

    private String emailFacturacion;
}