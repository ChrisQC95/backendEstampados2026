package mype_backend.service;

import mype_backend.dto.NotaCreditoRequestDTO;
import mype_backend.dto.ReporteVentaExcelDTO;
import mype_backend.dto.VentaRequestDTO;
import mype_backend.dto.GuiaRemisionRequestDTO;
import mype_backend.dto.VentaDetalleRequestDTO;
import mype_backend.entity.Venta;
import mype_backend.entity.VentaDetalle;
import mype_backend.entity.GuiaRemision;
import mype_backend.entity.Serie;
import mype_backend.entity.TipoComprobante;
import mype_backend.repository.VentaRepository;
import mype_backend.repository.SerieRepository;
import mype_backend.repository.GuiaRemisionRepository;
import mype_backend.repository.ProductoServicioRepository;
import mype_backend.repository.TipoComprobanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

@Service
public class VentaService {

    // IDs estables definidos en DataInitializer
    private static final long ID_FACTURA      = 1L;
    private static final long ID_BOLETA       = 2L;
    private static final long ID_NOTA_CREDITO = 4L;

    @Autowired private VentaRepository ventaRepository;
    @Autowired private SerieRepository serieRepository;
    @Autowired private ProductoServicioRepository productoServicioRepository;
    @Autowired private GuiaRemisionRepository guiaRemisionRepository;
    @Autowired private TipoComprobanteRepository tipoComprobanteRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // REGISTRAR VENTA NORMAL
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional
    public Venta registrarVenta(VentaRequestDTO dto) {
        List<Serie> seriesUsuario = serieRepository.findByUsuarioIdAndTipoComprobanteId(
                dto.getUsuarioId(), dto.getTipoComprobanteId());

        Serie serieSeleccionada = seriesUsuario.stream()
                .filter(s -> s.getSerie().equalsIgnoreCase(dto.getSerie()) && s.getActivo())
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "La serie '" + dto.getSerie() + "' no está configurada o activa para este comprobante."));

        Integer correlativo = serieSeleccionada.getCorrelativoActual() != null
                ? serieSeleccionada.getCorrelativoActual() : 0;
        Integer nuevoCorrelativo = correlativo + 1;
        serieSeleccionada.setCorrelativoActual(nuevoCorrelativo);
        serieRepository.save(serieSeleccionada);

        Venta venta = Venta.builder()
                .usuarioId(dto.getUsuarioId())
                .socioNegocioId(dto.getSocioNegocioId())
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
                    .unidadMedida(detalleDTO.getUnidadMedida())
                    .cantidad(detalleDTO.getCantidad())
                    .precioUnitario(detalleDTO.getPrecioUnitario())
                    .valorUnitario(detalleDTO.getValorUnitario())
                    .igvLinea(detalleDTO.getIgvLinea())
                    .totalLinea(detalleDTO.getTotalLinea())
                    .build();
            venta.agregarDetalle(detalle);

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

        Venta ventaGuardada = ventaRepository.save(venta);

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

    // ─────────────────────────────────────────────────────────────────────────
    // GENERAR NOTA DE CRÉDITO
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Emite una Nota de Crédito (código SUNAT '07') sobre una Factura o Boleta existente.
     * <ul>
     *   <li>Solo aplica sobre tipoComprobanteId = 1 (Factura) o 2 (Boleta).</li>
     *   <li>No puede emitirse si la venta ya tiene {@code documentoOrigenId != null} (ya fue anulada).</li>
     *   <li>Repone stock de todos los bienes ('B') del detalle original.</li>
     *   <li>Marca la venta original con {@code documentoOrigenId = ID de la NC} →
     *       inhabilita PDF y excluye del Excel.</li>
     * </ul>
     */
    @Transactional
    public Venta generarNotaCredito(Long ventaOriginalId, NotaCreditoRequestDTO dto) {

        // 1. Cargar y validar
        Venta original = ventaRepository.findById(ventaOriginalId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + ventaOriginalId));

        if (original.getTipoComprobanteId() != ID_FACTURA
                && original.getTipoComprobanteId() != ID_BOLETA) {
            throw new RuntimeException(
                    "Solo se pueden emitir Notas de Crédito sobre Facturas o Boletas.");
        }
        if (original.getDocumentoOrigenId() != null) {
            throw new RuntimeException(
                    "Este documento ya fue anulado mediante Nota de Crédito ID "
                    + original.getDocumentoOrigenId() + ".");
        }

        // 2. Serie y correlativo de la NC
        List<Serie> seriesNc = serieRepository.findByUsuarioIdAndTipoComprobanteId(
                original.getUsuarioId(), ID_NOTA_CREDITO);

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
            // Numeración automática cuando no hay serie NC configurada
            serieNc = "NC" + (original.getSerie().length() >= 2
                    ? original.getSerie().substring(0, 2) : original.getSerie());
            long existentes = ventaRepository.countByUsuarioIdAndTipoComprobanteId(
                    original.getUsuarioId(), ID_NOTA_CREDITO);
            correlativoNc = (int) (existentes + 1);
        }

        // 3. Construir la NC
        Venta nc = Venta.builder()
                .usuarioId(original.getUsuarioId())
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

        // 4. Clonar detalles + reponer stock de bienes
        for (VentaDetalle d : original.getDetalles()) {
            VentaDetalle dNc = VentaDetalle.builder()
                    .productoId(d.getProductoId())
                    .productoNombre(d.getProductoNombre())
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

        // 5. Guardar NC
        Venta ncGuardada = ventaRepository.save(nc);

        // 6. Marcar venta original: documentoOrigenId = ID de la NC
        original.setDocumentoOrigenId(ncGuardada.getId());
        ventaRepository.save(original);

        return ncGuardada;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HISTORIAL Y REPORTES
    // ─────────────────────────────────────────────────────────────────────────
    public List<Venta> listarHistorial(Long usuarioId) {
        return ventaRepository.findByUsuarioId(usuarioId);
    }

    /**
     * Reporte Excel — excluye NC (tipoComprobanteId=4) y ventas anuladas
     * (documentoOrigenId != null) mediante la query del repositorio.
     */
    public List<ReporteVentaExcelDTO> generarReporteExcel(Long usuarioId,
                                                          LocalDate fechaInicio,
                                                          LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin    = fechaFin.atTime(23, 59, 59);
        return ventaRepository.findReporteExcel(usuarioId, inicio, fin);
    }
}