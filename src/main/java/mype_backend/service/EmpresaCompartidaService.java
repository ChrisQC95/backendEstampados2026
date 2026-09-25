package mype_backend.service;

import mype_backend.entity.Usuario;
import mype_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmpresaCompartidaService {

    private final UsuarioRepository usuarioRepository;
    private final Long usuarioEmpresaConfigurado;

    public EmpresaCompartidaService(
            UsuarioRepository usuarioRepository,
            @Value("${app.single-company.owner-user-id:0}") Long usuarioEmpresaConfigurado) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioEmpresaConfigurado = usuarioEmpresaConfigurado;
    }

    public Long getUsuarioEmpresaId() {
        if (usuarioEmpresaConfigurado != null && usuarioEmpresaConfigurado > 0) {
            return usuarioEmpresaConfigurado;
        }

        return usuarioRepository.findFirstByOrderByIdAsc()
                .map(Usuario::getId)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe un usuario empresa. Inicia sesión al menos una vez para crear el usuario base."));
    }
}

