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

    @Transactional(readOnly = true)
    public EmpresaPerfilDTO obtenerPerfil(Long usuarioId) {
        // 1. Buscar el usuario (Obligatorio)
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Buscar la configuración extra (Opcional)
        Optional<EmpresaConfiguracion> configOpt = configRepository.findByUsuarioId(usuarioId);

        // 3. Ensamblar el DTO para el frontend
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
        // 1. Actualizar campos en la tabla 'usuarios'
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setRuc(dto.getRuc());
        usuario.setRazonSocial(dto.getRazonSocial());
        usuario.setNombreComercial(dto.getNombreComercial());
        usuario.setDireccionFiscal(dto.getDireccionFiscal());
        usuarioRepository.save(usuario);

        // 2. Upsert (Crear o Actualizar) en la tabla 'empresa_configuracion'
        EmpresaConfiguracion config = configRepository.findByUsuarioId(dto.getUsuarioId())
                .orElse(EmpresaConfiguracion.builder().usuarioId(dto.getUsuarioId()).build());

        // Verificar si la URL del logo cambió para eliminar la antigua del Storage
        String oldLogoUrl = config.getLogoUrl();
        String newLogoUrl = dto.getLogoUrl();

        if (oldLogoUrl != null && !oldLogoUrl.isEmpty() && !oldLogoUrl.equals(newLogoUrl)) {
            storageService.deleteFile(oldLogoUrl);
        }

        config.setTelefono(dto.getTelefono());
        config.setEmailContacto(dto.getEmailContacto());
        config.setLogoUrl(newLogoUrl);
        configRepository.save(config);

        return dto; // Retornamos el DTO actualizado
    }
}