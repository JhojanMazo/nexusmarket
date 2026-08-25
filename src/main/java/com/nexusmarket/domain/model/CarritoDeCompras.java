package com.nexusmarket.domain.model;

import com.nexusmarket.domain.valueobject.ItemPedido;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CarritoDeCompras {

    private final String idCarrito;
    private final Comprador comprador;
    private final List<ItemPedido> items = new ArrayList<>();

    public CarritoDeCompras(String idCarrito, Comprador comprador) {
        this.idCarrito = requireNoVacio(idCarrito, "idCarrito");
        this.comprador = Objects.requireNonNull(comprador, "comprador es obligatorio");
    }

    private static String requireNoVacio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede estar vacío.");
        }
        return valor;
    }

    public void agregarItem(ItemPedido item) {
        items.add(Objects.requireNonNull(item, "item es obligatorio"));
    }

    public void vaciar() {
        items.clear();
    }

    public String getIdCarrito() { return idCarrito; }
    public Comprador getComprador() { return comprador; }
    public List<ItemPedido> getItems() { return List.copyOf(items); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CarritoDeCompras)) return false;
        return idCarrito.equals(((CarritoDeCompras) o).idCarrito);
    }

    @Override
    public int hashCode() { return Objects.hash(idCarrito); }
}
