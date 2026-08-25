package com.nexusmarket.domain.model;

import com.nexusmarket.domain.enums.EstadoInventario;

import java.util.Objects;

public class Inventario {

    private final String idInventario;
    private final Producto producto;
    private final Bodega bodega;
    private int cantidadDisponible;
    private int cantidadReservada;
    private EstadoInventario estadoInventario;

    public Inventario(String idInventario, Producto producto, Bodega bodega,
                       int cantidadDisponible, int cantidadReservada, EstadoInventario estadoInventario) {
        this.idInventario = requireNoVacio(idInventario, "idInventario");
        this.producto = Objects.requireNonNull(producto, "producto es obligatorio");
        this.bodega = Objects.requireNonNull(bodega, "bodega es obligatoria");
        this.estadoInventario = Objects.requireNonNull(estadoInventario, "estadoInventario es obligatorio");
        validarNoNegativo(cantidadDisponible, "cantidadDisponible");
        validarNoNegativo(cantidadReservada, "cantidadReservada");
        this.cantidadDisponible = cantidadDisponible;
        this.cantidadReservada = cantidadReservada;
    }

    private static String requireNoVacio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede estar vacío.");
        }
        return valor;
    }

    private static void validarNoNegativo(int cantidad, String campo) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede ser negativo.");
        }
    }

    public void reservar(int cantidad) {
        if (estadoInventario == EstadoInventario.DANADO) {
            throw new IllegalStateException("No se puede reservar inventario marcado como Dañado.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a reservar debe ser mayor que cero.");
        }
        if (cantidad > cantidadDisponible) {
            throw new IllegalStateException("No hay existencias suficientes para reservar.");
        }
        cantidadDisponible -= cantidad;
        cantidadReservada += cantidad;
    }

    /** Confirma la salida por venta de unidades previamente reservadas. */
    public void confirmarSalidaPorVenta(int cantidad) {
        if (cantidad <= 0 || cantidad > cantidadReservada) {
            throw new IllegalArgumentException("Cantidad inválida para confirmar salida por venta.");
        }
        cantidadReservada -= cantidad;
    }

    public void reingresarPorDevolucion(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a reingresar debe ser mayor que cero.");
        }
        cantidadDisponible += cantidad;
    }

    public String getIdInventario() { return idInventario; }
    public Producto getProducto() { return producto; }
    public Bodega getBodega() { return bodega; }
    public int getCantidadDisponible() { return cantidadDisponible; }
    public int getCantidadReservada() { return cantidadReservada; }
    public EstadoInventario getEstadoInventario() { return estadoInventario; }
    public void setEstadoInventario(EstadoInventario estadoInventario) {
        this.estadoInventario = Objects.requireNonNull(estadoInventario, "estadoInventario es obligatorio");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Inventario)) return false;
        return idInventario.equals(((Inventario) o).idInventario);
    }

    @Override
    public int hashCode() { return Objects.hash(idInventario); }
}
