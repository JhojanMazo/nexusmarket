package com.nexusmarket.domain.model;

import com.nexusmarket.domain.enums.EstadoUsuario;
import com.nexusmarket.domain.enums.RolUsuario;
import com.nexusmarket.domain.valueobject.CorreoElectronico;
import com.nexusmarket.domain.valueobject.DocumentoIdentidad;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Vendedor extends Usuario {

    private String razonComercial;
    private final LocalDate fechaIncorporacion;
    private final List<Bodega> bodegasAsociadas = new ArrayList<>();

    public Vendedor(String idUsuario, String nombreCompleto, CorreoElectronico correoElectronico,
                     DocumentoIdentidad documentoIdentidad, EstadoUsuario estadoUsuario,
                     String razonComercial, LocalDate fechaIncorporacion) {
        super(idUsuario, nombreCompleto, correoElectronico, documentoIdentidad, RolUsuario.VENDEDOR, estadoUsuario);
        this.razonComercial = requireNoVacio(razonComercial, "razonComercial");
        this.fechaIncorporacion = Objects.requireNonNull(fechaIncorporacion, "fechaIncorporacion es obligatoria");
    }

    public void asociarBodega(Bodega bodega) {
        bodegasAsociadas.add(Objects.requireNonNull(bodega, "bodega es obligatoria"));
    }

    public String getRazonComercial() { return razonComercial; }
    public void setRazonComercial(String razonComercial) {
        this.razonComercial = requireNoVacio(razonComercial, "razonComercial");
    }

    public LocalDate getFechaIncorporacion() { return fechaIncorporacion; }

    public List<Bodega> getBodegasAsociadas() { return List.copyOf(bodegasAsociadas); }
}
