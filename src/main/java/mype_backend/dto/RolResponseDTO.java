package mype_backend.dto;

public record RolResponseDTO(
        Integer id,
        String codigo,
        String nombre,
        String descripcion,
        Boolean activo) {
}
