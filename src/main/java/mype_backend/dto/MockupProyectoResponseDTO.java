package mype_backend.dto;

import java.time.OffsetDateTime;

public record MockupProyectoResponseDTO(
        Long id,
        String nombre,
        MockupProductoBaseResponseDTO productoBase,
        String imagenClientePath,
        String resultadoPath,
        String colorSeleccionado,
        Long creadoPorUsuarioId,
        String creadoPorUsuarioNombre,
        OffsetDateTime creadoEn) {
}
