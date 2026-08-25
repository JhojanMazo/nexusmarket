package com.nexusmarket.domain.model;

import com.nexusmarket.domain.valueobject.Dinero;

import java.time.LocalDate;
import java.util.Objects;


public class Factura {

    private final String idFactura;
    private final Pedido pedido;
    private final LocalDate fechaEmision;
    private final Dinero subtotal;
    private final Dinero impuestos;
    private final Dinero total;

    public Factura(String idFactura, Pedido pedido, LocalDate fechaEmision,
                    Dinero subtotal, Dinero impuestos) {
        this.idFactura = requireNoVacio(idFactura, "idFactura");
        this.pedido = Objects.requireNonNull(pedido, "pedido es obligatorio");
        this.fechaEmision = Objects.requireNonNull(fechaEmision, "fechaEmision es obligatoria");
        this.subtotal = Objects.requireNonNull(subtotal, "subtotal es obligatorio");
        this.impuestos = Objects.requireNonNull(impuestos, "impuestos es obligatorio");
        this.total = subtotal.sumar(impuestos);
    }

    private static String requireNoVacio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede estar vacío.");
        }
        return valor;
    }

    public String getIdFactura() { return idFactura; }
    public Pedido getPedido() { return pedido; }
    public LocalDate getFechaEmision() { return fechaEmision; }
    public Dinero getSubtotal() { return subtotal; }
    public Dinero getImpuestos() { return impuestos; }
    public Dinero getTotal() { return total; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Factura)) return false;
        return idFactura.equals(((Factura) o).idFactura);
    }

    @Override
    public int hashCode() { return Objects.hash(idFactura); }
}
