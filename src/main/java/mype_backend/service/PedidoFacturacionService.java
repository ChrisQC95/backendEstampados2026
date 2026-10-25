package mype_backend.service;

import mype_backend.dto.PedidoFacturacionDTO;
import mype_backend.entity.PedidoTienda;
import mype_backend.entity.PedidoTiendaItem;
import mype_backend.entity.ProductoServicio;
import mype_backend.entity.TiendaProducto;
import mype_backend.entity.Usuario;
import mype_backend.repository.PedidoTiendaRepository;
import mype_backend.repository.ProductoServicioRepository;
import mype_backend.repository.TiendaProductoRepository;
import mype_backend.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
public class PedidoFacturacionService {

    private final PedidoTiendaRepository pedidoRepository;
    private final TiendaProductoRepository tiendaProductoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoServicioRepository productoServicioRepository;
    private final TiendaEnvioService tiendaEnvioService;

    public PedidoFacturacionService(
            PedidoTiendaRepository pedidoRepository,
            TiendaProductoRepository tiendaProductoRepository,
            UsuarioRepository usuarioRepository,
            ProductoServicioRepository productoServicioRepository,
            TiendaEnvioService tiendaEnvioService) {
        this.pedidoRepository = pedidoRepository;
        this.tiendaProductoRepository = tiendaProductoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoServicioRepository = productoServicioRepository;
        this.tiendaEnvioService = tiendaEnvioService;
    }

    @Transactional(readOnly = true)
    public List<PedidoFacturacionDTO> listarPendientes(Jwt jwt) {
        validarUsuarioSistema(jwt);
        return pedidoRepository.findPedidosPendientesFacturacion().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public List<PedidoFacturacionDTO> obtenerSeleccionados(Jwt jwt, List<Long> ids) {
        validarUsuarioSistema(jwt);
        if (ids == null || ids.isEmpty() || ids.size() > 50 || ids.stream().anyMatch(Objects::isNull)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona entre 1 y 50 pedidos.");
        }
        List<Long> distintos = ids.stream().distinct().toList();
        List<PedidoTienda> pedidos = pedidoRepository.findPedidosPendientesFacturacionPorIds(distintos);
        if (pedidos.size() != distintos.size()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Uno o más pedidos ya no están pagados o disponibles para facturar.");
        }
        return pedidos.stream().map(this::toDto).toList();
    }

    private void validarUsuarioSistema(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token Firebase inválido.");
        }
        Usuario usuario = usuarioRepository.findByFirebaseUid(jwt.getSubject())
                .filter(user -> Boolean.TRUE.equals(user.getActivo()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no autorizado."));
        Integer rolId = usuario.getRol() == null ? null : usuario.getRol().getId();
        if (rolId == null || rolId < 1 || rolId > 6) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "La bandeja de facturación está disponible para usuarios del sistema.");
        }
    }

    private PedidoFacturacionDTO toDto(PedidoTienda pedido) {
        List<PedidoFacturacionDTO.Item> items = pedido.getItems().stream()
                .map(this::toItemDto)
                .toList();
        String paymentMethod = pedido.getPago() == null ? null : pedido.getPago().getProveedorPago();
        if (paymentMethod != null && paymentMethod.startsWith("SIMULADO-")) {
            paymentMethod = paymentMethod.substring("SIMULADO-".length());
        }
        Long shippingProductId = pedido.getProductoServicioEnvioId();
        String shippingProductName = null;
        if (shippingProductId == null) {
            String shippingCode = tiendaEnvioService.codigoDesdeObservaciones(pedido.getObservaciones());
            if (shippingCode != null) {
                try { shippingProductId = tiendaEnvioService.resolverPorCodigo(shippingCode).productoId(); }
                catch (RuntimeException ignored) { /* The frontend reports a missing configured shipping service. */ }
            }
        }
        if (shippingProductId != null) {
            ProductoServicio shippingProduct = productoServicioRepository.findById(shippingProductId).orElse(null);
            if (shippingProduct != null) shippingProductName = shippingProduct.getNombre();
        }
        return new PedidoFacturacionDTO(
                pedido.getId(), pedido.getCodigo(), pedido.getEstado(),
                pedido.getPago() == null ? null : pedido.getPago().getEstado(), paymentMethod,
                pedido.getFechaCreacion(), pedido.getSubtotal(), pedido.getCostoEnvio(), shippingProductId, shippingProductName, pedido.getTotal(),
                pedido.getTipoComprobanteFacturacion(), pedido.getTipoDocumentoFacturacion(),
                pedido.getNumeroDocumentoFacturacion(), pedido.getNombreFacturacion(),
                pedido.getDireccionFiscalFacturacion(), pedido.getUbigeoFiscalFacturacion(),
                pedido.getEmailFacturacion(), pedido.getDireccionEntrega(), pedido.getUbigeoEntrega(), items);
    }

    private PedidoFacturacionDTO.Item toItemDto(PedidoTiendaItem item) {
        Long productId = item.getProductoServicioId();
        Long serviceId = item.getServicioEstampadoId();
        String productName = item.getNombreProducto();
        String serviceName = item.getNombreServicioEstampado();

        // Los pedidos anteriores a este flujo guardan el ID del anuncio, pero no los
        // IDs internos de producto/servicio. Se recuperan del anuncio vigente cuando es posible.
        if (productId == null || (serviceId == null && serviceName != null)) {
            TiendaProducto listing = tiendaProductoRepository.findById(item.getTiendaProductoId()).orElse(null);
            if (listing != null) {
                if (productId == null && listing.getProductoServicio() != null) {
                    productId = listing.getProductoServicio().getId();
                    productName = listing.getProductoServicio().getNombre();
                }
                if (serviceId == null && listing.getServicioEstampado() != null) {
                    serviceId = listing.getServicioEstampado().getId();
                    serviceName = listing.getServicioEstampado().getNombre();
                }
            }
        }

        return new PedidoFacturacionDTO.Item(
                item.getTiendaProductoId(), productId, serviceId, productName, serviceName, item.getTalla(),
                item.getCantidad(), item.getPrecioProducto(), item.getPrecioEstampado());
    }
}
