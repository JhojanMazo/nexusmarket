package com.nexusmarket.domain.model;

import com.nexusmarket.domain.enums.EstadoUsuario;
import com.nexusmarket.domain.enums.RolUsuario;
import com.nexusmarket.domain.valueobject.CorreoElectronico;
import com.nexusmarket.domain.valueobject.DocumentoIdentidad;

import java.util.Objects;

public abstract class Usuario {

    private final String idUsuario;
    private String nombreCompleto;
    private CorreoElectronico correoElectronico;
    private DocumentoIdentidad documentoIdentidad;
    private final RolUsuario rol;
    private EstadoUsuario estado;

    protected Usuario(String idUsuario, String nombreCompleto, CorreoElectronico correoElectronico,
                       DocumentoIdentidad documentoIdentidad, RolUsuario rol, EstadoUsuario estado) {
        this.idUsuario = requireNoVacio(idUsuario, "idUsuario");
        this.nombreCompleto = requireNoVacio(nombreCompleto, "nombreCompleto");
        this.correoElectronico = Objects.requireNonNull(correoElectronico, "correoElectronico es obligatorio");
        this.documentoIdentidad = Objects.requireNonNull(documentoIdentidad, "documentoIdentidad es obligatorio");
        this.rol = Objects.requireNonNull(rol, "rol es obligatorio");
        this.estado = Objects.requireNonNull(estado, "estado es obligatorio");
    }

    protected static String requireNoVacio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede estar vacío.");
        }
        return valor;
    }

    public boolean estaActivo() {
        return estado == EstadoUsuario.ACTIVO;
    }

    public void bloquear() {
        this.estado = EstadoUsuario.BLOQUEADO;
    }

    public void activar() {
        this.estado = EstadoUsuario.ACTIVO;
    }

    public String getIdUsuario() { return idUsuario; }
    public String getNombreCompleto() { return nombreCompleto; }
    public CorreoElectronico getCorreoElectronico() { return correoElectronico; }
    public DocumentoIdentidad getDocumentoIdentidad() { return documentoIdentidad; }
    public RolUsuario getRol() { return rol; }
    public EstadoUsuario getEstado() { return estado; }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = requireNoVacio(nombreCompleto, "nombreCompleto");
    }

    public void setCorreoElectronico(CorreoElectronico correoElectronico) {
        this.correoElectronico = Objects.requireNonNull(correoElectronico, "correoElectronico es obligatorio");
    }

    public void setEstado(EstadoUsuario estado) {
        this.estado = Objects.requireNonNull(estado, "estado es obligatorio");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        return idUsuario.equals(((Usuario) o).idUsuario);
    }

    @Override
    public int hashCode() { return Objects.hash(idUsuario); }
}
