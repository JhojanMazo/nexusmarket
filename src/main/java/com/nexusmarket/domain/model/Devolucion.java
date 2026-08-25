package com.nexusmarket.domain.model;

import com.nexusmarket.domain.enums.EstadoDevolucion;

import java.time.LocalDate;
import java.util.Objects;

public class Devolucion {

    private final String idDevolucion;
    private final Pedido pedido;
    private final String motivo;
    private EstadoDevolucion estado;
    private final LocalDate fechaSolicitud;

    public Devolucion(String idDevolucion, Pedido pedido, String motivo, LocalDate fechaSolicitud) {
        this.idDevolucion = requireNoVacio(idDevolucion, "idDevolucion");
        this.pedido = Objects.requireNonNull(pedido, "pedido es obligatorio");
        this.motivo = requireNoVacio(motivo, "motivo");
        this.fechaSolicitud = Objects.requireNonNull(fechaSolicitud, "fechaSolicitud es obligatoria");
        this.estado = EstadoDevolucion.SOLICITADA;
    }

    private static String requireNoVacio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede estar vacío.");
        }
        return valor;
    }

    public void aprobar() {
        if (estado != EstadoDevolucion.SOLICITADA) {
            throw new IllegalStateException("Solo una devolución SOLICITADA puede ser aprobada.");
        }
        estado = EstadoDevolucion.APROBADA;
    }

    public void rechazar() {
        if (estado != EstadoDevolucion.SOLICITADA) {
            throw new IllegalStateException("Solo una devolución SOLICITADA puede ser rechazada.");
        }
        estado = EstadoDevolucion.RECHAZADA;
    }

    public void completar() {
        if (estado != EstadoDevolucion.APROBADA) {
            throw new IllegalStateException("Solo una devolución APROBADA puede completarse.");
        }
        estado = EstadoDevolucion.COMPLETADA;
    }

    public String getIdDevolucion() { return idDevolucion; }
    public Pedido getPedido() { return pedido; }
    public String getMotivo() { return motivo; }
    public EstadoDevolucion getEstado() { return estado; }
    public LocalDate getFechaSolicitud() { return fechaSolicitud; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Devolucion)) return false;
        return idDevolucion.equals(((Devolucion) o).idDevolucion);
    }

    @Override
    public int hashCode() { return Objects.hash(idDevolucion); }
}
