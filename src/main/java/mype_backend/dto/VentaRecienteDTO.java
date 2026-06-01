package mype_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaRecienteDTO {
    private String clienteNombre;
    private String clienteEmail;
    private LocalDateTime fecha;
    private BigDecimal monto;
    private String estado;
}
