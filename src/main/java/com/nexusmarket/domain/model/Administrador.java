package com.nexusmarket.domain.model;

import com.nexusmarket.domain.enums.EstadoUsuario;
import com.nexusmarket.domain.enums.RolUsuario;
import com.nexusmarket.domain.valueobject.CorreoElectronico;
import com.nexusmarket.domain.valueobject.DocumentoIdentidad;

public class Administrador extends Usuario {

    public Administrador(String idUsuario, String nombreCompleto, CorreoElectronico correoElectronico,
                          DocumentoIdentidad documentoIdentidad, EstadoUsuario estadoUsuario) {
        super(idUsuario, nombreCompleto, correoElectronico, documentoIdentidad, RolUsuario.ADMINISTRADOR, estadoUsuario);
    }

    public void incorporarVendedor(Vendedor vendedor, Bodega primeraBodega) {
        vendedor.asociarBodega(primeraBodega);
    }
}
