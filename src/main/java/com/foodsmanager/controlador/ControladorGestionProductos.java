package com.foodsmanager.controlador;

import com.foodsmanager.modelo.Producto;
import com.foodsmanager.modelo.Restaurante;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Controla únicamente el formulario de administrador.fxml.
 * Delega las operaciones de datos a la API de ControladorProducto
 * para conservar las llamadas existentes del proyecto.
 */
public class ControladorGestionProductos {

    private final ControladorProducto controladorProducto;

    @FXML
    private ComboBox<Restaurante> comboRestaurante;

    @FXML
    private TextField campoNombre;

    @FXML
    private TextArea campoDescripcion;

    @FXML
    private TextField campoPrecio;

    @FXML
    private CheckBox campoDisponible;

    @FXML
    private Button botonRegistrar;

    @FXML
    private Label etiquetaMensaje;

    public ControladorGestionProductos() {
        this(new ControladorProducto());
    }

    public ControladorGestionProductos(
            ControladorProducto controladorProducto) {
        this.controladorProducto = Objects.requireNonNull(
                controladorProducto,
                "ControladorProducto es obligatorio."
        );
    }

    @FXML
    private void initialize() {
        // Mostrar solo el nombre, sin depender de Restaurante.toString().
        comboRestaurante.setCellFactory(lista -> crearCeldaRestaurante());
        comboRestaurante.setButtonCell(crearCeldaRestaurante());

        try {
            List<Restaurante> restaurantes =
                    controladorProducto.listarRestaurantesParaRegistro();

            comboRestaurante.getItems().setAll(restaurantes);
            boolean sinRestaurantes = restaurantes.isEmpty();
            comboRestaurante.setDisable(sinRestaurantes);
            botonRegistrar.setDisable(sinRestaurantes);

            if (sinRestaurantes) {
                mostrarMensaje(
                        "No hay restaurantes registrados para asociar el producto.",
                        true
                );
            }
        } catch (SQLException excepcion) {
            comboRestaurante.setDisable(true);
            botonRegistrar.setDisable(true);
            mostrarMensaje(
                    "No se pudieron cargar los restaurantes. Vuelva a abrir el formulario.",
                    true
            );
            excepcion.printStackTrace();
        }
    }

    private ListCell<Restaurante> crearCeldaRestaurante() {
        return new ListCell<Restaurante>() {
            @Override
            protected void updateItem(Restaurante restaurante, boolean vacia) {
                super.updateItem(restaurante, vacia);
                setText(vacia || restaurante == null
                        ? null
                        : restaurante.getNombre());
                setGraphic(null);
            }
        };
    }

    @FXML
    private void registrarDesdeFormulario() {
        try {
            Restaurante restaurante = comboRestaurante.getValue();

            if (restaurante == null) {
                throw new IllegalArgumentException(
                        "Seleccione un restaurante."
                );
            }

            Producto producto = controladorProducto.registrarProducto(
                    restaurante.getIdRestaurante(),
                    campoNombre.getText(),
                    campoDescripcion.getText(),
                    campoPrecio.getText(),
                    campoDisponible.isSelected()
            );

            campoNombre.clear();
            campoDescripcion.clear();
            campoPrecio.clear();
            campoDisponible.setSelected(true);

            mostrarMensaje(
                    "Producto registrado correctamente. Id: "
                            + producto.getIdProducto(),
                    false
            );
            campoNombre.requestFocus();

        } catch (IllegalArgumentException excepcion) {
            mostrarMensaje(excepcion.getMessage(), true);
        } catch (SQLException excepcion) {
            mostrarMensaje(
                    "No se pudo registrar el producto. Revise la conexión con la base de datos.",
                    true
            );
            excepcion.printStackTrace();
        }
    }

    private void mostrarMensaje(String mensaje, boolean error) {
        etiquetaMensaje.setText(mensaje);
        etiquetaMensaje.setStyle(
                error
                        ? "-fx-text-fill: #B91C1C;"
                        : "-fx-text-fill: #2F5D50;"
        );
    }
}
