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

    @Autowired
    private VentaRepository ventaRepository;
    @Autowired
    private ProductoServicioRepository productoRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private SocioNegocioRepository socioNegocioRepository;
    @Autowired
    private VehiculoRepository vehiculoRepository;
    @Autowired
    private ConductorRepository conductorRepository;

    public DashboardDTO getResumenDashboard(Long usuarioId) {
        long totalProductos = productoRepository.findByUsuarioId(usuarioId).size();
        long totalCategorias = categoriaRepository.findByUsuarioId(usuarioId).size();
        long totalVehiculos = vehiculoRepository.findByUsuarioId(usuarioId).size();
        long totalConductores = conductorRepository.findByUsuarioId(usuarioId).size();
        
        List<SocioNegocio> socios = socioNegocioRepository.findByUsuarioId(usuarioId);
        long totalSocios = socios.size();

        List<Venta> ventas = ventaRepository.findByUsuarioId(usuarioId);
        
        LocalDateTime now = LocalDateTime.now();
        YearMonth currentMonth = YearMonth.from(now);
        YearMonth previousMonth = currentMonth.minusMonths(1);

        BigDecimal ingresosMesActual = BigDecimal.ZERO;
        BigDecimal ingresosMesAnterior = BigDecimal.ZERO;
        long cantidadVentasMes = 0;

        for (Venta v : ventas) {
            YearMonth ventaMonth = YearMonth.from(v.getFechaEmision());
            if (ventaMonth.equals(currentMonth)) {
                ingresosMesActual = ingresosMesActual.add(v.getTotal());
                cantidadVentasMes++;
            } else if (ventaMonth.equals(previousMonth)) {
                ingresosMesAnterior = ingresosMesAnterior.add(v.getTotal());
            }
        }

        Double crecimiento = 0.0;
        if (ingresosMesAnterior.compareTo(BigDecimal.ZERO) == 0) {
            if (ingresosMesActual.compareTo(BigDecimal.ZERO) > 0) {
                crecimiento = 100.0;
            }
        } else {
            crecimiento = ingresosMesActual.subtract(ingresosMesAnterior)
                    .divide(ingresosMesAnterior, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).doubleValue();
        }

        List<VentaRecienteDTO> ventasRecientes = ventas.stream()
                .sorted(Comparator.comparing(Venta::getFechaEmision).reversed())
                .limit(5)
                .map(v -> {
                    SocioNegocio socio = socios.stream().filter(s -> s.getId().equals(v.getSocioNegocioId())).findFirst().orElse(null);
                    String nombre = socio != null ? socio.getNombreRazonSocial() : "Cliente General";
                    String email = socio != null && socio.getEmailFacturacion() != null ? socio.getEmailFacturacion() : "Sin correo";
                    return new VentaRecienteDTO(nombre, email, v.getFechaEmision(), v.getTotal(), v.getEstadoSunat());
                })
                .collect(Collectors.toList());

        List<GraficoVentaDTO> graficoVentas = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth targetMonth = currentMonth.minusMonths(i);
            BigDecimal totalMes = ventas.stream()
                    .filter(v -> YearMonth.from(v.getFechaEmision()).equals(targetMonth))
                    .map(Venta::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            String monthName = targetMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            graficoVentas.add(new GraficoVentaDTO(monthName, totalMes));
        }

        return new DashboardDTO(
                ingresosMesActual,
                crecimiento,
                cantidadVentasMes,
                totalProductos,
                totalCategorias,
                totalSocios,
                totalVehiculos,
                totalConductores,
                ventasRecientes,
                graficoVentas
        );
    }
}
