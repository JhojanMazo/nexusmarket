package com.nexusmarket.domain.valueobject;

import java.util.Objects;

public final class VarianteProducto {

    private final String atributo; 
    private final String valor;  

    public VarianteProducto(String atributo, String valor) {
        if (atributo == null || atributo.isBlank()) {
            throw new IllegalArgumentException("El atributo de la variante no puede estar vacío.");
        }
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El valor de la variante no puede estar vacío.");
        }
        this.atributo = atributo;
        this.valor = valor;
    }

    public String getAtributo() { return atributo; }
    public String getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VarianteProducto)) return false;
        VarianteProducto that = (VarianteProducto) o;
        return atributo.equalsIgnoreCase(that.atributo) && valor.equalsIgnoreCase(that.valor);
    }

    @Override
    public int hashCode() { return Objects.hash(atributo.toLowerCase(), valor.toLowerCase()); }

    @Override
    public String toString() { return atributo + ": " + valor; }
}
