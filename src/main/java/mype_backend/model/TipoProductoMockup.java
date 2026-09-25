package mype_backend.model;

import java.util.Arrays;

public enum TipoProductoMockup {
    PRENDAS("PRENDAS", "Prendas"),
    ACCESORIOS("ACCESORIOS", "Accesorios"),
    MERCHANDISING("MERCHANDISING", "Merchandising"),
    VARIOS("VARIOS", "Varios");

    private final String codigo;
    private final String label;

    TipoProductoMockup(String codigo, String label) {
        this.codigo = codigo;
        this.label = label;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getLabel() {
        return label;
    }

    public static TipoProductoMockup fromCodigo(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El tipo de producto es obligatorio.");
        }
        return Arrays.stream(values())
                .filter(tipo -> tipo.codigo.equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de producto no soportado: " + value));
    }
}
