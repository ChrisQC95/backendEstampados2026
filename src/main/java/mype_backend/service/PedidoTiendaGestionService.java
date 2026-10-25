package mype_backend.service;

import mype_backend.dto.PedidoTiendaResumenDTO;
import mype_backend.entity.PagoTienda;
import mype_backend.entity.PedidoTienda;
import mype_backend.entity.PedidoTiendaItem;
import mype_backend.entity.ProductoServicio;
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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PedidoTiendaGestionService {
    private final PedidoTiendaRepository pedidos;
    private final ProductoServicioRepository productos;
    private final UsuarioRepository usuarios;
    private final TiendaProductoRepository tiendaProductos;

    public PedidoTiendaGestionService(PedidoTiendaRepository pedidos,
            ProductoServicioRepository productos, UsuarioRepository usuarios,
            TiendaProductoRepository tiendaProductos) {
        this.pedidos = pedidos;
        this.productos = productos;
        this.usuarios = usuarios;
        this.tiendaProductos = tiendaProductos;
    }

    @Transactional(readOnly = true)
    public List<PedidoTiendaResumenDTO> listarMisPedidos(Jwt jwt) {
        Usuario usuario = usuarioAutenticado(jwt);
        if (usuario.getRol() == null || !"CLIENTE_TIENDA".equals(usuario.getRol().getCodigo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta lista es exclusiva para clientes de la tienda.");
        }
        return pedidos.findAllForCliente(usuario.getId()).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoTiendaResumenDTO> listarParaGestion(Jwt jwt) {
        validarPersonal(usuarioAutenticado(jwt));
        return pedidos.findAllForGestion().stream().map(this::toDto).toList();
    }

    @Transactional
    public PedidoTiendaResumenDTO cancelarPedido(Long pedidoId, Jwt jwt) {
        Usuario usuario = usuarioAutenticado(jwt);
        if (usuario.getRol() == null || !"CLIENTE_TIENDA".equals(usuario.getRol().getCodigo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el cliente de la tienda puede cancelar su pedido.");
        }
        PedidoTienda pedido = pedidos.findByIdForUpdate(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el pedido."));
        if (!usuario.getId().equals(pedido.getUsuarioId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes cancelar un pedido de otro cliente.");
        }
        if ("CANCELADO".equalsIgnoreCase(pedido.getEstadoFacturacion()) || "CANCELADO".equalsIgnoreCase(pedido.getEstado())) {
            return toDto(pedido);
        }
        if (pedido.getVentaId() != null || "TERMINADO".equalsIgnoreCase(pedido.getEstadoFacturacion())
                || "FACTURADO".equalsIgnoreCase(pedido.getEstadoFacturacion())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El pedido ya fue facturado y no puede cancelarse desde la tienda.");
        }
        restaurarStock(pedido.getItems());
        pedido.setEstadoFacturacion("CANCELADO");
        pedido.setEstado("CANCELADO");
        pedido.setActualizadoEn(OffsetDateTime.now());
        if (pedido.getPago() != null) anularPago(pedido.getPago());
        return toDto(pedido);
    }

    private void restaurarStock(List<PedidoTiendaItem> items) {
        for (PedidoTiendaItem item : items) {
            Long productoId = item.getProductoServicioId();
            if (productoId == null && item.getTiendaProductoId() != null) {
                productoId = tiendaProductos.findById(item.getTiendaProductoId())
                        .map(t -> t.getProductoServicio() == null ? null : t.getProductoServicio().getId()).orElse(null);
            }
            if (productoId == null) continue;
            ProductoServicio producto = productos.findByIdForUpdate(productoId).orElse(null);
            if (producto == null || !"B".equals(producto.getTipo())) continue;
            BigDecimal stock = producto.getStockActual() == null ? BigDecimal.ZERO : producto.getStockActual();
            producto.setStockActual(stock.add(BigDecimal.valueOf(item.getCantidad())));
        }
    }

    private void anularPago(PagoTienda pago) {
        pago.setEstado("ANULADO");
        Map<String, Object> respuesta = pago.getRespuestaPasarela() == null
                ? new HashMap<>() : new HashMap<>(pago.getRespuestaPasarela());
        respuesta.put("canceladoPorCliente", true);
        respuesta.put("canceladoEn", OffsetDateTime.now().toString());
        pago.setRespuestaPasarela(respuesta);
    }

    private Usuario usuarioAutenticado(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de autenticación inválido.");
        }
        return usuarios.findByFirebaseUid(jwt.getSubject())
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no autorizado."));
    }

    private void validarPersonal(Usuario usuario) {
        Integer rolId = usuario.getRol() == null ? null : usuario.getRol().getId();
        if (rolId == null || rolId < 1 || rolId > 6) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta vista es exclusiva para usuarios del sistema.");
        }
    }

    private PedidoTiendaResumenDTO toDto(PedidoTienda p) {
        String estado = p.getEstadoFacturacion();
        if ("CANCELADO".equalsIgnoreCase(estado) || "CANCELADO".equalsIgnoreCase(p.getEstado())) estado = "CANCELADO";
        else if (p.getVentaId() != null || "FACTURADO".equalsIgnoreCase(estado) || "TERMINADO".equalsIgnoreCase(estado)) estado = "TERMINADO";
        else estado = "PENDIENTE";
        String metodo = p.getPago() == null ? null : p.getPago().getProveedorPago();
        if (metodo != null && metodo.startsWith("SIMULADO-")) metodo = metodo.substring("SIMULADO-".length());
        List<PedidoTiendaResumenDTO.Item> items = new ArrayList<>();
        if (p.getItems() != null) for (PedidoTiendaItem i : p.getItems()) {
            items.add(new PedidoTiendaResumenDTO.Item(i.getId(), i.getNombreProducto(), i.getNombreServicioEstampado(),
                    i.getCantidad(), i.getColorSeleccionado(), i.getTalla(), i.getPrecioProducto(), i.getPrecioEstampado(),
                    i.getSubtotal(), i.getImagenClientePath(), i.getResultadoMockupPath()));
        }
        return new PedidoTiendaResumenDTO(p.getId(), p.getCodigo(), estado, p.getEstado(),
                p.getPago() == null ? null : p.getPago().getEstado(), metodo, p.getFechaCreacion(), p.getUsuarioId(),
                p.getNombreCliente(), p.getEmailCliente(), p.getTipoComprobanteFacturacion(),
                p.getTipoDocumentoFacturacion(), p.getNumeroDocumentoFacturacion(), p.getNombreFacturacion(),
                p.getDireccionEntrega(), p.getUbigeoEntrega(), p.getSubtotal(), p.getCostoEnvio(), p.getTotal(), items);
    }
}
