package mype_backend.service;

import mype_backend.dto.ReporteVentaExcelDTO;
import mype_backend.dto.VentaRequestDTO;
import mype_backend.dto.GuiaRemisionRequestDTO;
import mype_backend.dto.VentaDetalleRequestDTO;
import mype_backend.entity.Venta;
import mype_backend.entity.VentaDetalle;
import mype_backend.entity.GuiaRemision;
import mype_backend.entity.Serie;
import mype_backend.repository.VentaRepository;
import mype_backend.repository.SerieRepository;
import mype_backend.repository.GuiaRemisionRepository;
import mype_backend.repository.ProductoServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private SerieRepository serieRepository;

    @Autowired
    private ProductoServicioRepository productoServicioRepository;

    @Autowired
    private GuiaRemisionRepository guiaRemisionRepository;

    @Transactional
    public Venta registrarVenta(VentaRequestDTO dto) {
        // 1. Control de Correlativo: Buscar la serie exacta activa del usuario
        List<Serie> seriesUsuario = serieRepository.findByUsuarioIdAndTipoComprobanteId(dto.getUsuarioId(),
                dto.getTipoComprobanteId());

        Serie serieSeleccionada = seriesUsuario.stream()
                .filter(s -> s.getSerie().equalsIgnoreCase(dto.getSerie()) && s.getActivo())
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "La serie '" + dto.getSerie() + "' no está configurada o activa para este comprobante."));

        // 2. Incrementar el correlativo de forma automática
        Integer correlativo = serieSeleccionada.getCorrelativoActual() != null ? serieSeleccionada.getCorrelativoActual() : 0;
        Integer nuevoCorrelativo = correlativo + 1;
        serieSeleccionada.setCorrelativoActual(nuevoCorrelativo);
        serieRepository.save(serieSeleccionada); // Actualiza la numeración en la BD

        // 3. Mapear el DTO a la Entidad Venta (Cabecera)
        Venta venta = Venta.builder()
                .usuarioId(dto.getUsuarioId())
                .socioNegocioId(dto.getSocioNegocioId())
                .tipoComprobanteId(dto.getTipoComprobanteId())
                .tipoOperacionId(dto.getTipoOperacionId())
                .serie(dto.getSerie())
                .correlativo(nuevoCorrelativo) // Asignamos el número calculado por el backend
                .monedaId(dto.getMonedaId())
                .tipoPagoId(dto.getTipoPagoId())
                .opGravadas(dto.getOpGravadas())
                .opExoneradas(dto.getOpExoneradas())
                .opInafectas(dto.getOpInafectas())
                .igv(dto.getIgv())
                .total(dto.getTotal())
                .fechaVencimiento(dto.getFechaVencimiento())
                .build();

        // 4. Procesar el Carrito de Compras (Detalles) y Actualizar Inventario
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

            // Sincronizar la relación padre-hijo obligatoria en JPA
            venta.agregarDetalle(detalle);

            // Control de Inventario: Restar stock solo si es un Bien ('B'). Los Servicios
            // ('S') se ignoran.
            productoServicioRepository.findById(detalleDTO.getProductoId()).ifPresent(producto -> {
                if ("B".equals(producto.getTipo())) {
                    BigDecimal stockActual = producto.getStockActual() != null ? producto.getStockActual() : BigDecimal.ZERO;
                    if (stockActual.compareTo(detalleDTO.getCantidad()) < 0) {
                        throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre()
                                + ". Stock disponible: " + stockActual);
                    }
                    // Restamos el stock
                    producto.setStockActual(stockActual.subtract(detalleDTO.getCantidad()));
                    productoServicioRepository.save(producto);
                }
            });
        }
        Venta ventaGuardada = ventaRepository.save(venta);
        if (dto.getGuiaRemision() != null) {
            GuiaRemisionRequestDTO guiaDto = dto.getGuiaRemision();

            GuiaRemision guia = GuiaRemision.builder()
                    .ventaId(ventaGuardada.getId()) // <- Aquí usamos el ID recién creado
                    .motivoTrasladoCodigo(guiaDto.getMotivoTrasladoCodigo())
                    .conductorId(guiaDto.getConductorId())
                    .vehiculoId(guiaDto.getVehiculoId())
                    .pesoBrutoTotal(guiaDto.getPesoBrutoTotal())
                    .ubigeoPartida(guiaDto.getUbigeoPartida())
                    .direccionPartida(guiaDto.getDireccionPartida())
                    .ubigeoLlegada(guiaDto.getUbigeoLlegada())
                    .direccionLlegada(guiaDto.getDireccionLlegada())
                    .build();

            guiaRemisionRepository.save(guia);
        }

        return ventaGuardada;
    }

    public List<Venta> listarHistorial(Long usuarioId) {
        return ventaRepository.findByUsuarioId(usuarioId);
    }

    /**
     * Genera los datos para el reporte Excel Maestro-Detalle.
     * @param usuarioId ID del usuario (empresa)
     * @param fechaInicio Inicio del rango (YYYY-MM-DD), incluido
     * @param fechaFin Fin del rango (YYYY-MM-DD), incluido (se lleva al final del día)
     */
    public List<ReporteVentaExcelDTO> generarReporteExcel(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        // Convertir a LocalDateTime para la comparación (inicio: 00:00:00, fin: 23:59:59)
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(23, 59, 59);
        return ventaRepository.findReporteExcel(usuarioId, inicio, fin);
    }
}