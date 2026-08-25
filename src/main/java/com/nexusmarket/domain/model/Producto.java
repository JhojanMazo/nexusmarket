package com.nexusmarket.domain.model;

import com.nexusmarket.domain.enums.EstadoProducto;
import com.nexusmarket.domain.enums.TipoProducto;
import com.nexusmarket.domain.valueobject.Dinero;
import com.nexusmarket.domain.valueobject.VarianteProducto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Producto {

    private final String idProducto;
    private String nombre;
    private final TipoProducto tipoProducto;
    private final List<VarianteProducto> variantes = new ArrayList<>();
    private EstadoProducto estado;
    private Dinero precio;
    private final Vendedor vendedor;

    public Producto(String idProducto, String nombre, TipoProducto tipoProducto,
                     EstadoProducto estado, Dinero precio, Vendedor vendedor) {
        this.idProducto = requireNoVacio(idProducto, "idProducto");
        this.nombre = requireNoVacio(nombre, "nombre");
        this.tipoProducto = Objects.requireNonNull(tipoProducto, "tipoProducto es obligatorio");
        this.estado = Objects.requireNonNull(estado, "estado es obligatorio");
        this.precio = Objects.requireNonNull(precio, "precio es obligatorio");
        this.vendedor = Objects.requireNonNull(vendedor, "vendedor es obligatorio");
    }

    private static String requireNoVacio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede estar vacío.");
        }
        return valor;
    }

    public boolean esFisico() { return tipoProducto == TipoProducto.FISICO; }
    public boolean estaPublicado() { return estado == EstadoProducto.PUBLICADO; }

    public void agregarVariante(VarianteProducto variante) {
        variantes.add(Objects.requireNonNull(variante, "variante es obligatoria"));
    }

    public String getIdProducto() { return idProducto; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = requireNoVacio(nombre, "nombre"); }
    public TipoProducto getTipoProducto() { return tipoProducto; }
    public List<VarianteProducto> getVariantes() { return List.copyOf(variantes); }
    public EstadoProducto getEstado() { return estado; }
    public void setEstado(EstadoProducto estado) {
        this.estado = Objects.requireNonNull(estado, "estado es obligatorio");
    }
    public Dinero getPrecio() { return precio; }
    public void setPrecio(Dinero precio) {
        this.precio = Objects.requireNonNull(precio, "precio es obligatorio");
    }
    public Vendedor getVendedor() { return vendedor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Producto)) return false;
        return idProducto.equals(((Producto) o).idProducto);
    }

    @Override
    public int hashCode() { return Objects.hash(idProducto); }
}
