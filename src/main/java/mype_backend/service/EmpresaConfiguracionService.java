package mype_backend.service;

import mype_backend.dto.EmpresaPerfilDTO;
import mype_backend.entity.EmpresaConfiguracion;
import mype_backend.entity.Usuario;
import mype_backend.repository.EmpresaConfiguracionRepository;
import mype_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class EmpresaConfiguracionService {

    @Autowired
    private EmpresaConfiguracionRepository configRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private StorageService storageService;

    @Autowired
    private EmpresaCompartidaService empresaCompartidaService;

    @Transactional(readOnly = true)
    public EmpresaPerfilDTO obtenerPerfil(Long usuarioId) {
        Long usuarioEmpresaId = empresaCompartidaService.getUsuarioEmpresaId();
        Usuario usuario = usuarioRepository.findById(usuarioEmpresaId)
                .orElseThrow(() -> new RuntimeException("Usuario empresa no encontrado"));

        Optional<EmpresaConfiguracion> configOpt = configRepository.findByUsuarioId(usuarioEmpresaId);

        EmpresaPerfilDTO dto = new EmpresaPerfilDTO();
        dto.setUsuarioId(usuario.getId());
        dto.setRuc(usuario.getRuc());
        dto.setRazonSocial(usuario.getRazonSocial());
        dto.setNombreComercial(usuario.getNombreComercial());
        dto.setDireccionFiscal(usuario.getDireccionFiscal());

        if (configOpt.isPresent()) {
            EmpresaConfiguracion config = configOpt.get();
            dto.setTelefono(config.getTelefono());
            dto.setEmailContacto(config.getEmailContacto());
            dto.setLogoUrl(config.getLogoUrl());
        }

        return dto;
    }

    @Transactional
    public EmpresaPerfilDTO guardarOActualizar(EmpresaPerfilDTO dto) {
        Long usuarioEmpresaId = empresaCompartidaService.getUsuarioEmpresaId();
        Usuario usuario = usuarioRepository.findById(usuarioEmpresaId)
                .orElseThrow(() -> new RuntimeException("Usuario empresa no encontrado"));

        if (dto.getRuc() != null && !dto.getRuc().trim().isEmpty()) {
            Optional<Usuario> rucUser = usuarioRepository.findByRuc(dto.getRuc().trim());
            if (rucUser.isPresent() && !rucUser.get().getId().equals(usuario.getId())) {
                throw new IllegalArgumentException("El RUC " + dto.getRuc() + " ya está asociado a otra cuenta.");
            }
        }

        usuario.setRuc(dto.getRuc() != null ? dto.getRuc().trim() : null);
        usuario.setRazonSocial(dto.getRazonSocial());
        usuario.setNombreComercial(dto.getNombreComercial());
        usuario.setDireccionFiscal(dto.getDireccionFiscal());
        usuarioRepository.save(usuario);

        EmpresaConfiguracion config = configRepository.findByUsuarioId(usuarioEmpresaId)
                .orElse(EmpresaConfiguracion.builder().usuarioId(usuarioEmpresaId).build());

        String oldLogoUrl = config.getLogoUrl();
        String newLogoUrl = dto.getLogoUrl();

        if (oldLogoUrl != null && !oldLogoUrl.isEmpty() && !oldLogoUrl.equals(newLogoUrl)) {
            storageService.deleteFile(oldLogoUrl);
        }

        config.setTelefono(dto.getTelefono());
        config.setEmailContacto(dto.getEmailContacto());
        config.setLogoUrl(newLogoUrl);
        configRepository.save(config);

        dto.setUsuarioId(usuarioEmpresaId);
        return dto;
    }
}

