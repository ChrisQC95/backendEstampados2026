package mype_backend.service;

import mype_backend.dto.TiendaEnvioPublicoDTO;
import mype_backend.entity.ProductoServicio;
import mype_backend.repository.ProductoServicioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class TiendaEnvioService {
    private record Zona(String id, String nombre, String codigoServicio) { }

    public record ServicioEnvio(String id, String nombre, String codigo, Long productoId, BigDecimal precio,
                                Boolean afectoIgv) { }

    private static final List<Zona> ZONAS = List.of(
            new Zona("santa-anita", "Santa Anita", "SA-C8"),
            new Zona("lima-centro", "Lima Centro", "LC-C12"),
            new Zona("lima-norte", "Lima Norte", "LN-C16"),
            new Zona("lima-sur", "Lima Sur", "LS-C18")
    );

    private final ProductoServicioRepository productos;
    private final EmpresaCompartidaService empresa;

    public TiendaEnvioService(ProductoServicioRepository productos, EmpresaCompartidaService empresa) {
        this.productos = productos;
        this.empresa = empresa;
    }

    @Transactional(readOnly = true)
    public List<TiendaEnvioPublicoDTO> listarZonas() {
        Long usuarioId = empresa.getUsuarioEmpresaId();
        Map<String, ProductoServicio> porCodigo = productos.findByUsuarioIdAndCodigoIn(
                        usuarioId, ZONAS.stream().map(Zona::codigoServicio).toList())
                .stream().collect(java.util.stream.Collectors.toMap(ProductoServicio::getCodigo, p -> p, (a, b) -> a));
        return ZONAS.stream().filter(z -> porCodigo.containsKey(z.codigoServicio())
                        && "S".equals(porCodigo.get(z.codigoServicio()).getTipo()))
                .map(z -> new TiendaEnvioPublicoDTO(z.id(), z.nombre(), z.codigoServicio(),
                        porCodigo.get(z.codigoServicio()).getPrecioVenta()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ServicioEnvio resolver(String zonaId) {
        Zona zona = ZONAS.stream().filter(item -> item.id().equals(zonaId)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Zona de entrega no reconocida."));
        ProductoServicio servicio = productos.findByUsuarioIdAndCodigoIn(empresa.getUsuarioEmpresaId(), List.of(zona.codigoServicio()))
                .stream().filter(p -> "S".equals(p.getTipo())).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                        "Configura el servicio de envío " + zona.codigoServicio() + " en el catálogo."));
        return new ServicioEnvio(zona.id(), zona.nombre(), zona.codigoServicio(), servicio.getId(),
                servicio.getPrecioVenta(), Boolean.TRUE.equals(servicio.getAfectoIgv()));
    }

    @Transactional(readOnly = true)
    public ServicioEnvio resolverPorCodigo(String codigo) {
        Zona zona = ZONAS.stream().filter(item -> item.codigoServicio().equals(codigo)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Código de servicio de envío desconocido."));
        return resolver(zona.id());
    }

    public String codigoParaZona(String zonaId) {
        return ZONAS.stream().filter(item -> item.id().equals(zonaId)).map(Zona::codigoServicio).findFirst().orElse(null);
    }

    public String codigoDesdeObservaciones(String observaciones) {
        if (observaciones == null) return null;
        String prefix = "Zona de entrega: ";
        int start = observaciones.indexOf(prefix);
        if (start < 0) return null;
        String zoneId = observaciones.substring(start + prefix.length()).trim();
        return codigoParaZona(zoneId);
    }
}
