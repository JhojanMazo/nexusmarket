package com.nexusmarket.domain.valueobject;

import java.util.Objects;


public final class Direccion {

    private final String calle;
    private final String ciudad;
    private final String departamentoOEstado;
    private final String pais;
    private final String codigoPostal;
    private final String referencia; // opcional

    public Direccion(String calle, String ciudad, String departamentoOEstado,
                      String pais, String codigoPostal, String referencia) {
        this.calle = requireNoVacio(calle, "calle");
        this.ciudad = requireNoVacio(ciudad, "ciudad");
        this.departamentoOEstado = requireNoVacio(departamentoOEstado, "departamentoOEstado");
        this.pais = requireNoVacio(pais, "pais");
        this.codigoPostal = requireNoVacio(codigoPostal, "codigoPostal");
        this.referencia = referencia; // puede ser null
    }

    private static String requireNoVacio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo '" + campo + "' de Direccion no puede estar vacío.");
        }
        return valor;
    }

    public String getCalle() { return calle; }
    public String getCiudad() { return ciudad; }
    public String getDepartamentoOEstado() { return departamentoOEstado; }
    public String getPais() { return pais; }
    public String getCodigoPostal() { return codigoPostal; }
    public String getReferencia() { return referencia; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Direccion)) return false;
        Direccion that = (Direccion) o;
        return calle.equals(that.calle)
                && ciudad.equals(that.ciudad)
                && departamentoOEstado.equals(that.departamentoOEstado)
                && pais.equals(that.pais)
                && codigoPostal.equals(that.codigoPostal)
                && Objects.equals(referencia, that.referencia);
    }

    @Override
    public int hashCode() {
        return Objects.hash(calle, ciudad, departamentoOEstado, pais, codigoPostal, referencia);
    }

    @Override
    public String toString() {
        return calle + ", " + ciudad + ", " + departamentoOEstado + ", " + pais + " (" + codigoPostal + ")";
    }
}
