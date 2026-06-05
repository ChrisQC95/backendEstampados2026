package mype_backend.dto;

import lombok.Data;

/**
 * DTO para solicitar la emisión de una Nota de Crédito sobre una venta existente.
 * Catálogo 09 SUNAT: motivoNcCodigo (ej. "01"=Anulación, "02"=Anulación x error,
 * "07"=Devolución por ítem, "13"=Ajuste de precio, etc.)
 */
@Data
public class NotaCreditoRequestDTO {
    /** Código del motivo según Catálogo 09 SUNAT. Ej: "01", "07" */
    private String motivoNcCodigo;

    /** Texto libre que sustenta la emisión de la Nota de Crédito */
    private String sustentoNota;
}
