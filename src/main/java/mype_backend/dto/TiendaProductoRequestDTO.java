package mype_backend.dto;

public record TiendaProductoRequestDTO(
        Long productoServicioId,
        Long servicioEstampadoId,
        Long mockupProductoBaseId,
        String slug,
        String descripcionTienda,
        Boolean publicado,
        Boolean destacado,
        Boolean permitePersonalizacion,
        Boolean manejaTallas,
        Integer orden) {
}
