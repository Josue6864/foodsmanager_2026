/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.foodsmanager.controlador;

import com.foodsmanager.modelo.ItemCarrito;
import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
/**
 *
 * @author certe
 */
public class CardCarritoController {
    @FXML
    private ImageView imgProducto;

    @FXML
    private Label lblNombre;

    @FXML
    private Label lblDescripcion;

    @FXML
    private Label lblPrecio;

    @FXML
    private Label lblCantidad;

    @FXML
    private Label lblSubtotal;

    @FXML
    private Button btnRestar;

    @FXML
    private Button btnSumar;

    @FXML
    private Button btnEliminar;

    private ItemCarrito item;

    /*
     * Se ejecutará cuando cambie la cantidad.
     * Permitirá que CarritoController actualice el total.
     */
    private Runnable alCambiarCantidad;

    /*
     * Recibe el producto que debe eliminarse.
     */
    private Consumer<ItemCarrito> alEliminar;

    public void configurar(
            ItemCarrito item,
            Runnable alCambiarCantidad,
            Consumer<ItemCarrito> alEliminar
    ) {
        this.item = item;
        this.alCambiarCantidad = alCambiarCantidad;
        this.alEliminar = alEliminar;

        mostrarDatos();
    }

    private void mostrarDatos() {
        if (item == null) {
            return;
        }

        lblNombre.setText(item.getProducto().getNombre());
        lblDescripcion.setText(item.getProducto().getDescripcion());

        lblPrecio.setText(
                String.format(
                        "Precio: Q %.2f",
                        item.getProducto().getPrecio()
                )
        );

        actualizarCantidadYSubtotal();

        /*
         * Descomenta esta parte si Producto tiene getRutaImagen().
         */
        /*
        String rutaImagen = item.getProducto().getRutaImagen();

        if (rutaImagen != null && !rutaImagen.isBlank()) {
            try {
                Image imagen = new Image(rutaImagen, true);
                imgProducto.setImage(imagen);
            } catch (Exception excepcion) {
                System.err.println(
                        "No se pudo cargar la imagen: " + rutaImagen
                );
            }
        }
        */
    }

    @FXML
    private void aumentarCantidad() {
        if (item == null) {
            return;
        }

        item.aumentarCantidad();
        actualizarCantidadYSubtotal();
        notificarCambio();
    }

    @FXML
    private void disminuirCantidad() {
        if (item == null) {
            return;
        }

        item.disminuirCantidad();
        actualizarCantidadYSubtotal();
        notificarCambio();
    }

    @FXML
    private void eliminarProducto() {
        if (item != null && alEliminar != null) {
            alEliminar.accept(item);
        }
    }

    private void actualizarCantidadYSubtotal() {
        lblCantidad.setText(
                String.valueOf(item.getCantidad())
        );

        lblSubtotal.setText(
                String.format(
                        "Subtotal: Q %.2f",
                        item.calcularSubtotal()
                )
        );

        // No permite restar cuando solo queda una unidad.
        btnRestar.setDisable(item.getCantidad() <= 1);
    }

    private void notificarCambio() {
        if (alCambiarCantidad != null) {
            alCambiarCantidad.run();
        }
    }
}
