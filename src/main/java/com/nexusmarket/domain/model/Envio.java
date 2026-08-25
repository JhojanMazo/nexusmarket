package com.nexusmarket.domain.model;

import com.nexusmarket.domain.valueobject.Direccion;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;


public class Envio {

    private final String idEnvio;
    private final Pedido pedido;
    private final OperadorLogistico operadorLogistico;
    private LocalDate fechaDespacho;   // se asigna al despachar
    private LocalDate fechaEntrega;    // se asigna al confirmar la entrega
    private final Direccion direccionEntrega;

    public Envio(String idEnvio, Pedido pedido, OperadorLogistico operadorLogistico, Direccion direccionEntrega) {
        this.idEnvio = requireNoVacio(idEnvio, "idEnvio");
        this.pedido = Objects.requireNonNull(pedido, "pedido es obligatorio");
        this.operadorLogistico = Objects.requireNonNull(operadorLogistico, "operadorLogistico es obligatorio");
        this.direccionEntrega = Objects.requireNonNull(direccionEntrega, "direccionEntrega es obligatoria");
    }

    private static String requireNoVacio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede estar vacío.");
        }
        return valor;
    }

    public void registrarDespacho(LocalDate fecha) {
        this.fechaDespacho = Objects.requireNonNull(fecha, "fecha de despacho es obligatoria");
    }

    public void confirmarEntrega(LocalDate fecha) {
        if (fechaDespacho == null) {
            throw new IllegalStateException("No se puede confirmar la entrega de un envío que no ha sido despachado.");
        }
        this.fechaEntrega = Objects.requireNonNull(fecha, "fecha de entrega es obligatoria");
    }

    public String getIdEnvio() { return idEnvio; }
    public Pedido getPedido() { return pedido; }
    public OperadorLogistico getOperadorLogistico() { return operadorLogistico; }
    public Optional<LocalDate> getFechaDespacho() { return Optional.ofNullable(fechaDespacho); }
    public Optional<LocalDate> getFechaEntrega() { return Optional.ofNullable(fechaEntrega); }
    public Direccion getDireccionEntrega() { return direccionEntrega; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Envio)) return false;
        return idEnvio.equals(((Envio) o).idEnvio);
    }

    @Override
    public int hashCode() { return Objects.hash(idEnvio); }
}
