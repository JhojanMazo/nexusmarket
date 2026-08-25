package com.nexusmarket.domain.valueobject;

import java.util.Objects;


public final class DocumentoIdentidad {

    private final String tipo;   // ej. "CC", "PASAPORTE", "NIT"
    private final String numero;

    public DocumentoIdentidad(String tipo, String numero) {
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("El tipo de documento es obligatorio.");
        }
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("El número de documento es obligatorio.");
        }
        this.tipo = tipo;
        this.numero = numero;
    }

    public String getTipo() { return tipo; }
    public String getNumero() { return numero; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentoIdentidad)) return false;
        DocumentoIdentidad that = (DocumentoIdentidad) o;
        return tipo.equals(that.tipo) && numero.equals(that.numero);
    }

    @Override
    public int hashCode() { return Objects.hash(tipo, numero); }

    @Override
    public String toString() { return tipo + " " + numero; }
}
