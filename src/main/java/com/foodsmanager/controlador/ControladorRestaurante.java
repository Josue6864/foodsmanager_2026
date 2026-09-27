package com.foodsmanager.controlador;

import com.foodsmanager.modelo.Restaurante;
import com.foodsmanager.persistencia.RestauranteDAO;
import java.sql.SQLException;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

public class ControladorRestaurante {

    private final RestauranteDAO restauranteDAO = new RestauranteDAO();

    @FXML private TilePane contenedorRestaurantes;
    @FXML private Label etiquetaMensaje;

    @FXML
    private void initialize() {
        cargarRestaurantes();
    }

    @FXML
    private void cargarRestaurantes() {
        contenedorRestaurantes.getChildren().clear();
        mostrarMensaje("", false);

        try {
            List<Restaurante> restaurantes = restauranteDAO.listarTodos();
            if (restaurantes.isEmpty()) {
                mostrarMensaje("Todavía no hay restaurantes registrados.", false);
                return;
            }

            for (Restaurante restaurante : restaurantes) {
                contenedorRestaurantes.getChildren().add(crearTarjeta(restaurante));
            }
        } catch (SQLException excepcion) {
            mostrarMensaje("No se pudieron cargar los restaurantes. Intenta actualizar.", true);
            excepcion.printStackTrace();
        }
    }

    private VBox crearTarjeta(Restaurante restaurante) {
        Label nombre = new Label(restaurante.getNombre());
        nombre.getStyleClass().add("nombre-restaurante");
        nombre.setWrapText(true);
        nombre.setMinHeight(Region.USE_PREF_SIZE);
        nombre.setMaxWidth(Double.MAX_VALUE);

        Label ubicacion = new Label(restaurante.getUbicacion());
        ubicacion.getStyleClass().add("ubicacion-restaurante");
        ubicacion.setWrapText(true);
        ubicacion.setMinHeight(Region.USE_PREF_SIZE);
        ubicacion.setMaxWidth(Double.MAX_VALUE);

        Label descripcion = new Label(restaurante.getDescripcion().isEmpty()
                ? "Descripción pendiente." : restaurante.getDescripcion());
        descripcion.getStyleClass().add("descripcion-restaurante");
        descripcion.setWrapText(true);
        descripcion.setMinHeight(Region.USE_PREF_SIZE);
        descripcion.setMaxWidth(Double.MAX_VALUE);

        Region espacio = new Region();
        VBox.setVgrow(espacio, Priority.ALWAYS);

        Button botonProductos = new Button("Ver productos");
        botonProductos.getStyleClass().add("boton-principal");
        botonProductos.setMaxWidth(Double.MAX_VALUE);
        botonProductos.setOnAction(evento -> abrirProductos(
                evento, restaurante.getIdRestaurante()));

        VBox tarjeta = new VBox(14, nombre, ubicacion, descripcion, espacio, botonProductos);
        tarjeta.getStyleClass().add("tarjeta-restaurante");
        tarjeta.setMinWidth(250);
        tarjeta.setPrefWidth(250);
        tarjeta.setMaxWidth(250);
        tarjeta.setMinHeight(230);
        return tarjeta;
    }

    private void mostrarMensaje(String mensaje, boolean error) {
        etiquetaMensaje.setText(mensaje);
        etiquetaMensaje.getStyleClass().removeAll("mensaje-error", "subtitulo");
        etiquetaMensaje.getStyleClass().add(error ? "mensaje-error" : "subtitulo");
        etiquetaMensaje.setVisible(!mensaje.isEmpty());
        etiquetaMensaje.setManaged(!mensaje.isEmpty());
    }

    @FXML
    private void abrirLogin(ActionEvent evento) {
        NavegadorVistas.cambiarVista(evento,
                "/com/foodsmanager/vista/login.fxml",
                "FoodsManager - Acceso administrativo");
    }

    private void abrirProductos(ActionEvent evento, int idRestaurante) {
        NavegadorVistas.cambiarVistaProductos(evento,
                "/com/foodsmanager/vista/productos.fxml",
                "FoodsManager - Productos",
                idRestaurante);
    }
}
