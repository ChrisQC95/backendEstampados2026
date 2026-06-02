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

    // ID de tipos de comprobante según DataInitializer
    private static final long ID_FACTURA    = 1L;
    private static final long ID_BOLETA     = 2L;
    private static final long ID_NOTA_VENTA = 3L;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private SocioNegocioRepository socioNegocioRepository;

    @Autowired
    private ProductoServicioRepository productoRepository;

    public DashboardDTO getResumenDashboard(Long usuarioId) {
        List<Venta> ventas = ventaRepository.findByUsuarioId(usuarioId);
        List<SocioNegocio> socios = socioNegocioRepository.findByUsuarioId(usuarioId);

        long totalProductos = productoRepository.findByUsuarioId(usuarioId).size();
        long totalSocios    = socios.size();

        LocalDateTime now = LocalDateTime.now();
        YearMonth currentMonth = YearMonth.from(now);
        YearMonth previousMonth = currentMonth.minusMonths(1);

        // ── Acumuladores del mes actual ───────────────────────────────────
        BigDecimal totalVentasMes    = BigDecimal.ZERO;
        BigDecimal montoFacturas     = BigDecimal.ZERO;
        BigDecimal montoBoletas      = BigDecimal.ZERO;
        BigDecimal montoNotasVenta   = BigDecimal.ZERO;
        long cantidadVentasMes   = 0;
        long cantidadFacturas    = 0;
        long cantidadBoletas     = 0;
        long cantidadNotasVenta  = 0;

        BigDecimal ingresosMesAnterior = BigDecimal.ZERO;

        for (Venta v : ventas) {
            YearMonth ventaMonth = YearMonth.from(v.getFechaEmision());

            if (ventaMonth.equals(currentMonth)) {
                totalVentasMes = totalVentasMes.add(v.getTotal());
                cantidadVentasMes++;

                if (ID_FACTURA == v.getTipoComprobanteId()) {
                    montoFacturas = montoFacturas.add(v.getTotal());
                    cantidadFacturas++;
                } else if (ID_BOLETA == v.getTipoComprobanteId()) {
                    montoBoletas = montoBoletas.add(v.getTotal());
                    cantidadBoletas++;
                } else if (ID_NOTA_VENTA == v.getTipoComprobanteId()) {
                    montoNotasVenta = montoNotasVenta.add(v.getTotal());
                    cantidadNotasVenta++;
                }
            } else if (ventaMonth.equals(previousMonth)) {
                ingresosMesAnterior = ingresosMesAnterior.add(v.getTotal());
            }
        }

        // ── Cálculo de crecimiento ────────────────────────────────────────
        double crecimiento = 0.0;
        if (ingresosMesAnterior.compareTo(BigDecimal.ZERO) == 0) {
            if (totalVentasMes.compareTo(BigDecimal.ZERO) > 0) crecimiento = 100.0;
        } else {
            crecimiento = totalVentasMes.subtract(ingresosMesAnterior)
                    .divide(ingresosMesAnterior, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).doubleValue();
        }

        // ── Ventas recientes (últimas 5) ──────────────────────────────────
        List<VentaRecienteDTO> ventasRecientes = ventas.stream()
                .sorted(Comparator.comparing(Venta::getFechaEmision).reversed())
                .limit(5)
                .map(v -> {
                    SocioNegocio socio = socios.stream()
                            .filter(s -> s.getId().equals(v.getSocioNegocioId()))
                            .findFirst().orElse(null);
                    String nombre = socio != null ? socio.getNombreRazonSocial() : "Cliente General";
                    String email  = (socio != null && socio.getEmailFacturacion() != null)
                            ? socio.getEmailFacturacion() : "Sin correo";
                    return new VentaRecienteDTO(nombre, email, v.getFechaEmision(), v.getTotal(), v.getEstadoSunat());
                })
                .collect(Collectors.toList());

        // ── Gráfico últimos 6 meses desglosado por tipo ───────────────────
        Locale localePeru = new Locale("es", "PE");
        List<GraficoVentaDTO> graficoVentas = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            YearMonth targetMonth = currentMonth.minusMonths(i);

            BigDecimal fMes  = BigDecimal.ZERO;
            BigDecimal bMes  = BigDecimal.ZERO;
            BigDecimal nvMes = BigDecimal.ZERO;

            for (Venta v : ventas) {
                if (!YearMonth.from(v.getFechaEmision()).equals(targetMonth)) continue;
                if (ID_FACTURA    == v.getTipoComprobanteId()) fMes  = fMes.add(v.getTotal());
                else if (ID_BOLETA     == v.getTipoComprobanteId()) bMes  = bMes.add(v.getTotal());
                else if (ID_NOTA_VENTA == v.getTipoComprobanteId()) nvMes = nvMes.add(v.getTotal());
            }

            BigDecimal totalMes = fMes.add(bMes).add(nvMes);
            // Nombre del mes abreviado en español (Ene, Feb, Mar...)
            String monthName = targetMonth.getMonth()
                    .getDisplayName(TextStyle.SHORT, localePeru);
            // Capitalizar primera letra
            monthName = Character.toUpperCase(monthName.charAt(0)) + monthName.substring(1).toLowerCase();

            graficoVentas.add(new GraficoVentaDTO(monthName, fMes, bMes, nvMes, totalMes));
        }

        return new DashboardDTO(
                totalVentasMes,
                crecimiento,
                cantidadVentasMes,
                montoFacturas,
                cantidadFacturas,
                montoBoletas,
                cantidadBoletas,
                montoNotasVenta,
                cantidadNotasVenta,
                totalProductos,
                totalSocios,
                ventasRecientes,
                graficoVentas
        );
    }
}
