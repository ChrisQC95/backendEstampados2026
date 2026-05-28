package mype_backend.controller;

import mype_backend.dto.SocioNegocioDTO;
import mype_backend.entity.SocioNegocio;
import mype_backend.service.SocioNegocioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/socios")
// @CrossOrigin(origins = "*")
public class SocioNegocioController {

    @Autowired
    private SocioNegocioService socioNegocioService;

    // Obtener todos los socios (Pantalla de Catálogo de Socios)
    @GetMapping("/usuario/{usuarioId}")
    public List<SocioNegocio> listarTodos(@PathVariable Long usuarioId) {
        return socioNegocioService.listarPorUsuario(usuarioId);
    }

    // Obtener solo clientes (Para el combo box en "Nueva Venta")
    @GetMapping("/usuario/{usuarioId}/clientes")
    public List<SocioNegocio> listarClientes(@PathVariable Long usuarioId) {
        return socioNegocioService.listarClientes(usuarioId);
    }

    @PostMapping
    public SocioNegocio crear(@RequestBody SocioNegocioDTO dto) {
        SocioNegocio socio = SocioNegocio.builder()
                .id(dto.getId())
                .usuarioId(dto.getUsuarioId())
                .tipoSocio(dto.getTipoSocio())
                .tipoDocumento(dto.getTipoDocumento())
                .numeroDocumento(dto.getNumeroDocumento())
                .tipoPersona(dto.getTipoPersona())
                .nombreRazonSocial(dto.getNombreRazonSocial())
                .direccionFiscal(dto.getDireccionFiscal())
                .telefonoMovil(dto.getTelefonoMovil())
                .telefonoFijo(dto.getTelefonoFijo())
                .emailFacturacion(dto.getEmailFacturacion())
                .build();

        return socioNegocioService.guardar(
                socio,
                dto.getUbigeo());
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        socioNegocioService.eliminar(id);
    }
}