package com.nexusmarket.domain.model;

import com.nexusmarket.domain.enums.TipoBodega;
import com.nexusmarket.domain.valueobject.Direccion;

import java.util.Objects;
import java.util.Optional;

public class Bodega {

    private final String idBodega;
    private String nombre;
    private final TipoBodega tipoBodega;
    private Direccion ubicacion;
    private final Vendedor vendedorAsociado;

    public Bodega(String idBodega, String nombre, TipoBodega tipoBodega,
                   Direccion ubicacion, Vendedor vendedorAsociado) {
        this.idBodega = requireNoVacio(idBodega, "idBodega");
        this.nombre = requireNoVacio(nombre, "nombre");
        this.tipoBodega = Objects.requireNonNull(tipoBodega, "tipoBodega es obligatorio");
        this.ubicacion = Objects.requireNonNull(ubicacion, "ubicacion es obligatoria");
        if (tipoBodega == TipoBodega.VENDEDOR && vendedorAsociado == null) {
            throw new IllegalArgumentException("Una bodega de tipo VENDEDOR requiere un vendedor asociado.");
        }
        this.vendedorAsociado = vendedorAsociado;
    }

    private static String requireNoVacio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede estar vacío.");
        }
        return valor;
    }

    public String getIdBodega() { return idBodega; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = requireNoVacio(nombre, "nombre"); }
    public TipoBodega getTipoBodega() { return tipoBodega; }
    public Direccion getUbicacion() { return ubicacion; }
    public void setUbicacion(Direccion ubicacion) {
        this.ubicacion = Objects.requireNonNull(ubicacion, "ubicacion es obligatoria");
    }
    public Optional<Vendedor> getVendedorAsociado() { return Optional.ofNullable(vendedorAsociado); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bodega)) return false;
        return idBodega.equals(((Bodega) o).idBodega);
    }

    @Override
    public int hashCode() { return Objects.hash(idBodega); }
}
