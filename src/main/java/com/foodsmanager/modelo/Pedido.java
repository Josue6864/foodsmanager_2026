/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.foodsmanager.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author certe
 */
public class Pedido {
    private int idPedido;
    private String nombreCliente;
    private String telefono;
    private String personaRecoge;
    private String horaRecogida;
    private String indicaciones;
    private LocalDateTime fechaPedido;
    private double total;
    private String estado;

    private final List<DetallePedido> detalles;

    public Pedido(
            String nombreCliente,
            String telefono,
            String personaRecoge,
            String horaRecogida,
            String indicaciones
    ) {
        this.nombreCliente = nombreCliente;
        this.telefono = telefono;
        this.personaRecoge = personaRecoge;
        this.horaRecogida = horaRecogida;
        this.indicaciones = indicaciones;

        fechaPedido = LocalDateTime.now();
        estado = "PAGADO";
        detalles = new ArrayList<>();
    }

    public void agregarDetalle(DetallePedido detalle) {
        if (detalle != null) {
            detalles.add(detalle);
            calcularTotal();
        }
    }

    public void calcularTotal() {
        total = 0;

        for (DetallePedido detalle : detalles) {
            total += detalle.getSubtotal();
        }
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;

        for (DetallePedido detalle : detalles) {
            detalle.setIdPedido(idPedido);
        }
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getPersonaRecoge() {
        return personaRecoge;
    }

    public String getHoraRecogida() {
        return horaRecogida;
    }

    public String getIndicaciones() {
        return indicaciones;
    }

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }

    public double getTotal() {
        return total;
    }

    public String getEstado() {
        return estado;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }
}
