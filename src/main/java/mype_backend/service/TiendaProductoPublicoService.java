package mype_backend.service;

import mype_backend.dto.TiendaProductoPublicoDTO;
import mype_backend.entity.MockupProductoBase;
import mype_backend.entity.ProductoServicio;
import mype_backend.entity.TiendaProducto;
import mype_backend.repository.TiendaProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
public class TiendaProductoPublicoService {

    private final TiendaProductoRepository tiendaProductoRepository;

    public TiendaProductoPublicoService(TiendaProductoRepository tiendaProductoRepository) {
        this.tiendaProductoRepository = tiendaProductoRepository;
    }

    @Transactional(readOnly = true)
    public List<TiendaProductoPublicoDTO> listarPublicados() {
        return tiendaProductoRepository.findPublicadosConDetalle()
                .stream()
                .map(this::toPublico)
                .toList();
    }

    private TiendaProductoPublicoDTO toPublico(TiendaProducto item) {
        ProductoServicio producto = item.getProductoServicio();
        ProductoServicio servicio = item.getServicioEstampado();
        MockupProductoBase mockup = item.getMockupProductoBase();

        BigDecimal precioProducto = producto == null || producto.getPrecioVenta() == null
                ? BigDecimal.ZERO
                : producto.getPrecioVenta();
        BigDecimal precioServicio = servicio == null || servicio.getPrecioVenta() == null
                ? BigDecimal.ZERO
                : servicio.getPrecioVenta();

        return new TiendaProductoPublicoDTO(
                item.getId(),
                item.getSlug(),
                producto == null ? "Producto" : producto.getNombre(),
                mockup == null ? "Varios" : mockup.getTipoProducto(),
                item.getDescripcionTienda(),
                precioProducto,
                precioServicio,
                precioProducto.add(precioServicio),
                Boolean.TRUE.equals(item.getDestacado()) ? "Destacado" : "Personalizable",
                Boolean.TRUE.equals(item.getDestacado()),
                Boolean.TRUE.equals(item.getPermitePersonalizacion()),
                Boolean.TRUE.equals(item.getManejaTallas()),
                mockup == null ? null : mockup.getImagenBasePath(),
                deserializarColores(mockup),
                mockup == null || mockup.getAreaX() == null ? BigDecimal.ZERO : mockup.getAreaX(),
                mockup == null || mockup.getAreaY() == null ? BigDecimal.ZERO : mockup.getAreaY(),
                mockup == null || mockup.getAreaWidth() == null ? BigDecimal.ZERO : mockup.getAreaWidth(),
                mockup == null || mockup.getAreaHeight() == null ? BigDecimal.ZERO : mockup.getAreaHeight());
    }

    private List<String> deserializarColores(MockupProductoBase mockup) {
        if (mockup == null) {
            return List.of();
        }
        if (mockup.getColoresDisponibles() != null && !mockup.getColoresDisponibles().isBlank()) {
            return Arrays.stream(mockup.getColoresDisponibles().split(","))
                    .map(String::trim)
                    .filter(color -> !color.isBlank())
                    .distinct()
                    .toList();
        }
        if (mockup.getColor() != null && !mockup.getColor().isBlank()) {
            return List.of(mockup.getColor().trim());
        }
        return List.of();
    }
}
