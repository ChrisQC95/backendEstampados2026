package mype_backend.controller;

import mype_backend.dto.MockupProductoBaseRequestDTO;
import mype_backend.dto.MockupProductoBaseResponseDTO;
import mype_backend.dto.MockupProyectoRequestDTO;
import mype_backend.dto.MockupProyectoResponseDTO;
import mype_backend.dto.MockupResultadoResponseDTO;
import mype_backend.service.MockupService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/mockups")
public class MockupController {

    private final MockupService mockupService;

    public MockupController(MockupService mockupService) {
        this.mockupService = mockupService;
    }

    @GetMapping("/productos-base")
    public List<MockupProductoBaseResponseDTO> listarProductosBaseActivos(@AuthenticationPrincipal Jwt jwt) {
        return mockupService.listarProductosBaseActivos(jwt);
    }

    @GetMapping("/productos-base/todos")
    public List<MockupProductoBaseResponseDTO> listarProductosBase(@AuthenticationPrincipal Jwt jwt) {
        return mockupService.listarProductosBase(jwt);
    }

    @PostMapping("/productos-base")
    public MockupProductoBaseResponseDTO crearProductoBase(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("nombre") String nombre,
            @RequestParam("tipoProducto") String tipoProducto,
            @RequestParam("coloresDisponibles") List<String> coloresDisponibles,
            @RequestParam("areaX") BigDecimal areaX,
            @RequestParam("areaY") BigDecimal areaY,
            @RequestParam("areaWidth") BigDecimal areaWidth,
            @RequestParam("areaHeight") BigDecimal areaHeight,
            @RequestParam(value = "activo", required = false) Boolean activo,
            @RequestParam("imagenBase") MultipartFile imagenBase) {
        MockupProductoBaseRequestDTO request = new MockupProductoBaseRequestDTO(
                nombre,
                tipoProducto,
                coloresDisponibles,
                areaX,
                areaY,
                areaWidth,
                areaHeight,
                activo);
        return mockupService.crearProductoBase(jwt, request, imagenBase);
    }

    @PutMapping("/productos-base/{id}")
    public MockupProductoBaseResponseDTO actualizarProductoBase(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestBody MockupProductoBaseRequestDTO request) {
        return mockupService.actualizarProductoBase(jwt, id, request);
    }

    @PostMapping("/productos-base/{id}/imagen")
    public MockupProductoBaseResponseDTO actualizarImagenBase(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestParam("imagenBase") MultipartFile imagenBase) {
        return mockupService.actualizarImagenBase(jwt, id, imagenBase);
    }

    @PutMapping("/productos-base/{id}/estado")
    public MockupProductoBaseResponseDTO cambiarEstadoProductoBase(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestParam("activo") Boolean activo) {
        return mockupService.cambiarEstadoProductoBase(jwt, id, activo);
    }

    @GetMapping("/proyectos")
    public List<MockupProyectoResponseDTO> listarProyectos(@AuthenticationPrincipal Jwt jwt) {
        return mockupService.listarProyectos(jwt);
    }

    @PostMapping("/proyectos")
    public MockupProyectoResponseDTO crearProyecto(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("nombre") String nombre,
            @RequestParam("productoBaseId") Long productoBaseId,
            @RequestParam(value = "colorSeleccionado", required = false) String colorSeleccionado,
            @RequestParam("imagenCliente") MultipartFile imagenCliente) {
        MockupProyectoRequestDTO request = new MockupProyectoRequestDTO(nombre, productoBaseId, colorSeleccionado);
        return mockupService.crearProyecto(jwt, request, imagenCliente);
    }

    @PostMapping("/proyectos/{id}/resultado")
    public MockupResultadoResponseDTO guardarResultado(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestParam("resultado") MultipartFile resultado) {
        return mockupService.guardarResultado(jwt, id, resultado);
    }
}


