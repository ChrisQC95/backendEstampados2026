package mype_backend.dto;

public record UsuarioCreateRequest(
        String email,
        String nombre,
        Integer rolId) {
}
