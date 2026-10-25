package mype_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportePedidosTiendaDTO {
    private List<PedidosTiendaPorEstadoDTO> pedidosPorEstado;
    private List<ProductosTiendaMasSolicitadosDTO> productosMasSolicitados;
}
