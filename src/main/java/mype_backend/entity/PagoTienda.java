package mype_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(name = "pagos_tienda", uniqueConstraints = {
        @UniqueConstraint(name = "uq_pagos_tienda_pedido", columnNames = "pedido_tienda_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoTienda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_tienda_id", nullable = false, unique = true)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PedidoTienda pedido;

    @Column(name = "proveedor_pago", nullable = false, length = 50)
    private String proveedorPago;

    @Column(name = "codigo_transaccion", length = 150)
    private String codigoTransaccion;

    @Column(nullable = false, length = 40)
    @Builder.Default
    private String estado = "PENDIENTE";

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String moneda = "PEN";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "respuesta_pasarela", columnDefinition = "jsonb")
    private Map<String, Object> respuestaPasarela;

    @Column(name = "pagado_en")
    private OffsetDateTime pagadoEn;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    @PrePersist
    void prePersist() {
        if (fechaCreacion == null) fechaCreacion = OffsetDateTime.now();
    }
}
