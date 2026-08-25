package com.nexusmarket.domain.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

public final class CorreoElectronico {

    private static final Pattern FORMATO_VALIDO =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private final String valor;

    public CorreoElectronico(String valor) {
        if (valor == null || !FORMATO_VALIDO.matcher(valor).matches()) {
            throw new IllegalArgumentException("Formato de correo electrónico inválido: " + valor);
        }
        this.valor = valor.toLowerCase();
    }

    public String getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CorreoElectronico)) return false;
        return valor.equals(((CorreoElectronico) o).valor);
    }

    @Override
    public int hashCode() { return Objects.hash(valor); }

    @Override
    public String toString() { return valor; }
}
