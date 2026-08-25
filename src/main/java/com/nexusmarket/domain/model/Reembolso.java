package com.nexusmarket.domain.model;

import com.nexusmarket.domain.enums.EstadoDevolucion;
import com.nexusmarket.domain.enums.EstadoReembolso;
import com.nexusmarket.domain.valueobject.Dinero;

import java.util.Objects;

public class Reembolso {

    private final String idReembolso;
    private final Devolucion devolucion;
    private final Dinero monto;
    private EstadoReembolso estado;

    public Reembolso(String idReembolso, Devolucion devolucion, Dinero monto) {
        if (devolucion.getEstado() != EstadoDevolucion.APROBADA
                && devolucion.getEstado() != EstadoDevolucion.COMPLETADA) {
            throw new IllegalArgumentException(
                    "Solo se puede generar un reembolso a partir de una devolución aprobada o completada.");
        }
        this.idReembolso = requireNoVacio(idReembolso, "idReembolso");
        this.devolucion = devolucion;
        this.monto = Objects.requireNonNull(monto, "monto es obligatorio");
        this.estado = EstadoReembolso.PENDIENTE;
    }

    private static String requireNoVacio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede estar vacío.");
        }
        return valor;
    }

    public void procesar() {
        if (estado != EstadoReembolso.PENDIENTE) {
            throw new IllegalStateException("Solo un reembolso PENDIENTE puede procesarse.");
        }
        estado = EstadoReembolso.PROCESADO;
    }

    public void rechazar() {
        if (estado != EstadoReembolso.PENDIENTE) {
            throw new IllegalStateException("Solo un reembolso PENDIENTE puede rechazarse.");
        }
        estado = EstadoReembolso.RECHAZADO;
    }

    public String getIdReembolso() { return idReembolso; }
    public Devolucion getDevolucion() { return devolucion; }
    public Dinero getMonto() { return monto; }
    public EstadoReembolso getEstado() { return estado; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reembolso)) return false;
        return idReembolso.equals(((Reembolso) o).idReembolso);
    }

    @Override
    public int hashCode() { return Objects.hash(idReembolso); }
}
