package mype_backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class GuiaRemisionRequestDTO {
    private String motivoTrasladoCodigo;
    private Long conductorId;
    private Long vehiculoId;
    private BigDecimal pesoBrutoTotal;
    private String ubigeoPartida;
    private String direccionPartida;
    private String ubigeoLlegada;
    private String direccionLlegada;
}
