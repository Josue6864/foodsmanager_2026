/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.foodsmanager.modelo;

/**
 *
 * @author certe
 */
public class ItemCarrito {

    private final Producto producto;
    private int cantidad;

    public ItemCarrito(Producto producto) {
        this(producto, 1);
    }

    public ItemCarrito(Producto producto, int cantidad) {
        if (producto == null) {
            throw new IllegalArgumentException(
                    "El producto es obligatorio."
            );
        }

        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero."
            );
        }

        this.producto = producto;
        this.cantidad = cantidad;
    }

    public void aumentarCantidad() {
        cantidad++;
    }

    public void disminuirCantidad() {
        if (cantidad > 1) {
            cantidad--;
        }
    }

    public double calcularSubtotal() {
        return producto.getPrecio() * cantidad;
    }

    public Producto getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }
}
