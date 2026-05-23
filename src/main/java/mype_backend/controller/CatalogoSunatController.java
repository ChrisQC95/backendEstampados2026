package mype_backend.controller;

import mype_backend.entity.*;
import mype_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/catalogos-sunat")
@CrossOrigin(origins = "*")
public class CatalogoSunatController {

    @Autowired
    private MonedaRepository monedaRepository;

    @Autowired
    private TipoComprobanteRepository tipoComprobanteRepository;

    @Autowired
    private TipoOperacionRepository tipoOperacionRepository;

    @Autowired
    private UbigeoRepository ubigeoRepository;

    @GetMapping("/monedas")
    public List<Moneda> listarMonedas() {
        return monedaRepository.findAll();
    }

    @GetMapping("/tipos-comprobante")
    public List<TipoComprobante> listarTiposComprobante() {
        return tipoComprobanteRepository.findAll();
    }

    @GetMapping("/tipos-operacion")
    public List<TipoOperacion> listarTiposOperacion() {
        return tipoOperacionRepository.findAll();
    }

    @GetMapping("/departamentos")
    public List<String> listarDepartamentos() {
        return ubigeoRepository.listarDepartamentos();
    }

    @GetMapping("/provincias")
    public List<String> listarProvincias(
            @RequestParam String departamento) {
        return ubigeoRepository.listarProvincias(departamento);
    }

    @GetMapping("/distritos")
    public List<Ubigeo> listarDistritos(
            @RequestParam String departamento,
            @RequestParam String provincia) {
        return ubigeoRepository.listarDistritos(departamento, provincia);
    }

    @GetMapping("/ubigeos/{codigo}")
    public org.springframework.http.ResponseEntity<Ubigeo> obtenerUbigeoPorCodigo(
            @PathVariable("codigo") String codigo) {
        return ubigeoRepository.findById(codigo)
                .map(org.springframework.http.ResponseEntity::ok)
                .orElse(org.springframework.http.ResponseEntity.notFound().build());
    }
}