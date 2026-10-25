package mype_backend.controller;

import mype_backend.dto.TiendaProductoPublicoDTO;
import mype_backend.service.TiendaProductoPublicoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/tienda/productos")
public class PublicTiendaProductoController {

    private final TiendaProductoPublicoService tiendaProductoPublicoService;

    public PublicTiendaProductoController(TiendaProductoPublicoService tiendaProductoPublicoService) {
        this.tiendaProductoPublicoService = tiendaProductoPublicoService;
    }

    @GetMapping
    public List<TiendaProductoPublicoDTO> listarPublicados() {
        return tiendaProductoPublicoService.listarPublicados();
    }
}
