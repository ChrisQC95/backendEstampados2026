package mype_backend.service;

import mype_backend.dto.NotaCreditoRequestDTO;
import mype_backend.dto.ReporteVentaExcelDTO;
import mype_backend.dto.VentaRequestDTO;
import mype_backend.dto.GuiaRemisionRequestDTO;
import mype_backend.dto.VentaDetalleRequestDTO;
import mype_backend.entity.Venta;
import mype_backend.entity.VentaDetalle;
import mype_backend.entity.GuiaRemision;
import mype_backend.entity.PedidoTienda;
import mype_backend.entity.SocioNegocio;
import mype_backend.entity.Ubigeo;
import mype_backend.entity.Serie;
import mype_backend.entity.TipoComprobante;
import mype_backend.entity.TiendaProducto;
import mype_backend.repository.VentaRepository;
import mype_backend.repository.SerieRepository;
import mype_backend.repository.GuiaRemisionRepository;
import mype_backend.repository.ProductoServicioRepository;
import mype_backend.repository.TipoComprobanteRepository;
import mype_backend.repository.PedidoTiendaRepository;
import mype_backend.repository.SocioNegocioRepository;
import mype_backend.repository.UbigeoRepository;
import mype_backend.repository.TiendaProductoRepository;
import mype_backend.service.TiendaEnvioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.math.BigDecimal;

@Service
public class VentaService {

    private static final long ID_FACTURA = 1L;
    private static final long ID_BOLETA = 2L;
    private static final long ID_NOTA_CREDITO = 4L;

    @Autowired private VentaRepository ventaRepository;
    @Autowired private SerieRepository serieRepository;
    @Autowired private ProductoServicioRepository productoServicioRepository;
    @Autowired private GuiaRemisionRepository guiaRemisionRepository;
    @Autowired private TipoComprobanteRepository tipoComprobanteRepository;
    @Autowired private EmpresaCompartidaService empresaCompartidaService;
    @Autowired private PedidoTiendaRepository pedidoTiendaRepository;
    @Autowired private SocioNegocioRepository socioNegocioRepository;
    @Autowired private UbigeoRepository ubigeoRepository;
    @Autowired private TiendaProductoRepository tiendaProductoRepository;
    @Autowired private TiendaEnvioService tiendaEnvioService;

    @Transactional
    public Venta registrarVenta(VentaRequestDTO dto) {
        Long usuarioEmpresaId = empresaCompartidaService.getUsuarioEmpresaId();
        List<PedidoTienda> pedidosTienda = resolverPedidosTienda(dto);
        Long socioNegocioId = resolverSocioNegocioId(dto, usuarioEmpresaId, pedidosTienda);
        validarDatosPedidos(dto, pedidosTienda);
        Set<Long> productosConStockReservado = new java.util.HashSet<>();
        for (PedidoTienda pedido : pedidosTienda) {
            for (var item : pedido.getItems()) {
                TiendaProducto listing = tiendaProductoRepository.findById(item.getTiendaProductoId()).orElse(null);
                Long productId = item.getProductoServicioId() != null ? item.getProductoServicioId()
                        : listing == null || listing.getProductoServicio() == null ? null : listing.getProductoServicio().getId();
                if (productId != null) productosConStockReservado.add(productId);
            }
        }
        List<Serie> seriesUsuario = serieRepository.findByUsuarioIdAndTipoComprobanteId(
                usuarioEmpresaId, dto.getTipoComprobanteId());

        Serie serieSeleccionada = seriesUsuario.stream()
                .filter(s -> s.getSerie().equalsIgnoreCase(dto.getSerie()) && Boolean.TRUE.equals(s.getActivo()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "La serie '" + dto.getSerie() + "' no está configurada o activa para este comprobante."));

        Integer correlativo = serieSeleccionada.getCorrelativoActual() != null
                ? serieSeleccionada.getCorrelativoActual() : 0;
        Integer nuevoCorrelativo = correlativo + 1;
        serieSeleccionada.setCorrelativoActual(nuevoCorrelativo);
        serieRepository.save(serieSeleccionada);

        Venta venta = Venta.builder()
                .usuarioId(usuarioEmpresaId)
                .socioNegocioId(socioNegocioId)
                .tipoComprobanteId(dto.getTipoComprobanteId())
                .tipoOperacionId(dto.getTipoOperacionId())
                .serie(dto.getSerie())
                .correlativo(nuevoCorrelativo)
                .monedaId(dto.getMonedaId())
                .tipoPagoId(dto.getTipoPagoId())
                .opGravadas(dto.getOpGravadas())
                .opExoneradas(dto.getOpExoneradas())
                .opInafectas(dto.getOpInafectas())
                .igv(dto.getIgv())
                .total(dto.getTotal())
                .fechaVencimiento(dto.getFechaVencimiento())
                .build();

        TipoComprobante tipoComprobante = tipoComprobanteRepository.findById(dto.getTipoComprobanteId())
                .orElseThrow(() -> new RuntimeException(
                        "Tipo de comprobante no encontrado: ID " + dto.getTipoComprobanteId()));

        boolean esNotaDeVenta = "Nota de Venta".equalsIgnoreCase(tipoComprobante.getDescripcion());
        if (esNotaDeVenta) {
            for (VentaDetalleRequestDTO d : dto.getDetalles()) {
                productoServicioRepository.findById(d.getProductoId()).ifPresent(p -> {
                    if (Boolean.TRUE.equals(p.getAfectoIgv())) {
                        throw new RuntimeException(
                                "Las Notas de Venta solo permiten productos no gravados. '"
                                        + p.getNombre() + "' tiene afectación IGV.");
                    }
                });
            }
        }

        for (VentaDetalleRequestDTO detalleDTO : dto.getDetalles()) {
            VentaDetalle detalle = VentaDetalle.builder()
                    .productoId(detalleDTO.getProductoId())
                    .productoNombre(detalleDTO.getProductoNombre())
                    .talla(detalleDTO.getTalla())
                    .unidadMedida(detalleDTO.getUnidadMedida())
                    .cantidad(detalleDTO.getCantidad())
                    .precioUnitario(detalleDTO.getPrecioUnitario())
                    .valorUnitario(detalleDTO.getValorUnitario())
                    .igvLinea(detalleDTO.getIgvLinea())
                    .totalLinea(detalleDTO.getTotalLinea())
                    .build();
            venta.agregarDetalle(detalle);

            if (!productosConStockReservado.contains(detalleDTO.getProductoId())) {
            productoServicioRepository.findById(detalleDTO.getProductoId()).ifPresent(producto -> {
                if ("B".equals(producto.getTipo())) {
                    BigDecimal stock = producto.getStockActual() != null
                            ? producto.getStockActual() : BigDecimal.ZERO;
                    if (stock.compareTo(detalleDTO.getCantidad()) < 0) {
                        throw new RuntimeException("Stock insuficiente para: " + producto.getNombre()
                                + ". Disponible: " + stock);
                    }
                    producto.setStockActual(stock.subtract(detalleDTO.getCantidad()));
                    productoServicioRepository.save(producto);
                }
            });
            }
        }

        Venta ventaGuardada = ventaRepository.save(venta);

        if (!pedidosTienda.isEmpty()) {
            for (PedidoTienda pedido : pedidosTienda) {
                pedido.setEstadoFacturacion("TERMINADO");
                pedido.setVentaId(ventaGuardada.getId());
            }
            pedidoTiendaRepository.saveAll(pedidosTienda);
        }

        if (dto.getGuiaRemision() != null) {
            GuiaRemisionRequestDTO gDto = dto.getGuiaRemision();
            GuiaRemision guia = GuiaRemision.builder()
                    .ventaId(ventaGuardada.getId())
                    .motivoTrasladoCodigo(gDto.getMotivoTrasladoCodigo())
                    .conductorId(gDto.getConductorId())
                    .vehiculoId(gDto.getVehiculoId())
                    .pesoBrutoTotal(gDto.getPesoBrutoTotal())
                    .ubigeoPartida(gDto.getUbigeoPartida())
                    .direccionPartida(gDto.getDireccionPartida())
                    .ubigeoLlegada(gDto.getUbigeoLlegada())
                    .direccionLlegada(gDto.getDireccionLlegada())
                    .build();
            guiaRemisionRepository.save(guia);
        }

        return ventaGuardada;
    }

    private List<PedidoTienda> resolverPedidosTienda(VentaRequestDTO dto) {
        if (dto.getPedidoTiendaIds() == null || dto.getPedidoTiendaIds().isEmpty()) return List.of();
        List<Long> ids = dto.getPedidoTiendaIds().stream().distinct().toList();
        if (ids.size() != dto.getPedidoTiendaIds().size()) {
            throw new RuntimeException("La selección contiene pedidos duplicados.");
        }
        List<PedidoTienda> pedidos = pedidoTiendaRepository.findPedidosPendientesFacturacionPorIdsForUpdate(ids);
        if (pedidos.size() != ids.size()) {
            throw new RuntimeException("Uno o más pedidos ya fueron facturados o dejaron de estar pagados.");
        }
        return pedidos;
    }

    private Long resolverSocioNegocioId(VentaRequestDTO dto, Long usuarioEmpresaId, List<PedidoTienda> pedidos) {
        if (pedidos.isEmpty()) return dto.getSocioNegocioId();

        PedidoTienda principal = pedidos.get(0);
        for (PedidoTienda pedido : pedidos) {
            if (!principal.getTipoComprobanteFacturacion().equals(pedido.getTipoComprobanteFacturacion())
                    || !principal.getTipoDocumentoFacturacion().equals(pedido.getTipoDocumentoFacturacion())
                    || !principal.getNumeroDocumentoFacturacion().equals(pedido.getNumeroDocumentoFacturacion())) {
                throw new RuntimeException("Solo se pueden facturar juntos pedidos del mismo cliente y comprobante.");
            }
        }

        SocioNegocio socio = null;
        if (dto.getSocioNegocioId() != null && dto.getSocioNegocioId() > 0) {
            socio = socioNegocioRepository.findById(dto.getSocioNegocioId())
                    .orElseThrow(() -> new RuntimeException("El cliente seleccionado ya no existe."));
            if (!usuarioEmpresaId.equals(socio.getUsuarioId())
                    || !principal.getTipoDocumentoFacturacion().equals(socio.getTipoDocumento())
                    || !principal.getNumeroDocumentoFacturacion().equals(socio.getNumeroDocumento())) {
                throw new RuntimeException("El cliente seleccionado no coincide con los datos de facturación del pedido.");
            }
        }

        if (socio == null) {
            socio = socioNegocioRepository
                    .findFirstByUsuarioIdAndTipoDocumentoAndNumeroDocumento(usuarioEmpresaId,
                            principal.getTipoDocumentoFacturacion(), principal.getNumeroDocumentoFacturacion())
                    .orElseGet(SocioNegocio::new);
        }
        socio.setUsuarioId(usuarioEmpresaId);
        socio.setTipoSocio("C");
        socio.setTipoDocumento(principal.getTipoDocumentoFacturacion());
        socio.setNumeroDocumento(principal.getNumeroDocumentoFacturacion());
        socio.setTipoPersona("06".equals(principal.getTipoDocumentoFacturacion()) ? "Jurídica" : "Natural");
        socio.setNombreRazonSocial(principal.getNombreFacturacion());
        socio.setDireccionFiscal(principal.getDireccionFiscalFacturacion());
        socio.setEmailFacturacion(principal.getEmailFacturacion());
        if (principal.getUbigeoFiscalFacturacion() != null) {
            Ubigeo ubigeo = ubigeoRepository.findById(principal.getUbigeoFiscalFacturacion())
                    .orElseThrow(() -> new RuntimeException("El ubigeo fiscal del pedido no existe."));
            socio.setUbigeo(ubigeo);
        }
        return socioNegocioRepository.save(socio).getId();
    }

    private void validarDatosPedidos(VentaRequestDTO dto, List<PedidoTienda> pedidos) {
        if (pedidos.isEmpty()) return;
        PedidoTienda principal = pedidos.get(0);
        long tipoComprobanteEsperado = "FACTURA".equals(principal.getTipoComprobanteFacturacion()) ? ID_FACTURA : 2L;
        if (!Long.valueOf(tipoComprobanteEsperado).equals(dto.getTipoComprobanteId())) {
            throw new RuntimeException("El tipo de comprobante no coincide con el seleccionado por el cliente.");
        }
        if (!Long.valueOf(1L).equals(dto.getTipoPagoId())) {
            throw new RuntimeException("Los pedidos pagados en línea deben registrarse como pago al contado.");
        }
        BigDecimal totalPedidos = pedidos.stream()
                .map(p -> p.getTotal() == null ? p.getSubtotal().add(p.getCostoEnvio()) : p.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (dto.getTotal() == null || totalPedidos.compareTo(dto.getTotal()) != 0) {
            throw new RuntimeException("El total de la venta debe coincidir con los productos de los pedidos seleccionados.");
        }

        Map<Long, BigDecimal> cantidadesEsperadas = new HashMap<>();
        Map<Long, BigDecimal> importesEsperados = new HashMap<>();
        for (PedidoTienda pedido : pedidos) {
            for (var item : pedido.getItems()) {
                TiendaProducto listing = tiendaProductoRepository.findById(item.getTiendaProductoId()).orElse(null);
                Long productId = item.getProductoServicioId() != null ? item.getProductoServicioId()
                        : listing == null || listing.getProductoServicio() == null ? null : listing.getProductoServicio().getId();
                if (productId == null) throw new RuntimeException("No se pudo resolver el producto interno del pedido " + pedido.getCodigo() + ".");
                acumularLineaEsperada(cantidadesEsperadas, importesEsperados, productId, item.getCantidad(), item.getPrecioProducto());
                Long serviceId = item.getServicioEstampadoId() != null ? item.getServicioEstampadoId()
                        : listing == null || listing.getServicioEstampado() == null ? null : listing.getServicioEstampado().getId();
                if (serviceId != null && item.getPrecioEstampado() != null && item.getPrecioEstampado().signum() > 0) {
                    acumularLineaEsperada(cantidadesEsperadas, importesEsperados, serviceId, item.getCantidad(), item.getPrecioEstampado());
                }
            }
            BigDecimal costoEnvio = pedido.getCostoEnvio() == null ? BigDecimal.ZERO : pedido.getCostoEnvio();
            if (costoEnvio.signum() > 0) {
                Long shippingProductId = resolverProductoEnvioId(pedido);
                if (shippingProductId == null) {
                    throw new RuntimeException("No se pudo resolver el servicio de envío del pedido " + pedido.getCodigo() + ".");
                }
                acumularLineaEsperada(cantidadesEsperadas, importesEsperados, shippingProductId, 1, costoEnvio);
            }
        }
        Map<Long, BigDecimal> cantidadesVenta = new HashMap<>();
        Map<Long, BigDecimal> importesVenta = new HashMap<>();
        for (VentaDetalleRequestDTO detalle : dto.getDetalles()) {
            if (detalle.getCantidad() == null || detalle.getPrecioUnitario() == null || detalle.getTotalLinea() == null
                    || detalle.getPrecioUnitario().multiply(detalle.getCantidad()).compareTo(detalle.getTotalLinea()) != 0) {
                throw new RuntimeException("El precio y el importe de cada línea deben ser consistentes.");
            }
            cantidadesVenta.merge(detalle.getProductoId(), detalle.getCantidad(), BigDecimal::add);
            importesVenta.merge(detalle.getProductoId(), detalle.getTotalLinea(), BigDecimal::add);
        }
        if (!mapasMonetariamenteIguales(cantidadesEsperadas, cantidadesVenta)
                || !mapasMonetariamenteIguales(importesEsperados, importesVenta)) {
            throw new RuntimeException("Las líneas de la venta deben coincidir exactamente con los productos y precios de los pedidos.");
        }
    }

    private Long resolverProductoEnvioId(PedidoTienda pedido) {
        if (pedido.getProductoServicioEnvioId() != null) return pedido.getProductoServicioEnvioId();
        String codigo = tiendaEnvioService.codigoDesdeObservaciones(pedido.getObservaciones());
        if (codigo == null) return null;
        return tiendaEnvioService.resolverPorCodigo(codigo).productoId();
    }

    private void acumularLineaEsperada(Map<Long, BigDecimal> cantidades, Map<Long, BigDecimal> importes,
                                       Long productoId, Integer cantidad, BigDecimal precioUnitario) {
        cantidades.merge(productoId, BigDecimal.valueOf(cantidad), BigDecimal::add);
        importes.merge(productoId, precioUnitario.multiply(BigDecimal.valueOf(cantidad)), BigDecimal::add);
    }

    private boolean mapasMonetariamenteIguales(Map<Long, BigDecimal> esperados, Map<Long, BigDecimal> recibidos) {
        if (!esperados.keySet().equals(recibidos.keySet())) return false;
        return esperados.entrySet().stream().allMatch(entry -> {
            BigDecimal recibida = recibidos.get(entry.getKey());
            return recibida != null && entry.getValue().compareTo(recibida) == 0;
        });
    }

    @Transactional
    public Venta generarNotaCredito(Long ventaOriginalId, NotaCreditoRequestDTO dto) {
        Long usuarioEmpresaId = empresaCompartidaService.getUsuarioEmpresaId();
        Venta original = ventaRepository.findById(ventaOriginalId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + ventaOriginalId));

        if (!usuarioEmpresaId.equals(original.getUsuarioId())) {
            throw new RuntimeException("La venta no pertenece a la empresa configurada.");
        }

        if (original.getTipoComprobanteId() != ID_FACTURA
                && original.getTipoComprobanteId() != ID_BOLETA) {
            throw new RuntimeException("Solo se pueden emitir Notas de Crédito sobre Facturas o Boletas.");
        }
        if (original.getDocumentoOrigenId() != null) {
            throw new RuntimeException("Este documento ya fue anulado mediante Nota de Crédito ID "
                    + original.getDocumentoOrigenId() + ".");
        }

        List<Serie> seriesNc = serieRepository.findByUsuarioIdAndTipoComprobanteId(
                usuarioEmpresaId, ID_NOTA_CREDITO);

        String serieNc;
        int correlativoNc;

        if (!seriesNc.isEmpty()) {
            Serie s = seriesNc.stream()
                    .filter(Serie::getActivo)
                    .findFirst()
                    .orElse(seriesNc.get(0));
            correlativoNc = (s.getCorrelativoActual() != null ? s.getCorrelativoActual() : 0) + 1;
            s.setCorrelativoActual(correlativoNc);
            serieRepository.save(s);
            serieNc = s.getSerie();
        } else {
            serieNc = "NC" + (original.getSerie().length() >= 2
                    ? original.getSerie().substring(0, 2) : original.getSerie());
            long existentes = ventaRepository.countByUsuarioIdAndTipoComprobanteId(
                    usuarioEmpresaId, ID_NOTA_CREDITO);
            correlativoNc = (int) (existentes + 1);
        }

        Venta nc = Venta.builder()
                .usuarioId(usuarioEmpresaId)
                .socioNegocioId(original.getSocioNegocioId())
                .tipoComprobanteId(ID_NOTA_CREDITO)
                .tipoOperacionId(original.getTipoOperacionId())
                .serie(serieNc)
                .correlativo(correlativoNc)
                .monedaId(original.getMonedaId())
                .tipoPagoId(original.getTipoPagoId())
                .opGravadas(original.getOpGravadas())
                .opExoneradas(original.getOpExoneradas())
                .opInafectas(original.getOpInafectas())
                .igv(original.getIgv())
                .total(original.getTotal())
                .documentoModificadoId(original.getId())
                .motivoNcCodigo(dto.getMotivoNcCodigo())
                .sustentoNota(dto.getSustentoNota())
                .fechaEmision(LocalDateTime.now())
                .estadoSunat("PENDIENTE")
                .build();

        for (VentaDetalle d : original.getDetalles()) {
            VentaDetalle dNc = VentaDetalle.builder()
                    .productoId(d.getProductoId())
                    .productoNombre(d.getProductoNombre())
                    .talla(d.getTalla())
                    .unidadMedida(d.getUnidadMedida())
                    .cantidad(d.getCantidad())
                    .precioUnitario(d.getPrecioUnitario())
                    .valorUnitario(d.getValorUnitario())
                    .igvLinea(d.getIgvLinea())
                    .totalLinea(d.getTotalLinea())
                    .build();
            nc.agregarDetalle(dNc);

            productoServicioRepository.findById(d.getProductoId()).ifPresent(p -> {
                if ("B".equals(p.getTipo())) {
                    BigDecimal stock = p.getStockActual() != null ? p.getStockActual() : BigDecimal.ZERO;
                    p.setStockActual(stock.add(d.getCantidad()));
                    productoServicioRepository.save(p);
                }
            });
        }

        Venta ncGuardada = ventaRepository.save(nc);
        original.setDocumentoOrigenId(ncGuardada.getId());
        ventaRepository.save(original);

        return ncGuardada;
    }

    public List<Venta> listarHistorial(Long usuarioId) {
        return ventaRepository.findByUsuarioId(empresaCompartidaService.getUsuarioEmpresaId());
    }

    public List<ReporteVentaExcelDTO> generarReporteExcel(Long usuarioId,
                                                          LocalDate fechaInicio,
                                                          LocalDate fechaFin) {
        Long usuarioEmpresaId = empresaCompartidaService.getUsuarioEmpresaId();
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(23, 59, 59);
        return ventaRepository.findReporteExcel(usuarioEmpresaId, inicio, fin);
    }
}

