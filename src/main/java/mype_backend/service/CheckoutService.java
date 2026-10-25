package mype_backend.service;

import mype_backend.dto.CheckoutItem;
import mype_backend.dto.CheckoutRequest;
import mype_backend.dto.CheckoutResponse;
import mype_backend.dto.PagoSimuladoRequest;
import mype_backend.dto.PagoSimuladoResponse;
import mype_backend.entity.PagoTienda;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.Map;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class CheckoutService {

    private static final BigDecimal MAX_ORDER_AMOUNT = new BigDecimal("99999999.99");

    private static final java.util.Set<String> AVAILABLE_SIZES = java.util.Set.of("S", "M", "L", "XL");

    private final UsuarioRepository usuarioRepository;
    private final TiendaProductoRepository tiendaProductoRepository;
    private final ProductoServicioRepository productoServicioRepository;
    private final PedidoTiendaRepository pedidoTiendaRepository;
    private final TiendaEnvioService tiendaEnvioService;

    public CheckoutService(
            UsuarioRepository usuarioRepository,
            TiendaProductoRepository tiendaProductoRepository,
            ProductoServicioRepository productoServicioRepository,
            PedidoTiendaRepository pedidoTiendaRepository,
            TiendaEnvioService tiendaEnvioService) {
        this.usuarioRepository = usuarioRepository;
        this.tiendaProductoRepository = tiendaProductoRepository;
        this.productoServicioRepository = productoServicioRepository;
        this.pedidoTiendaRepository = pedidoTiendaRepository;
        this.tiendaEnvioService = tiendaEnvioService;
    }

    @Transactional
    public CheckoutResponse createPendingOrder(Jwt jwt, CheckoutRequest request) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token Firebase inválido.");
        }

        Usuario customer = usuarioRepository.findByFirebaseUid(jwt.getSubject())
                .filter(user -> Boolean.TRUE.equals(user.getActivo()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Cliente no autorizado."));

        if (customer.getRol() == null || !"CLIENTE_TIENDA".equals(customer.getRol().getCodigo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La compra requiere una cuenta de cliente de tienda.");
        }

        // El código único también materializa la clave de idempotencia en el esquema actual.
        String orderCode = "W" + request.idempotencyKey().replace("-", "").toUpperCase();
        var previous = pedidoTiendaRepository.findByCodigo(orderCode);
        if (previous.isPresent()) {
            if (!previous.get().getUsuarioId().equals(customer.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "La clave de compra ya fue utilizada.");
            }
            return toResponse(previous.get());
        }

        if (customer.getEmail() == null || customer.getEmail().length() > 150) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "El correo del cliente no es válido para registrar el pedido.");
        }
        String customerName = customer.getNombre() == null || customer.getNombre().isBlank()
                ? customer.getEmail()
                : customer.getNombre().trim();
        if (customerName.length() > 150) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "El nombre del cliente supera el máximo permitido.");
        }

        TiendaEnvioService.ServicioEnvio servicioEnvio = tiendaEnvioService.resolver(request.delivery().zoneId());
        BigDecimal shipping = money(servicioEnvio.precio());

        PedidoTienda pedido = PedidoTienda.builder()
                .usuarioId(customer.getId())
                .codigo(orderCode)
                .estado("PENDIENTE_PAGO")
                .nombreCliente(customerName)
                .emailCliente(customer.getEmail())
                .direccionEntrega(request.delivery().address().trim())
                .ubigeoEntrega(request.delivery().ubigeo())
                .referenciaEntrega(blankToNull(request.delivery().reference()))
                .tipoComprobanteFacturacion(request.billingDetails().invoiceType())
                .tipoDocumentoFacturacion(request.billingDetails().documentType())
                .numeroDocumentoFacturacion(request.billingDetails().documentNumber().trim())
                .nombreFacturacion(blankToNull("FACTURA".equals(request.billingDetails().invoiceType())
                        ? request.billingDetails().businessName() : request.billingDetails().fullName()))
                .direccionFiscalFacturacion(blankToNull(request.billingDetails().fiscalAddress()))
                .ubigeoFiscalFacturacion(blankToNull(request.billingDetails().fiscalUbigeo()))
                .emailFacturacion(request.billingDetails().email().trim())
                .costoEnvio(shipping)
                .productoServicioEnvioId(servicioEnvio.productoId())
                // La tabla no tiene columna zona_entrega; se conserva la zona en observaciones.
                .observaciones("Zona de entrega: " + request.delivery().zoneId())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (CheckoutItem requestedItem : request.items().stream()
                .sorted(Comparator.comparing(CheckoutItem::tiendaProductoId))
                .toList()) {
            TiendaProducto listing = tiendaProductoRepository.findById(requestedItem.tiendaProductoId())
                    .filter(item -> Boolean.TRUE.equals(item.getPublicado()))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Producto de tienda no disponible: " + requestedItem.tiendaProductoId()));

            if (listing.getMockupProductoBase() == null
                    || !Boolean.TRUE.equals(listing.getMockupProductoBase().getActivo())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El producto ya no está disponible.");
            }

            String talla = blankToNull(requestedItem.size());
            if (Boolean.TRUE.equals(listing.getManejaTallas())) {
                if (talla == null || !AVAILABLE_SIZES.contains(talla.toUpperCase(java.util.Locale.ROOT))) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Selecciona una talla válida (S, M, L o XL) para " + listing.getProductoServicio().getNombre() + ".");
                }
                talla = talla.toUpperCase(java.util.Locale.ROOT);
            } else {
                talla = null;
            }

            // Lock de la fila del producto para que dos checkouts concurrentes no consuman el mismo stock.
            ProductoServicio product = productoServicioRepository.findByIdForUpdate(listing.getProductoServicio().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Producto base no disponible."));
            BigDecimal productPrice = money(product.getPrecioVenta());
            BigDecimal servicePrice = listing.getServicioEstampado() == null
                    ? BigDecimal.ZERO.setScale(2)
                    : money(listing.getServicioEstampado().getPrecioVenta());

            if ("B".equals(product.getTipo())) {
                BigDecimal stock = product.getStockActual() == null ? BigDecimal.ZERO : product.getStockActual();
                BigDecimal requestedQuantity = BigDecimal.valueOf(requestedItem.quantity());
                if (stock.compareTo(requestedQuantity) < 0) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Stock insuficiente para " + product.getNombre() + ". Disponible: " + stock.stripTrailingZeros().toPlainString());
                }
                // Reserva restando stock en esta misma transacción. Un flujo de cancelación debe liberarlo.
                product.setStockActual(stock.subtract(requestedQuantity));
                productoServicioRepository.save(product);
            }

            BigDecimal unitPrice = productPrice.add(servicePrice).setScale(2, RoundingMode.HALF_UP);
            BigDecimal lineSubtotal = unitPrice.multiply(BigDecimal.valueOf(requestedItem.quantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(lineSubtotal);

            PedidoTiendaItem item = PedidoTiendaItem.builder()
                    .tiendaProductoId(listing.getId())
                    .productoServicioId(product.getId())
                    .servicioEstampadoId(listing.getServicioEstampado() == null
                            ? null : listing.getServicioEstampado().getId())
                    .nombreProducto(product.getNombre())
                    .nombreServicioEstampado(listing.getServicioEstampado() == null
                            ? null : listing.getServicioEstampado().getNombre())
                    .cantidad(requestedItem.quantity())
                    .colorSeleccionado(blankToNull(requestedItem.color()))
                    .talla(talla)
                    .precioProducto(productPrice)
                    .precioEstampado(servicePrice)
                    .precioUnitarioTotal(unitPrice)
                    .subtotal(lineSubtotal)
                    .imagenClientePath(blankToNull(requestedItem.customerImagePath()))
                    .resultadoMockupPath(blankToNull(requestedItem.resultadoMockupPath()))
                    .areaX(decimal(requestedItem.mockup() == null ? null : requestedItem.mockup().x()))
                    .areaY(decimal(requestedItem.mockup() == null ? null : requestedItem.mockup().y()))
                    .areaWidth(decimal(requestedItem.mockup() == null ? null : requestedItem.mockup().scale()))
                    .areaHeight(decimal(requestedItem.mockup() == null ? null : requestedItem.mockup().scale()))
                    .build();
            pedido.agregarItem(item);
        }

        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(shipping).setScale(2, RoundingMode.HALF_UP);
        if (total.compareTo(MAX_ORDER_AMOUNT) > 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "El total supera el máximo admitido para un pedido.");
        }
        pedido.setSubtotal(subtotal);
        pedido.setTotal(total);

        PagoTienda payment = PagoTienda.builder()
                .proveedorPago(request.paymentMethod().name())
                .estado("PENDIENTE")
                .monto(total)
                .moneda("PEN")
                .build();
        pedido.asignarPago(payment);

        return toResponse(pedidoTiendaRepository.saveAndFlush(pedido));
    }

    @Transactional
    public PagoSimuladoResponse procesarPagoSimulado(Long pedidoId, Jwt jwt, PagoSimuladoRequest request) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token Firebase inválido.");
        }

        Usuario customer = usuarioRepository.findByFirebaseUid(jwt.getSubject())
                .filter(user -> Boolean.TRUE.equals(user.getActivo()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Cliente no autorizado."));

        PedidoTienda pedido = pedidoTiendaRepository.findByIdForUpdate(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado."));

        // Se responde 404 también cuando pertenece a otro usuario para no revelar pedidos ajenos.
        if (!customer.getId().equals(pedido.getUsuarioId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado.");
        }

        PagoTienda pago = pedido.getPago();
        if ("APROBADO".equals(pago == null ? null : pago.getEstado())
                && "EN_PRODUCCION".equals(pedido.getEstado())) {
            return new PagoSimuladoResponse("APROBADO", "El pago de este pedido ya estaba confirmado.");
        }

        if (!"PENDIENTE_PAGO".equals(pedido.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El pedido no está pendiente de pago.");
        }
        if (pago == null || !"PENDIENTE".equals(pago.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El pago no está disponible para procesarse.");
        }

        // Simulación interna: no se envían datos de tarjeta a la API ni a la base de datos.
        pago.setProveedorPago("SIMULADO-" + request.paymentMethodId());
        pago.setCodigoTransaccion("SIM-" + UUID.randomUUID());
        pago.setEstado("APROBADO");
        pago.setPagadoEn(OffsetDateTime.now());
        pago.setRespuestaPasarela(Map.of(
                "modo", "SIMULADO",
                "paymentMethodId", request.paymentMethodId(),
                "resultado", "APROBADO"
        ));

        // La restricción CHECK existente de Supabase acepta EN_PRODUCCION, pero no PREPARACION.
        pedido.setEstado("EN_PRODUCCION");

        return new PagoSimuladoResponse("APROBADO", "Pago aprobado. Pedido enviado a preparación.");
    }

    private CheckoutResponse toResponse(PedidoTienda pedido) {
        String paymentStatus = pedido.getPago() == null ? "PENDIENTE" : pedido.getPago().getEstado();
        return new CheckoutResponse(pedido.getId(), pedido.getCodigo(), pedido.getEstado(), paymentStatus,
                pedido.getSubtotal(), pedido.getCostoEnvio(), pedido.getTotal(), "PEN");
    }

    private static BigDecimal money(BigDecimal value) {
        if (value == null || value.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El producto tiene un precio inválido.");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal decimal(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
