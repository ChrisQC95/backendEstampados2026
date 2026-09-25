package mype_backend.controller;

import mype_backend.entity.Ubigeo;
import mype_backend.repository.UbigeoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ubigeos")
public class UbigeoController {

    @Autowired
    private UbigeoRepository ubigeoRepository;

    /**
     * GET /api/ubigeos/departamentos
     * Retorna la lista de departamentos únicos ordenados alfabéticamente.
     */
    @GetMapping("/departamentos")
    public List<String> listarDepartamentos() {
        return ubigeoRepository.listarDepartamentos();
    }

    /**
     * GET /api/ubigeos/provincias?departamento=LIMA
     * Retorna las provincias del departamento indicado.
     */
    @GetMapping("/provincias")
    public List<String> listarProvincias(
            @RequestParam String departamento) {
        return ubigeoRepository.listarProvincias(departamento);
    }

    /**
     * GET /api/ubigeos/distritos?departamento=LIMA&provincia=LIMA
     * Retorna los objetos Ubigeo completos (ubigeo, departamento, provincia,
     * distrito).
     * El frontend extrae el campo `ubigeo` (código 6 dígitos) para enviarlo al
     * backend.
     */
    @GetMapping("/distritos")
    public List<Ubigeo> listarDistritos(
            @RequestParam String departamento,
            @RequestParam String provincia) {
        return ubigeoRepository.listarDistritos(departamento, provincia);
    }
}

