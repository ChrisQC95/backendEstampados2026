package mype_backend.service;

import mype_backend.dto.DashboardDTO;
import mype_backend.dto.GraficoVentaDTO;
import mype_backend.dto.VentaRecienteDTO;
import mype_backend.entity.SocioNegocio;
import mype_backend.entity.Venta;
import mype_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    // IDs según DataInitializer
    private static final long ID_FACTURA = 1L;
    private static final long ID_BOLETA = 2L;
    private static final long ID_NOTA_VENTA = 3L;
    private static final long ID_NOTA_CREDITO = 4L;

    @Autowired
    private VentaRepository ventaRepository;
    @Autowired
    private SocioNegocioRepository socioNegocioRepository;
    @Autowired
    private ProductoServicioRepository productoRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // HELPER: determina si una venta debe ser IGNORADA por completo en los
    // cálculos financieros.
    //
    // Ignorar cuando:
    // 1. estadoSunat = 'ANULADO' (anulación directa en SUNAT)
    // 2. documentoOrigenId != null (la venta fue anulada por una NC emitida en el
    // sistema)
    //
    // Las NC (tipoComprobanteId=4) NO se ignoran: se acumulan separado para restar.
    // ─────────────────────────────────────────────────────────────────────────
    private static boolean esAnuladaDirectamente(Venta v) {
        return "ANULADO".equalsIgnoreCase(v.getEstadoSunat())
                || v.getDocumentoOrigenId() != null;
    }

    public DashboardDTO getResumenDashboard(Long usuarioId) {
        List<Venta> ventas = ventaRepository.findByUsuarioId(usuarioId);
        List<SocioNegocio> socios = socioNegocioRepository.findByUsuarioId(usuarioId);

        long totalProductos = productoRepository.findByUsuarioId(usuarioId).size();
        long totalSocios = socios.size();

        LocalDateTime now = LocalDateTime.now();
        YearMonth currentMonth = YearMonth.from(now);
        YearMonth previousMonth = currentMonth.minusMonths(1);

        // ── Mapa para buscar el documento original de una NC ──────────────
        Map<Long, Venta> ventaById = ventas.stream()
                .collect(Collectors.toMap(Venta::getId, v -> v));

        // ── Acumuladores del mes actual ───────────────────────────────────
        // Comprobantes "positivos" vigentes (no anulados, no NC)
        BigDecimal montoFacturasBruto = BigDecimal.ZERO;
        BigDecimal montoBoletasBruto = BigDecimal.ZERO;
        BigDecimal montoNotasVenta = BigDecimal.ZERO;

        // NCs del mes separadas por el tipo de comprobante al que afectan
        BigDecimal ncFacturasMes = BigDecimal.ZERO;
        BigDecimal ncBoletasMes = BigDecimal.ZERO;

        long cantidadVentasMes = 0; // solo comprobantes vigentes (excluye NC y anuladas)
        long cantidadFacturas = 0;
        long cantidadBoletas = 0;
        long cantidadNotasVenta = 0;

        BigDecimal ingresosMesAnterior = BigDecimal.ZERO;

        for (Venta v : ventas) {
            YearMonth ventaMonth = YearMonth.from(v.getFechaEmision());

            if (ventaMonth.equals(currentMonth)) {

                if (ID_NOTA_CREDITO == v.getTipoComprobanteId()) {
                    if (!esAnuladaDirectamente(v)) {
                        Venta docOriginal = v.getDocumentoModificadoId() != null
                                ? ventaById.get(v.getDocumentoModificadoId())
                                : null;

                        if (docOriginal != null && !esAnuladaDirectamente(docOriginal)) {
                            if (docOriginal.getTipoComprobanteId() == ID_FACTURA) {
                                ncFacturasMes = ncFacturasMes.add(v.getTotal());
                            } else if (docOriginal.getTipoComprobanteId() == ID_BOLETA) {
                                ncBoletasMes = ncBoletasMes.add(v.getTotal());
                            }
                        }
                    }
                    continue;
                }

                // Saltar comprobantes anulados (estado SUNAT o con NC emitida)
                if (esAnuladaDirectamente(v))
                    continue;

                // Comprobante vigente → acumular por tipo
                cantidadVentasMes++;

                if (ID_FACTURA == v.getTipoComprobanteId()) {
                    montoFacturasBruto = montoFacturasBruto.add(v.getTotal());
                    cantidadFacturas++;
                } else if (ID_BOLETA == v.getTipoComprobanteId()) {
                    montoBoletasBruto = montoBoletasBruto.add(v.getTotal());
                    cantidadBoletas++;
                } else if (ID_NOTA_VENTA == v.getTipoComprobanteId()) {
                    montoNotasVenta = montoNotasVenta.add(v.getTotal());
                    cantidadNotasVenta++;
                }

            } else if (ventaMonth.equals(previousMonth)) {
                // Mes anterior: igual criterio (excluir NC y anuladas)
                if (ID_NOTA_CREDITO != v.getTipoComprobanteId()
                        && !esAnuladaDirectamente(v)) {
                    ingresosMesAnterior = ingresosMesAnterior.add(v.getTotal());
                }
            }
        }

        // ── Desglose NETO por tipo de comprobante (para Cards 2 y 3) ─────
        BigDecimal montoFacturasNeto = montoFacturasBruto.subtract(ncFacturasMes).max(BigDecimal.ZERO);
        BigDecimal montoBoletasNeto = montoBoletasBruto.subtract(ncBoletasMes).max(BigDecimal.ZERO);

        // ── Total Neto = Facturas Netas + Boletas Netas + Notas de Venta ─
        BigDecimal totalNetoMes = montoFacturasNeto.add(montoBoletasNeto).add(montoNotasVenta);

        // ── Cálculo de crecimiento (sobre el neto) ────────────────────────
        double crecimiento = 0.0;
        if (ingresosMesAnterior.compareTo(BigDecimal.ZERO) == 0) {
            if (totalNetoMes.compareTo(BigDecimal.ZERO) > 0)
                crecimiento = 100.0;
        } else {
            crecimiento = totalNetoMes.subtract(ingresosMesAnterior)
                    .divide(ingresosMesAnterior, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).doubleValue();
        }

        // ── Ventas recientes (últimas 5, excluyendo NC y anuladas) ────────
        List<VentaRecienteDTO> ventasRecientes = ventas.stream()
                .filter(v -> ID_NOTA_CREDITO != v.getTipoComprobanteId()
                        && !esAnuladaDirectamente(v))
                .sorted(Comparator.comparing(Venta::getFechaEmision).reversed())
                .limit(5)
                .map(v -> {
                    SocioNegocio socio = socios.stream()
                            .filter(s -> s.getId().equals(v.getSocioNegocioId()))
                            .findFirst().orElse(null);
                    String nombre = socio != null ? socio.getNombreRazonSocial() : "Cliente General";
                    String email = (socio != null && socio.getEmailFacturacion() != null)
                            ? socio.getEmailFacturacion()
                            : "Sin correo";
                    return new VentaRecienteDTO(nombre, email, v.getFechaEmision(), v.getTotal(), v.getEstadoSunat());
                })
                .collect(Collectors.toList());

        // ── Gráfico últimos 6 meses — montos NETOS por tipo ──────────────
        Locale localePeru = new Locale("es", "PE");
        List<GraficoVentaDTO> graficoVentas = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            YearMonth targetMonth = currentMonth.minusMonths(i);

            BigDecimal fBruto = BigDecimal.ZERO;
            BigDecimal bBruto = BigDecimal.ZERO;
            BigDecimal nvBruto = BigDecimal.ZERO;

            BigDecimal ncFacturas = BigDecimal.ZERO;
            BigDecimal ncBoletas = BigDecimal.ZERO;

            for (Venta v : ventas) {
                if (!YearMonth.from(v.getFechaEmision()).equals(targetMonth))
                    continue;

                if (ID_NOTA_CREDITO == v.getTipoComprobanteId()) {
                    if (!esAnuladaDirectamente(v)) {
                        Venta docOriginal = v.getDocumentoModificadoId() != null
                                ? ventaById.get(v.getDocumentoModificadoId())
                                : null;

                        if (docOriginal != null && !esAnuladaDirectamente(docOriginal)) {
                            if (docOriginal.getTipoComprobanteId() == ID_FACTURA) {
                                ncFacturas = ncFacturas.add(v.getTotal());
                            } else if (docOriginal.getTipoComprobanteId() == ID_BOLETA) {
                                ncBoletas = ncBoletas.add(v.getTotal());
                            }
                        }
                    }
                } else if (!esAnuladaDirectamente(v)) {
                    if (ID_FACTURA == v.getTipoComprobanteId())
                        fBruto = fBruto.add(v.getTotal());
                    else if (ID_BOLETA == v.getTipoComprobanteId())
                        bBruto = bBruto.add(v.getTotal());
                    else if (ID_NOTA_VENTA == v.getTipoComprobanteId())
                        nvBruto = nvBruto.add(v.getTotal());
                }
            }

            BigDecimal fNeto = fBruto.subtract(ncFacturas).max(BigDecimal.ZERO);
            BigDecimal bNeto = bBruto.subtract(ncBoletas).max(BigDecimal.ZERO);

            BigDecimal totalMes = fNeto.add(bNeto).add(nvBruto);

            String monthName = targetMonth.getMonth().getDisplayName(TextStyle.SHORT, localePeru);
            monthName = Character.toUpperCase(monthName.charAt(0)) + monthName.substring(1).toLowerCase();

            graficoVentas.add(new GraficoVentaDTO(monthName, fNeto, bNeto, nvBruto, totalMes));
        }

        return new DashboardDTO(
                totalNetoMes,
                crecimiento,
                cantidadVentasMes,
                montoFacturasNeto, // neto (ya descontadas NC)
                cantidadFacturas,
                montoBoletasNeto, // neto (ya descontadas NC)
                cantidadBoletas,
                montoNotasVenta, // notas de venta sin NC (docs internos)
                cantidadNotasVenta,
                totalProductos,
                totalSocios,
                ventasRecientes,
                graficoVentas);
    }
}
