package com.nexusmarket.domain.model;

import com.nexusmarket.domain.enums.TipoMovimientoInventario;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;


public final class MovimientoInventario {

    private final String idMovimiento;
    private final Inventario inventario;
    private final TipoMovimientoInventario tipoMovimiento;
    private final int cantidad;
    private final LocalDateTime fecha;
    private final String referencia; // opcional: documento o proceso de origen (ej. idPedido)

    public MovimientoInventario(String idMovimiento, Inventario inventario,
                                 TipoMovimientoInventario tipoMovimiento, int cantidad,
                                 LocalDateTime fecha, String referencia) {
        this.idMovimiento = requireNoVacio(idMovimiento, "idMovimiento");
        this.inventario = Objects.requireNonNull(inventario, "inventario es obligatorio");
        this.tipoMovimiento = Objects.requireNonNull(tipoMovimiento, "tipoMovimiento es obligatorio");
        if (cantidad == 0) {
            throw new IllegalArgumentException("La cantidad del movimiento no puede ser cero.");
        }
        this.cantidad = cantidad;
        this.fecha = Objects.requireNonNull(fecha, "fecha es obligatoria");
        this.referencia = referencia;
    }

    private static String requireNoVacio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede estar vacío.");
        }
        return valor;
    }

    public String getIdMovimiento() { return idMovimiento; }
    public Inventario getInventario() { return inventario; }
    public TipoMovimientoInventario getTipoMovimiento() { return tipoMovimiento; }
    public int getCantidad() { return cantidad; }
    public LocalDateTime getFecha() { return fecha; }
    public Optional<String> getReferencia() { return Optional.ofNullable(referencia); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MovimientoInventario)) return false;
        return idMovimiento.equals(((MovimientoInventario) o).idMovimiento);
    }

    @Override
    public int hashCode() { return Objects.hash(idMovimiento); }
}
