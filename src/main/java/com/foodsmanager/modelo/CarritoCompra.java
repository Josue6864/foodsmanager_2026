/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.foodsmanager.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author certe
 */
public class CarritoCompra {

    private static final CarritoCompra INSTANCIA
            = new CarritoCompra();

    private final List<ItemCarrito> items;

    private CarritoCompra() {
        items = new ArrayList<>();
    }

    public static CarritoCompra getInstancia() {
        return INSTANCIA;
    }

    public void agregarProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException(
                    "El producto es obligatorio."
            );
        }

        for (ItemCarrito item : items) {
            if (esMismoProducto(
                    item.getProducto(),
                    producto
            )) {
                item.aumentarCantidad();
                return;
            }
        }

        items.add(new ItemCarrito(producto));
    }

    private boolean esMismoProducto(
            Producto primero,
            Producto segundo
    ) {
        return primero.getIdProducto()
                == segundo.getIdProducto();
    }

    public void eliminarProducto(ItemCarrito item) {
        items.remove(item);
    }

    public void vaciar() {
        items.clear();
    }

    public List<ItemCarrito> getItems() {
        return Collections.unmodifiableList(items);
    }

    public int getCantidadTotal() {
        int cantidadTotal = 0;

        for (ItemCarrito item : items) {
            cantidadTotal += item.getCantidad();
        }

        return cantidadTotal;
    }

    public double calcularTotal() {
        double total = 0;

        for (ItemCarrito item : items) {
            total += item.calcularSubtotal();
        }

        return total;
    }

    public boolean estaVacio() {
        return items.isEmpty();
    }

}
