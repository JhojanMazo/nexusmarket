package com.nexusmarket.domain.valueobject;

import java.math.BigDecimal;
import java.util.Objects;

public final class Dinero {

    private final BigDecimal monto;
    private final String moneda; // código ISO 4217, ej. "COP", "USD"

    public Dinero(BigDecimal monto, String moneda) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto de Dinero no puede ser negativo.");
        }
        if (moneda == null || moneda.isBlank()) {
            throw new IllegalArgumentException("La moneda de Dinero es obligatoria.");
        }
        this.monto = monto;
        this.moneda = moneda.toUpperCase();
    }

    public Dinero sumar(Dinero otro) {
        validarMismaMoneda(otro);
        return new Dinero(this.monto.add(otro.monto), this.moneda);
    }

    public Dinero restar(Dinero otro) {
        validarMismaMoneda(otro);
        return new Dinero(this.monto.subtract(otro.monto), this.moneda);
    }

    private void validarMismaMoneda(Dinero otro) {
        if (!this.moneda.equals(otro.moneda)) {
            throw new IllegalArgumentException("No se pueden operar montos en monedas distintas.");
        }
    }

    public BigDecimal getMonto() { return monto; }
    public String getMoneda() { return moneda; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dinero)) return false;
        Dinero dinero = (Dinero) o;
        return monto.compareTo(dinero.monto) == 0 && moneda.equals(dinero.moneda);
    }

    @Override
    public int hashCode() {
        return Objects.hash(monto.stripTrailingZeros(), moneda);
    }

    @Override
    public String toString() {
        return monto.toPlainString() + " " + moneda;
    }
}
