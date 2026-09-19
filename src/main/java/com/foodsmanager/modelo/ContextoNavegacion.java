/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.foodsmanager.modelo;

/**
 *
 * @author certe
 */
public class ContextoNavegacion {

    private static final ContextoNavegacion INSTANCIA
            = new ContextoNavegacion();

    private int idUltimoRestaurante;

    private ContextoNavegacion() {
        idUltimoRestaurante = 0;
    }

    public static ContextoNavegacion getInstancia() {
        return INSTANCIA;
    }

    public int getIdUltimoRestaurante() {
        return idUltimoRestaurante;
    }

    public void setIdUltimoRestaurante(
            int idUltimoRestaurante
    ) {
        if (idUltimoRestaurante > 0) {
            this.idUltimoRestaurante
                    = idUltimoRestaurante;
        }
    }

    public boolean tieneRestauranteSeleccionado() {
        return idUltimoRestaurante > 0;
    }
}
