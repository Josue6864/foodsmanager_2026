/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.foodsmanager.controlador;

import com.foodsmanager.modelo.CarritoCompra;
import com.foodsmanager.modelo.DetallePedido;
import com.foodsmanager.modelo.ItemCarrito;
import com.foodsmanager.modelo.Pedido;
import com.foodsmanager.persistencia.PedidoDAO;

import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 *
 * @author certe
 */
public class DatosPedidoController {

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtTelefono;

    @FXML
    private TextField txtPersonaRecoge;

    @FXML
    private ComboBox<String> cmbHoraRecogida;

    @FXML
    private TextArea txtIndicaciones;

    @FXML
    private Button btnRegresarCarrito;

    @FXML
    private Button btnConfirmarPago;

    private final List<ItemCarrito> items =
            new ArrayList<>();

    private final PedidoDAO pedidoDAO =
            new PedidoDAO();

    private final CarritoCompra carrito =
            CarritoCompra.getInstancia();

    private double total;

    @FXML
    private void initialize() {
        cargarHorarios();

        /*
         * Permite escribir solamente números
         * y un máximo de ocho dígitos.
         */
        txtTelefono.textProperty()
                .addListener((
                        observable,
                        textoAnterior,
                        textoNuevo
                ) -> {
                    if (!textoNuevo.matches("\\d{0,8}")) {
                        txtTelefono.setText(textoAnterior);
                    }
                });
    }

    public void establecerPedido(
            List<ItemCarrito> items,
            double total
    ) {
        this.items.clear();

        if (items != null) {
            this.items.addAll(items);
        }

        this.total = total;
    }

    private void cargarHorarios() {
        cmbHoraRecogida.getItems().clear();

        LocalTime primeraHora = LocalTime.now()
                .plusMinutes(30)
                .withSecond(0)
                .withNano(0);

        int minutosRestantes =
                primeraHora.getMinute() % 15;

        if (minutosRestantes != 0) {
            primeraHora = primeraHora.plusMinutes(
                    15 - minutosRestantes
            );
        }

        DateTimeFormatter formato =
                DateTimeFormatter.ofPattern("HH:mm");

        for (int i = 0; i < 12; i++) {
            LocalTime horaDisponible =
                    primeraHora.plusMinutes(i * 15L);

            cmbHoraRecogida.getItems().add(
                    horaDisponible.format(formato)
            );
        }
    }

    @FXML
    private void volverCarrito(ActionEvent evento) {
        NavegadorVistas.cambiarVista(
                evento,
                "/com/foodsmanager/vista/carrito.fxml",
                "FoodsManager - Carrito"
        );
    }

    @FXML
    private void confirmarPedido(ActionEvent evento) {
        limpiarEstilosDeError();

        if (!validarFormulario()) {
            return;
        }

        if (items.isEmpty()) {
            mostrarAdvertencia(
                    "Carrito vacío",
                    "No hay productos para registrar."
            );
            return;
        }

        String nombre =
                txtNombre.getText().trim();

        String telefono =
                txtTelefono.getText().trim();

        String personaRecoge =
                txtPersonaRecoge.getText().trim();

        String horaRecogida =
                cmbHoraRecogida.getValue();

        String indicaciones =
                txtIndicaciones.getText().trim();

        Pedido pedido = new Pedido(
                nombre,
                telefono,
                personaRecoge,
                horaRecogida,
                indicaciones
        );

        for (ItemCarrito item : items) {
            DetallePedido detalle =
                    new DetallePedido(
                            item.getProducto(),
                            item.getCantidad(),
                            item.getProducto().getPrecio()
                    );

            pedido.agregarDetalle(detalle);
        }

        btnConfirmarPago.setDisable(true);

        try {
            int idPedido =
                    pedidoDAO.registrarPedido(pedido);

            carrito.vaciar();

            mostrarConfirmacion(
                    idPedido,
                    pedido.getTotal()
            );

            NavegadorVistas.cambiarVista(
                    evento,
                    "/com/foodsmanager/vista/restaurantes.fxml",
                    "FoodsManager - Restaurantes"
            );

        } catch (SQLException excepcion) {
            btnConfirmarPago.setDisable(false);

            mostrarError(
                    "No se pudo registrar el pedido.\n"
                    + excepcion.getMessage()
            );

            excepcion.printStackTrace();
        }
    }

    private boolean validarFormulario() {
        String nombre =
                txtNombre.getText().trim();

        String telefono =
                txtTelefono.getText().trim();

        String personaRecoge =
                txtPersonaRecoge.getText().trim();

        if (nombre.isEmpty()) {
            marcarError(txtNombre);

            mostrarAdvertencia(
                    "Nombre obligatorio",
                    "Ingrese el nombre completo del cliente."
            );

            txtNombre.requestFocus();
            return false;
        }

        if (!telefono.matches("\\d{8}")) {
            marcarError(txtTelefono);

            mostrarAdvertencia(
                    "Teléfono incorrecto",
                    "El número de teléfono debe contener "
                    + "exactamente 8 dígitos."
            );

            txtTelefono.requestFocus();
            return false;
        }

        if (personaRecoge.isEmpty()) {
            marcarError(txtPersonaRecoge);

            mostrarAdvertencia(
                    "Nombre obligatorio",
                    "Ingrese el nombre de la persona "
                    + "que recogerá el pedido."
            );

            txtPersonaRecoge.requestFocus();
            return false;
        }

        if (cmbHoraRecogida.getValue() == null) {
            cmbHoraRecogida.setStyle(
                    "-fx-border-color: #B3261E;"
                    + "-fx-border-width: 2px;"
            );

            mostrarAdvertencia(
                    "Hora obligatoria",
                    "Seleccione una hora aproximada "
                    + "para recoger el pedido."
            );

            cmbHoraRecogida.requestFocus();
            return false;
        }

        return true;
    }

    private void marcarError(TextField campo) {
        campo.setStyle(
                "-fx-border-color: #B3261E;"
                + "-fx-border-width: 2px;"
        );
    }

    private void limpiarEstilosDeError() {
        txtNombre.setStyle("");
        txtTelefono.setStyle("");
        txtPersonaRecoge.setStyle("");
        cmbHoraRecogida.setStyle("");
    }

    private void mostrarConfirmacion(
            int idPedido,
            double totalPagado
    ) {
        Alert alerta = new Alert(
                Alert.AlertType.INFORMATION
        );

        alerta.setTitle("Pedido confirmado");

        alerta.setHeaderText(
                "El pago fue registrado correctamente"
        );

        alerta.setContentText(
                "Número de pedido: " + idPedido
                + "\nTotal pagado: "
                + String.format(
                        "Q %.2f",
                        totalPagado
                )
        );

        alerta.showAndWait();
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
                "No se pudo registrar el pedido"
        );

        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
