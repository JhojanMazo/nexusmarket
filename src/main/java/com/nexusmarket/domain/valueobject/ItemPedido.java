package com.nexusmarket.domain.valueobject;

import com.nexusmarket.domain.model.Producto;

import java.util.Objects;


public final class ItemPedido {

    private final Producto producto;
    private final int cantidad;
    private final Dinero precioUnitario;

    public ItemPedido(Producto producto, int cantidad, Dinero precioUnitario) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto del ítem de pedido es obligatorio.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }
        if (precioUnitario == null) {
            throw new IllegalArgumentException("El precio unitario es obligatorio.");
        }
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public Dinero subtotal() {
        return new Dinero(
                precioUnitario.getMonto().multiply(java.math.BigDecimal.valueOf(cantidad)),
                precioUnitario.getMoneda()
        );
    }

    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public Dinero getPrecioUnitario() { return precioUnitario; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemPedido)) return false;
        ItemPedido that = (ItemPedido) o;
        return cantidad == that.cantidad
                && producto.equals(that.producto)
                && precioUnitario.equals(that.precioUnitario);
    }

    @Override
    public int hashCode() { return Objects.hash(producto, cantidad, precioUnitario); }
}
