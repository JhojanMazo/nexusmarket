package com.nexusmarket.domain.model;

import com.nexusmarket.domain.enums.EstadoComercialComprador;
import com.nexusmarket.domain.enums.EstadoUsuario;
import com.nexusmarket.domain.enums.RolUsuario;
import com.nexusmarket.domain.valueobject.CorreoElectronico;
import com.nexusmarket.domain.valueobject.Direccion;
import com.nexusmarket.domain.valueobject.DocumentoIdentidad;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Comprador extends Usuario {

    private Direccion direccionPrincipal;
    private final List<Direccion> direccionesAdicionales = new ArrayList<>();
    private EstadoComercialComprador estadoComercial;

    public Comprador(String idUsuario, String nombreCompleto, CorreoElectronico correoElectronico,
                      DocumentoIdentidad documentoIdentidad, EstadoUsuario estadoUsuario,
                      Direccion direccionPrincipal, EstadoComercialComprador estadoComercial) {
        super(idUsuario, nombreCompleto, correoElectronico, documentoIdentidad, RolUsuario.COMPRADOR, estadoUsuario);
        this.direccionPrincipal = Objects.requireNonNull(direccionPrincipal, "direccionPrincipal es obligatoria");
        this.estadoComercial = Objects.requireNonNull(estadoComercial, "estadoComercial es obligatorio");
    }

    public boolean puedeComprar() {
        return estaActivo() && estadoComercial == EstadoComercialComprador.HABILITADO;
    }

    public void agregarDireccionAdicional(Direccion direccion) {
        direccionesAdicionales.add(Objects.requireNonNull(direccion, "direccion es obligatoria"));
    }

    public Direccion getDireccionPrincipal() { return direccionPrincipal; }
    public void setDireccionPrincipal(Direccion direccionPrincipal) {
        this.direccionPrincipal = Objects.requireNonNull(direccionPrincipal, "direccionPrincipal es obligatoria");
    }

    public List<Direccion> getDireccionesAdicionales() {
        return List.copyOf(direccionesAdicionales);
    }

    public EstadoComercialComprador getEstadoComercial() { return estadoComercial; }
    public void setEstadoComercial(EstadoComercialComprador estadoComercial) {
        this.estadoComercial = Objects.requireNonNull(estadoComercial, "estadoComercial es obligatorio");
    }
}
