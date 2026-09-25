package mype_backend.dto;

public record UsuarioCreateResponse(
        UsuarioResponseDTO usuario,
        String passwordResetLink) {
}
