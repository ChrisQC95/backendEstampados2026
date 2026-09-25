package mype_backend.service;

import mype_backend.dto.RolResponseDTO;
import mype_backend.entity.Rol;
import mype_backend.repository.RolRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RolService {

    private final RolRepository rolRepository;

    public RolService(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    public List<RolResponseDTO> listarActivos() {
        return rolRepository.findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    RolResponseDTO toResponse(Rol rol) {
        return new RolResponseDTO(
                rol.getId(),
                rol.getCodigo(),
                rol.getNombre(),
                rol.getDescripcion(),
                rol.getActivo());
    }
}
