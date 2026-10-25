package mype_backend.service;

import mype_backend.dto.RolResponseDTO;
import mype_backend.entity.Rol;
import mype_backend.repository.RolRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RolService {

    private static final String ROL_CLIENTE_TIENDA = "CLIENTE_TIENDA";

    private final RolRepository rolRepository;

    public RolService(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    public List<RolResponseDTO> listarActivos() {
        return rolesActivos().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<RolResponseDTO> listarRolesSistemaPublicos() {
        return rolesActivos().stream()
                .filter(rol -> !ROL_CLIENTE_TIENDA.equals(rol.getCodigo()))
                .map(this::toResponse)
                .toList();
    }

    public List<RolResponseDTO> listarRolesTiendaPublicos() {
        return rolesActivos().stream()
                .filter(rol -> ROL_CLIENTE_TIENDA.equals(rol.getCodigo()))
                .map(this::toResponse)
                .toList();
    }

    private List<Rol> rolesActivos() {
        return rolRepository.findByActivoTrueOrderByNombreAsc();
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
