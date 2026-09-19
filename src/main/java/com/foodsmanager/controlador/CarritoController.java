/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.foodsmanager.controlador;

import com.foodsmanager.modelo.CarritoCompra;
import com.foodsmanager.modelo.ItemCarrito;

import java.io.IOException;
import java.util.ArrayList;

import com.foodsmanager.modelo.ContextoNavegacion;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author certe
 */
public class CarritoController {

    @FXML
    private ScrollPane scrollCarrito;

    @FXML
    private TilePane contenedorCarrito;

    @FXML
    private Label lblTotal;

    @FXML
    private Label lblCarritoVacio;

    @FXML
    private Button btnVaciarCarrito;

    @FXML
    private Button btnContinuarPago;

    private final CarritoCompra carrito = CarritoCompra.getInstancia();

    @FXML
    private ComboBox<String> cmbNavegacion;

    private final ContextoNavegacion contextoNavegacion = ContextoNavegacion.getInstancia();

    @FXML
    private void initialize() {
        configurarMenuNavegacion();
        configurarContenedor();
        configurarTamanoTarjetas();
        actualizarVista();
    }

    private void abrirRestaurantes(ActionEvent evento) {
        NavegadorVistas.cambiarVista(
                evento,
                "/com/foodsmanager/vista/restaurantes.fxml",
                "FoodsManager - Restaurantes"
        );
    }

    private void abrirUltimoRestaurante() {
        if (!contextoNavegacion
                .tieneRestauranteSeleccionado()) {

            mostrarAdvertencia(
                    "Restaurante no seleccionado",
                    "Primero debes seleccionar un restaurante."
            );

            cmbNavegacion.setValue(null);
            return;
        }

        int idRestaurante
                = contextoNavegacion
                        .getIdUltimoRestaurante();

        try {
            FXMLLoader cargador = new FXMLLoader(
                    getClass().getResource(
                            "/com/foodsmanager/vista/productos.fxml"
                    )
            );

            Parent raiz = cargador.load();

            ControladorProducto controlador
                    = cargador.getController();

            controlador.cargarProductosPorRestaurante(
                    idRestaurante
            );

            Stage ventana = (Stage) cmbNavegacion
                    .getScene()
                    .getWindow();

            ventana.setScene(new Scene(raiz));

            ventana.setTitle(
                    "FoodsManager - Productos"
            );

            ventana.show();

        } catch (IOException excepcion) {
            mostrarError(
                    "No se pudo abrir el menú "
                    + "del restaurante.\n"
                    + excepcion.getMessage()
            );

            excepcion.printStackTrace();
            cmbNavegacion.setValue(null);
        }
    }

    @FXML
    private void navegarDesdeMenu(ActionEvent evento) {
        String opcion
                = cmbNavegacion.getValue();

        if (opcion == null) {
            return;
        }

        switch (opcion) {
            case "Restaurantes" ->
                abrirRestaurantes(evento);

            case "Menú del último restaurante" ->
                abrirUltimoRestaurante();

            default ->
                cmbNavegacion.setValue(null);
        }
    }

    private void configurarMenuNavegacion() {
        cmbNavegacion.getItems().clear();

        cmbNavegacion.getItems().addAll(
                "Restaurantes",
                "Menú del último restaurante"
        );

        cmbNavegacion.setPromptText("Ir a...");
    }

    private void configurarContenedor() {
        contenedorCarrito.setHgap(18);
        contenedorCarrito.setVgap(18);
        contenedorCarrito.setPadding(
                new Insets(20)
        );

        contenedorCarrito.setPrefColumns(3);
        contenedorCarrito.setAlignment(
                Pos.TOP_CENTER
        );

        if (scrollCarrito != null) {
            scrollCarrito.setFitToWidth(true);
            scrollCarrito.setHbarPolicy(
                    ScrollPane.ScrollBarPolicy.NEVER
            );

            scrollCarrito.setVbarPolicy(
                    ScrollPane.ScrollBarPolicy.AS_NEEDED
            );
        }
    }

    private void configurarTamanoTarjetas() {
        if (scrollCarrito == null) {
            return;
        }

        scrollCarrito.viewportBoundsProperty()
                .addListener((
                        observable,
                        limitesAnteriores,
                        limitesNuevos) -> {
                    double anchoDisponible
                            = limitesNuevos.getWidth();

                    double paddingHorizontal = 40;
                    double separaciones = 36;

                    double anchoTarjeta
                            = (anchoDisponible
                            - paddingHorizontal
                            - separaciones) / 3.0;

                    if (anchoTarjeta < 240) {
                        anchoTarjeta = 240;
                    }

                    contenedorCarrito.setPrefTileWidth(
                            anchoTarjeta
                    );
                });
    }

    private void actualizarVista() {
        if (contenedorCarrito == null) {
            return;
        }

        contenedorCarrito.getChildren().clear();

        for (ItemCarrito item : carrito.getItems()) {
            VBox tarjeta = crearTarjetaCarrito(item);

            contenedorCarrito
                    .getChildren()
                    .add(tarjeta);
        }

        actualizarTotal();
        actualizarEstadoBotones();
    }

    private VBox crearTarjetaCarrito(
            ItemCarrito item
    ) {
        Label nombre = new Label(
                item.getProducto().getNombre()
        );

        nombre.getStyleClass().add(
                "nombre-producto-carrito"
        );

        nombre.setWrapText(true);

        Label descripcion = new Label(
                item.getProducto().getDescripcion()
        );

        descripcion.getStyleClass().add(
                "descripcion-producto-carrito"
        );

        descripcion.setWrapText(true);
        descripcion.setMaxWidth(Double.MAX_VALUE);

        Label precio = new Label(
                String.format(
                        "Precio: Q %.2f",
                        item.getProducto().getPrecio()
                )
        );

        precio.getStyleClass().add(
                "precio-producto-carrito"
        );

        Label lblCantidad = new Label(
                String.valueOf(item.getCantidad())
        );

        lblCantidad.getStyleClass().add(
                "cantidad-producto"
        );

        Label lblSubtotal = new Label(
                String.format(
                        "Subtotal: Q %.2f",
                        item.calcularSubtotal()
                )
        );

        lblSubtotal.getStyleClass().add(
                "subtotal-producto-carrito"
        );

        Button btnRestar = new Button("−");

        btnRestar.getStyleClass().add(
                "boton-cantidad"
        );

        btnRestar.setDisable(
                item.getCantidad() <= 1
        );

        Button btnSumar = new Button("+");

        btnSumar.getStyleClass().add(
                "boton-cantidad"
        );

        Button btnEliminar = new Button(
                "Eliminar"
        );

        btnEliminar.getStyleClass().add(
                "boton-eliminar"
        );

        HBox selectorCantidad = new HBox(
                10,
                btnRestar,
                lblCantidad,
                btnSumar
        );

        selectorCantidad.setAlignment(
                Pos.CENTER
        );

        selectorCantidad.getStyleClass().add(
                "selector-cantidad"
        );

        Region espacio = new Region();

        VBox.setVgrow(
                espacio,
                Priority.ALWAYS
        );

        btnSumar.setOnAction(evento -> {
            item.aumentarCantidad();

            actualizarInformacionTarjeta(
                    item,
                    lblCantidad,
                    lblSubtotal,
                    btnRestar
            );

            actualizarTotal();
        });

        btnRestar.setOnAction(evento -> {
            item.disminuirCantidad();

            actualizarInformacionTarjeta(
                    item,
                    lblCantidad,
                    lblSubtotal,
                    btnRestar
            );

            actualizarTotal();
        });

        btnEliminar.setOnAction(evento -> {
            confirmarEliminacion(item);
        });

        VBox tarjeta = new VBox(
                10,
                nombre,
                descripcion,
                precio,
                espacio,
                selectorCantidad,
                lblSubtotal,
                btnEliminar
        );

        tarjeta.getStyleClass().add(
                "card-carrito"
        );

        tarjeta.setPadding(
                new Insets(16)
        );

        tarjeta.setPrefHeight(240);
        tarjeta.setMinHeight(240);
        tarjeta.setMaxHeight(240);

        tarjeta.setMaxWidth(
                Double.MAX_VALUE
        );

        return tarjeta;
    }

    private void actualizarInformacionTarjeta(
            ItemCarrito item,
            Label lblCantidad,
            Label lblSubtotal,
            Button btnRestar
    ) {
        lblCantidad.setText(
                String.valueOf(item.getCantidad())
        );

        lblSubtotal.setText(
                String.format(
                        "Subtotal: Q %.2f",
                        item.calcularSubtotal()
                )
        );

        btnRestar.setDisable(
                item.getCantidad() <= 1
        );
    }

    private void confirmarEliminacion(
            ItemCarrito item
    ) {
        Alert alerta = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        alerta.setTitle("Eliminar producto");

        alerta.setHeaderText(
                "¿Deseas eliminar "
                + item.getProducto().getNombre()
                + "?"
        );

        alerta.setContentText(
                "Se eliminarán todas las unidades "
                + "de este producto."
        );

        alerta.showAndWait().ifPresent(
                respuesta -> {
                    if (respuesta == ButtonType.OK) {
                        carrito.eliminarProducto(item);
                        actualizarVista();
                    }
                }
        );
    }

    public void actualizarTotal() {
        if (lblTotal == null) {
            return;
        }

        lblTotal.setText(
                String.format(
                        "Total: Q %.2f",
                        carrito.calcularTotal()
                )
        );
    }

    private void actualizarEstadoBotones() {
        boolean vacio = carrito.estaVacio();

        if (btnVaciarCarrito != null) {
            btnVaciarCarrito.setDisable(vacio);
        }

        if (btnContinuarPago != null) {
            btnContinuarPago.setDisable(vacio);
        }

        if (lblCarritoVacio != null) {
            lblCarritoVacio.setVisible(vacio);
            lblCarritoVacio.setManaged(vacio);
        }
    }

    @FXML
    private void vaciarCarrito() {
        if (carrito.estaVacio()) {
            return;
        }

        Alert alerta = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        alerta.setTitle("Vaciar carrito");

        alerta.setHeaderText(
                "¿Deseas eliminar todos los productos?"
        );

        alerta.setContentText(
                "Esta acción no se puede deshacer."
        );

        alerta.showAndWait().ifPresent(
                respuesta -> {
                    if (respuesta == ButtonType.OK) {
                        carrito.vaciar();
                        actualizarVista();
                    }
                }
        );
    }

    @FXML
    private void continuarPago() {
        if (carrito.estaVacio()) {
            mostrarAdvertencia(
                    "El carrito está vacío",
                    "Agrega al menos un producto."
            );

            return;
        }

        try {
            FXMLLoader cargador = new FXMLLoader(
                    getClass().getResource(
                            "/com/foodsmanager/vista/"
                            + "datos-pedido.fxml"
                    )
            );

            Parent raiz = cargador.load();

            DatosPedidoController controlador
                    = cargador.getController();

            controlador.establecerPedido(
                    new ArrayList<>(
                            carrito.getItems()
                    ),
                    carrito.calcularTotal()
            );

            Stage ventana = (Stage) btnContinuarPago
                    .getScene()
                    .getWindow();

            ventana.setScene(
                    new Scene(raiz)
            );

            ventana.setTitle(
                    "FoodsManager - Datos del pedido"
            );

            ventana.show();

        } catch (IOException excepcion) {
            mostrarError(
                    "No se pudo abrir la vista "
                    + "de datos del pedido.\n"
                    + excepcion.getMessage()
            );

            excepcion.printStackTrace();
        }
    }

    private void mostrarAdvertencia(
            String titulo,
            String mensaje
    ) {
        Alert alerta = new Alert(
                Alert.AlertType.WARNING
        );

        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(
                Alert.AlertType.ERROR
        );

        alerta.setTitle("Error");

        alerta.setHeaderText(
                "No se pudo completar la operación"
        );

        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
