package mype_backend.controller;

import mype_backend.dto.TiendaEnvioPublicoDTO;
import mype_backend.service.TiendaEnvioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/tienda/envios")
public class PublicTiendaEnvioController {
    private final TiendaEnvioService envioService;

    public PublicTiendaEnvioController(TiendaEnvioService envioService) {
        this.envioService = envioService;
    }

    @GetMapping
    public List<TiendaEnvioPublicoDTO> listar() {
        return envioService.listarZonas();
    }
}
