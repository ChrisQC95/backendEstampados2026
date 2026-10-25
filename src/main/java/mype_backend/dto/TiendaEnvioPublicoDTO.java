package mype_backend.dto;

import java.math.BigDecimal;

public record TiendaEnvioPublicoDTO(String id, String nombre, String codigoServicio, BigDecimal precio) {
}
