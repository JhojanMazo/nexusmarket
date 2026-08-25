package com.nexusmarket.domain.model;

import com.nexusmarket.domain.enums.EstadoPedido;
import com.nexusmarket.domain.valueobject.Dinero;
import com.nexusmarket.domain.valueobject.Direccion;
import com.nexusmarket.domain.valueobject.ItemPedido;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Pedido {

    private static final List<EstadoPedido> ORDEN_CICLO_VIDA = List.of(
            EstadoPedido.CARRITO,
            EstadoPedido.PENDIENTE_PAGO,
            EstadoPedido.PAGADO,
            EstadoPedido.DESPACHADO,
            EstadoPedido.ENTREGADO_FINALIZADO
    );

    private final String idPedido;
    private final Comprador comprador;
    private final List<ItemPedido> items = new ArrayList<>();
    private Direccion direccionEntrega;
    private EstadoPedido estadoPedido;
    private final LocalDate fechaCreacion;

    public Pedido(String idPedido, Comprador comprador, List<ItemPedido> items,
                   Direccion direccionEntrega, LocalDate fechaCreacion) {
        this.idPedido = requireNoVacio(idPedido, "idPedido");
        this.comprador = Objects.requireNonNull(comprador, "comprador es obligatorio");
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("El pedido debe tener al menos un ítem.");
        }
        this.items.addAll(items);
        this.direccionEntrega = Objects.requireNonNull(direccionEntrega, "direccionEntrega es obligatoria");
        this.fechaCreacion = Objects.requireNonNull(fechaCreacion, "fechaCreacion es obligatoria");
        this.estadoPedido = EstadoPedido.CARRITO;
    }

    private static String requireNoVacio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede estar vacío.");
        }
        return valor;
    }

    /** Avanza el pedido al siguiente estado del ciclo de vida, en orden estricto. */
    public void avanzarA(EstadoPedido nuevoEstado) {
        if (estadoPedido == EstadoPedido.ENTREGADO_FINALIZADO) {
            throw new IllegalStateException("Un pedido finalizado no podrá ser modificado bajo ninguna circunstancia.");
        }
        int actual = ORDEN_CICLO_VIDA.indexOf(estadoPedido);
        int destino = ORDEN_CICLO_VIDA.indexOf(nuevoEstado);
        if (destino != actual + 1) {
            throw new IllegalStateException(
                    "Transición inválida de " + estadoPedido + " a " + nuevoEstado + ".");
        }
        this.estadoPedido = nuevoEstado;
    }

    public Dinero total() {
        Dinero acumulado = null;
        for (ItemPedido item : items) {
            Dinero subtotal = item.subtotal();
            acumulado = (acumulado == null) ? subtotal : acumulado.sumar(subtotal);
        }
        return acumulado;
    }

    public String getIdPedido() { return idPedido; }
    public Comprador getComprador() { return comprador; }
    public List<ItemPedido> getItems() { return List.copyOf(items); }
    public Direccion getDireccionEntrega() { return direccionEntrega; }
    public EstadoPedido getEstadoPedido() { return estadoPedido; }
    public LocalDate getFechaCreacion() { return fechaCreacion; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pedido)) return false;
        return idPedido.equals(((Pedido) o).idPedido);
    }

    @Override
    public int hashCode() { return Objects.hash(idPedido); }
}
