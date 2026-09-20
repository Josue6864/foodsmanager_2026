package com.foodsmanager.controlador;

import com.foodsmanager.modelo.Producto;
import com.foodsmanager.modelo.Restaurante;
import com.foodsmanager.seguridad.SesionAdministrador;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/** Controla el formulario de registro existente: administrador.fxml. */
public class ControladorGestionProductos {

    private final ControladorProducto controladorProducto;

    @FXML private ComboBox<Restaurante> comboRestaurante;
    @FXML private TextField campoNombre;
    @FXML private TextArea campoDescripcion;
    @FXML private TextField campoPrecio;
    @FXML private CheckBox campoDisponible;
    @FXML private Button botonRegistrar;
    @FXML private Button botonVolver;
    @FXML private Label etiquetaMensaje;

    public ControladorGestionProductos() {
        this(new ControladorProducto());
    }

    public ControladorGestionProductos(ControladorProducto controladorProducto) {
        this.controladorProducto = Objects.requireNonNull(
                controladorProducto, "ControladorProducto es obligatorio.");
    }

    @FXML
    private void initialize() {
        if (!comprobarSesion()) {
            return;
        }
        comboRestaurante.setCellFactory(lista -> crearCeldaRestaurante());
        comboRestaurante.setButtonCell(crearCeldaRestaurante());
        try {
            List<Restaurante> restaurantes = controladorProducto.listarRestaurantesParaRegistro();
            comboRestaurante.getItems().setAll(restaurantes);
            boolean sinRestaurantes = restaurantes.isEmpty();
            comboRestaurante.setDisable(sinRestaurantes);
            botonRegistrar.setDisable(sinRestaurantes);
            if (sinRestaurantes) {
                mostrarMensaje("No hay restaurantes registrados para asociar el producto.", true);
            }
        } catch (SQLException excepcion) {
            comboRestaurante.setDisable(true);
            botonRegistrar.setDisable(true);
            mostrarMensaje("No se pudieron cargar los restaurantes. Vuelva a abrir el formulario.", true);
            excepcion.printStackTrace();
        }
    }

    private ListCell<Restaurante> crearCeldaRestaurante() {
        return new ListCell<Restaurante>() {
            @Override
            protected void updateItem(Restaurante restaurante, boolean vacia) {
                super.updateItem(restaurante, vacia);
                setText(vacia || restaurante == null ? null : restaurante.getNombre());
                setGraphic(null);
            }
        };
    }

    @FXML
    private void registrarDesdeFormulario() {
        if (!comprobarSesion()) {
            return;
        }
        try {
            Restaurante restaurante = comboRestaurante.getValue();
            if (restaurante == null) {
                throw new IllegalArgumentException("Seleccione un restaurante.");
            }
            Producto producto = controladorProducto.registrarProducto(
                    restaurante.getIdRestaurante(), campoNombre.getText(),
                    campoDescripcion.getText(), campoPrecio.getText(), campoDisponible.isSelected());
            campoNombre.clear();
            campoDescripcion.clear();
            campoPrecio.clear();
            campoDisponible.setSelected(true);
            mostrarMensaje("Producto registrado correctamente. Id: " + producto.getIdProducto(), false);
            campoNombre.requestFocus();
        } catch (IllegalArgumentException excepcion) {
            mostrarMensaje(excepcion.getMessage(), true);
        } catch (SQLException excepcion) {
            mostrarMensaje("No se pudo registrar el producto. Revise la conexión con la base de datos.", true);
            excepcion.printStackTrace();
        }
    }

    @FXML
    private void volverAProductos(ActionEvent evento) {
        if (comprobarSesion()) {
            NavegadorVistas.cambiarVista(evento,
                    "/com/foodsmanager/vista/gestion-productos.fxml",
                    "FoodsManager - Gestionar productos");
        }
    }

    private boolean comprobarSesion() {
        if (SesionAdministrador.haySesionActiva()) {
            return true;
        }
        bloquearFormulario();
        mostrarMensaje("Debe iniciar sesión como administrador.", true);
        return false;
    }

    private void bloquearFormulario() {
        comboRestaurante.setDisable(true);
        campoNombre.setDisable(true);
        campoDescripcion.setDisable(true);
        campoPrecio.setDisable(true);
        campoDisponible.setDisable(true);
        botonRegistrar.setDisable(true);
        botonVolver.setDisable(true);
    }

    @FXML
    private void cerrarSesion(ActionEvent evento) {
        SesionAdministrador.cerrar();
        bloquearFormulario();
        NavegadorVistas.cambiarVista(evento,
                "/com/foodsmanager/vista/login.fxml",
                "FoodsManager - Acceso administrativo");
    }

    private void mostrarMensaje(String mensaje, boolean error) {
        etiquetaMensaje.setText(mensaje);
        etiquetaMensaje.setStyle(error ? "-fx-text-fill: #B91C1C;" : "-fx-text-fill: #2F5D50;");
    }
}