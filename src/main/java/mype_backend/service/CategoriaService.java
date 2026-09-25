package mype_backend.service;

import mype_backend.entity.Categoria;
import mype_backend.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private EmpresaCompartidaService empresaCompartidaService;

    public List<Categoria> listarPorUsuario(Long usuarioId) {
        return categoriaRepository.findByUsuarioId(empresaCompartidaService.getUsuarioEmpresaId());
    }

    public Categoria guardar(Categoria categoria) {
        categoria.setUsuarioId(empresaCompartidaService.getUsuarioEmpresaId());
        return categoriaRepository.save(categoria);
    }

    public void eliminar(Long id) {
        categoriaRepository.deleteById(id);
    }
}

