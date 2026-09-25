package mype_backend.dto;

public record UsuarioResponseDTO(
        Long id,
        String firebaseUid,
        String email,
        String nombre,
        Boolean activo,
        RolResponseDTO rol) {
}
