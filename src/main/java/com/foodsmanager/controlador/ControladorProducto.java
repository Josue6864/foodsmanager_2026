package com.foodsmanager.controlador;

import com.foodsmanager.modelo.Producto;
import com.foodsmanager.modelo.Restaurante;
import com.foodsmanager.persistencia.ProductoDAO;
import com.foodsmanager.persistencia.RestauranteDAO;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Coordina las operaciones relacionadas con los productos y el formulario
 * administrativo de registro.
 */
public class ControladorProducto {

    private final ProductoDAO productoDAO;
    private final RestauranteDAO restauranteDAO;

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

    public ControladorProducto() {
        this(
                new ProductoDAO(),
                new RestauranteDAO()
        );
    }

    public ControladorProducto(
            ProductoDAO productoDAO,
            RestauranteDAO restauranteDAO) {

        this.productoDAO = Objects.requireNonNull(
                productoDAO,
                "ProductoDAO es obligatorio."
        );

        this.restauranteDAO = Objects.requireNonNull(
                restauranteDAO,
                "RestauranteDAO es obligatorio."
        );
    }

    /**
     * Se ejecuta automáticamente después de cargar el FXML.
     */
    @FXML
    private void initialize() {
        campoDisponible.setSelected(true);
        cargarRestaurantes();
    }

    public List<Restaurante> listarRestaurantes()
            throws SQLException {

        return restauranteDAO.listarTodos();
    }

    public Producto registrarProducto(
            int idRestaurante,
            String nombre,
            String descripcion,
            String precioTexto,
            boolean disponible) throws SQLException {

        double precio = convertirPrecio(precioTexto);

        return registrarProducto(
                idRestaurante,
                nombre,
                descripcion,
                precio,
                disponible
        );
    }

    public Producto registrarProducto(
            int idRestaurante,
            String nombre,
            String descripcion,
            double precio,
            boolean disponible) throws SQLException {

        Producto producto = new Producto(
                idRestaurante,
                nombre,
                descripcion,
                precio,
                disponible
        );

        productoDAO.insertar(producto);

        return producto;
    }

    @FXML
    private void registrarDesdeFormulario() {
        Restaurante restaurante
                = comboRestaurante.getValue();

        if (restaurante == null) {
            mostrarError(
                    "Debe seleccionar un restaurante.");
            return;
        }

        try {
            Producto producto = registrarProducto(
                    restaurante.getIdRestaurante(),
                    campoNombre.getText(),
                    campoDescripcion.getText(),
                    campoPrecio.getText(),
                    campoDisponible.isSelected()
            );

            mostrarExito(
                    "Producto registrado correctamente. ID: "
                    + producto.getIdProducto()
            );

            limpiarFormulario();
        } catch (IllegalArgumentException excepcion) {
            mostrarError(excepcion.getMessage());
        } catch (SQLException excepcion) {
            mostrarError(
                    "No se pudo registrar el producto en la base de datos."
            );

            excepcion.printStackTrace();
        }
    }

    private void cargarRestaurantes() {
        try {
            List<Restaurante> restaurantes
                    = listarRestaurantes();

            comboRestaurante.setItems(
                    FXCollections.observableArrayList(
                            restaurantes
                    )
            );

            if (restaurantes.isEmpty()) {
                botonRegistrar.setDisable(true);
                mostrarError(
                        "No existen restaurantes disponibles.");
            } else {
                comboRestaurante
                        .getSelectionModel()
                        .selectFirst();
            }
        } catch (SQLException excepcion) {
            botonRegistrar.setDisable(true);

            mostrarError(
                    "No se pudieron cargar los restaurantes."
            );

            excepcion.printStackTrace();
        }
    }

    private double convertirPrecio(String precioTexto) {
        if (precioTexto == null
                || precioTexto.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El precio del producto es obligatorio.");
        }

        String precioNormalizado = precioTexto
                .trim()
                .replace(',', '.');

        try {
            double precio
                    = Double.parseDouble(precioNormalizado);

            if (!Double.isFinite(precio) || precio < 0) {
                throw new IllegalArgumentException(
                        "El precio debe ser un número mayor o igual a cero.");
            }

            return precio;
        } catch (NumberFormatException excepcion) {
            throw new IllegalArgumentException(
                    "El precio ingresado no es válido.",
                    excepcion
            );
        }
    }

    private void limpiarFormulario() {
        campoNombre.clear();
        campoDescripcion.clear();
        campoPrecio.clear();
        campoDisponible.setSelected(true);
        campoNombre.requestFocus();
    }

    private void mostrarExito(String mensaje) {
        etiquetaMensaje.setStyle(
                "-fx-text-fill: #157347;");
        etiquetaMensaje.setText(mensaje);
    }

    private void mostrarError(String mensaje) {
        etiquetaMensaje.setStyle(
                "-fx-text-fill: #b02a37;");
        etiquetaMensaje.setText(mensaje);
    }
}
