package mype_backend.service;

import mype_backend.entity.ProductoServicio;
import mype_backend.repository.ProductoServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductoServicioService {

    @Autowired
    private ProductoServicioRepository productoServicioRepository;

    @Autowired
    private EmpresaCompartidaService empresaCompartidaService;

    public List<ProductoServicio> listarPorUsuario(Long usuarioId) {
        return productoServicioRepository.findByUsuarioId(empresaCompartidaService.getUsuarioEmpresaId());
    }

    public ProductoServicio guardar(ProductoServicio producto) {
        producto.setUsuarioId(empresaCompartidaService.getUsuarioEmpresaId());
        return productoServicioRepository.save(producto);
    }

    public void eliminar(Long id) {
        productoServicioRepository.deleteById(id);
    }
}

