package mype_backend.service;

import mype_backend.dto.TiendaProductoRequestDTO;
import mype_backend.dto.TiendaProductoResponseDTO;
import mype_backend.entity.MockupProductoBase;
import mype_backend.entity.ProductoServicio;
import mype_backend.entity.TiendaProducto;
import mype_backend.repository.MockupProductoBaseRepository;
import mype_backend.repository.ProductoServicioRepository;
import mype_backend.repository.TiendaProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class TiendaProductoService {

    private final TiendaProductoRepository tiendaProductoRepository;
    private final ProductoServicioRepository productoServicioRepository;
    private final MockupProductoBaseRepository mockupProductoBaseRepository;
    private final EmpresaCompartidaService empresaCompartidaService;
    private final MockupService mockupService;

    public TiendaProductoService(
            TiendaProductoRepository tiendaProductoRepository,
            ProductoServicioRepository productoServicioRepository,
            MockupProductoBaseRepository mockupProductoBaseRepository,
            EmpresaCompartidaService empresaCompartidaService,
            MockupService mockupService) {
        this.tiendaProductoRepository = tiendaProductoRepository;
        this.productoServicioRepository = productoServicioRepository;
        this.mockupProductoBaseRepository = mockupProductoBaseRepository;
        this.empresaCompartidaService = empresaCompartidaService;
        this.mockupService = mockupService;
    }

    @Transactional(readOnly = true)
    public List<TiendaProductoResponseDTO> listar() {
        return tiendaProductoRepository.findAllByOrderByOrdenAscIdDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TiendaProductoResponseDTO obtener(Long id) {
        return toResponse(obtenerEntidad(id));
    }


    @Transactional
    public TiendaProductoResponseDTO crear(TiendaProductoRequestDTO request) {
        validarRequest(request);
        TiendaProducto tiendaProducto = new TiendaProducto();
        aplicarRequest(tiendaProducto, request);
        return toResponse(tiendaProductoRepository.save(tiendaProducto));
    }

    @Transactional
    public TiendaProductoResponseDTO actualizar(Long id, TiendaProductoRequestDTO request) {
        validarRequest(request);
        TiendaProducto tiendaProducto = obtenerEntidad(id);
        aplicarRequest(tiendaProducto, request);
        tiendaProducto.setActualizadoEn(OffsetDateTime.now());
        return toResponse(tiendaProductoRepository.save(tiendaProducto));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!tiendaProductoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto de tienda no encontrado.");
        }
        tiendaProductoRepository.deleteById(id);
    }

    private void aplicarRequest(TiendaProducto tiendaProducto, TiendaProductoRequestDTO request) {
        ProductoServicio producto = obtenerProductoEmpresa(request.productoServicioId(), "Producto no encontrado.");
        if (!"B".equals(producto.getTipo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El producto principal debe ser un bien.");
        }

        ProductoServicio servicio = null;
        if (request.servicioEstampadoId() != null) {
            servicio = obtenerProductoEmpresa(request.servicioEstampadoId(), "Servicio de estampado no encontrado.");
            if (!"S".equals(servicio.getTipo())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El servicio de estampado debe ser de tipo servicio.");
            }
        }

        MockupProductoBase mockup = mockupProductoBaseRepository.findById(request.mockupProductoBaseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto base de mockup no encontrado."));

        String slug = normalizarSlug(request.slug(), producto.getNombre());
        tiendaProductoRepository.findBySlug(slug)
                .filter(existente -> tiendaProducto.getId() == null || !existente.getId().equals(tiendaProducto.getId()))
                .ifPresent(existente -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un producto de tienda con ese slug.");
                });

        tiendaProducto.setProductoServicio(producto);
        tiendaProducto.setServicioEstampado(servicio);
        tiendaProducto.setMockupProductoBase(mockup);
        tiendaProducto.setSlug(slug);
        tiendaProducto.setDescripcionTienda(normalizarTexto(request.descripcionTienda()));
        tiendaProducto.setPublicado(request.publicado() == null || request.publicado());
        tiendaProducto.setDestacado(Boolean.TRUE.equals(request.destacado()));
        tiendaProducto.setPermitePersonalizacion(request.permitePersonalizacion() == null || request.permitePersonalizacion());
        tiendaProducto.setManejaTallas(Boolean.TRUE.equals(request.manejaTallas()));
        tiendaProducto.setOrden(request.orden() == null ? 0 : request.orden());
    }

    private void validarRequest(TiendaProductoRequestDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos del producto de tienda son obligatorios.");
        }
        if (request.productoServicioId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona el producto del catálogo.");
        }
        if (request.mockupProductoBaseId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona el producto base para el mockup.");
        }
    }

    private ProductoServicio obtenerProductoEmpresa(Long id, String mensaje) {
        ProductoServicio producto = productoServicioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, mensaje));
        Long empresaUsuarioId = empresaCompartidaService.getUsuarioEmpresaId();
        if (!empresaUsuarioId.equals(producto.getUsuarioId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes usar productos de otra empresa.");
        }
        return producto;
    }

    private TiendaProducto obtenerEntidad(Long id) {
        return tiendaProductoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto de tienda no encontrado."));
    }

    private String normalizarTexto(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizarSlug(String slug, String fallback) {
        String base = normalizarTexto(slug);
        if (base == null) {
            base = fallback;
        }
        String normalized = Normalizer.normalize(base, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (normalized.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El slug no es válido.");
        }
        return normalized;
    }

    private TiendaProductoResponseDTO toResponse(TiendaProducto tiendaProducto) {
        return new TiendaProductoResponseDTO(
                tiendaProducto.getId(),
                toProductoResumen(tiendaProducto.getProductoServicio()),
                toProductoResumen(tiendaProducto.getServicioEstampado()),
                mockupService.toProductoBaseResponse(tiendaProducto.getMockupProductoBase()),
                tiendaProducto.getSlug(),
                tiendaProducto.getDescripcionTienda(),
                tiendaProducto.getPublicado(),
                tiendaProducto.getDestacado(),
                tiendaProducto.getPermitePersonalizacion(),
                Boolean.TRUE.equals(tiendaProducto.getManejaTallas()),
                tiendaProducto.getOrden(),
                tiendaProducto.getCreadoEn(),
                tiendaProducto.getActualizadoEn());
    }

    private TiendaProductoResponseDTO.ProductoResumenDTO toProductoResumen(ProductoServicio producto) {
        if (producto == null) {
            return null;
        }
        return new TiendaProductoResponseDTO.ProductoResumenDTO(
                producto.getId(),
                producto.getCodigo(),
                producto.getNombre(),
                producto.getTipo(),
                producto.getUnidadMedida(),
                producto.getPrecioVenta(),
                producto.getPrecioCompra(),
                producto.getStockActual());
    }
}

