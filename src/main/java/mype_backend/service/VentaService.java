package mype_backend.service;

import mype_backend.dto.VentaRequestDTO;
import mype_backend.dto.VentaDetalleRequestDTO;
import mype_backend.entity.Venta;
import mype_backend.entity.VentaDetalle;
import mype_backend.entity.Serie;
import mype_backend.repository.VentaRepository;
import mype_backend.repository.SerieRepository;
import mype_backend.repository.ProductoServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private SerieRepository serieRepository;

    @Autowired
    private ProductoServicioRepository productoServicioRepository;

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
        Integer nuevoCorrelativo = serieSeleccionada.getCorrelativoActual() + 1;
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
                    if (producto.getStockActual().compareTo(detalleDTO.getCantidad()) < 0) {
                        throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre()
                                + ". Stock disponible: " + producto.getStockActual());
                    }
                    // Restamos el stock
                    producto.setStockActual(producto.getStockActual().subtract(detalleDTO.getCantidad()));
                    productoServicioRepository.save(producto);
                }
            });
        }

        // 5. Guardar la Venta completa (Por cascada guardará automáticamente el detalle
        // en ventas_detalle)
        return ventaRepository.save(venta);
    }

    public List<Venta> listarHistorial(Long usuarioId) {
        return ventaRepository.findByUsuarioId(usuarioId);
    }
}