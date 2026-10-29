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
    private int idUsuario;
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

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        if (idUsuario <= 0) {
            throw new IllegalArgumentException("El pedido debe pertenecer a una cuenta válida.");
        }
        this.idUsuario = idUsuario;
    }

    public void setFechaPedido(LocalDateTime fechaPedido) {
        if (fechaPedido == null) {
            throw new IllegalArgumentException("La fecha del pedido es obligatoria.");
        }
        this.fechaPedido = fechaPedido;
    }

    public void setEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            throw new IllegalArgumentException("El estado del pedido es obligatorio.");
        }
        this.estado = estado;
    }

    public void setTotal(double total) {
        if (!Double.isFinite(total) || total < 0) {
            throw new IllegalArgumentException("El total del pedido no es válido.");
        }
        this.total = total;
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
