package com.foodsmanager.controlador;

import com.foodsmanager.modelo.Restaurante;
import com.foodsmanager.persistencia.RestauranteDAO;
import com.foodsmanager.seguridad.SesionUsuarioEstandar;
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
    @FXML private Button botonCliente;

    @FXML
    private void initialize() {
        botonCliente.setText(SesionUsuarioEstandar.haySesionActiva()
                ? "Mis pedidos" : "Iniciar sesión como cliente");
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

    @FXML
    private void abrirCarrito(ActionEvent evento) {
        NavegadorVistas.cambiarVista(
                evento,
                "/com/foodsmanager/vista/carrito.fxml",
                "FoodsManager - Carrito"
        );
    }

    private VBox crearTarjeta(Restaurante restaurante) {
    Label nombre = new Label(restaurante.getNombre());
    nombre.getStyleClass().add("nombre-restaurante");
    nombre.setWrapText(true);
    nombre.setMinHeight(Region.USE_PREF_SIZE);
    nombre.setMaxWidth(Double.MAX_VALUE);
    nombre.setStyle("-fx-text-fill: #111827; -fx-font-weight: bold;");

    Label ubicacion = new Label(restaurante.getUbicacion());
    ubicacion.getStyleClass().add("ubicacion-restaurante");
    ubicacion.setWrapText(true);
    ubicacion.setMinHeight(Region.USE_PREF_SIZE);
    ubicacion.setMaxWidth(Double.MAX_VALUE);
    ubicacion.setStyle("-fx-text-fill: #4338ca; -fx-font-weight: bold;");

    Label descripcion = new Label(restaurante.getDescripcion().isEmpty()
            ? "Descripción pendiente." : restaurante.getDescripcion());
    descripcion.getStyleClass().add("descripcion-restaurante");
    descripcion.setWrapText(true);
    descripcion.setMinHeight(Region.USE_PREF_SIZE);
    descripcion.setMaxWidth(Double.MAX_VALUE);
    descripcion.setStyle("-fx-text-fill: #6b7280;");

    Region espacio = new Region();
    VBox.setVgrow(espacio, Priority.ALWAYS);

    Button botonProductos = new Button("Ver productos");
    botonProductos.getStyleClass().add("boton-principal");
    botonProductos.setMaxWidth(Double.MAX_VALUE);
    botonProductos.setStyle("-fx-background-color: linear-gradient(to bottom right, #4f46e5, #4338ca);"
            + " -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12;"
            + " -fx-padding: 10 20 10 20; -fx-cursor: hand;"
            + " -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
    botonProductos.setOnAction(evento -> abrirProductos(
            evento, restaurante.getIdRestaurante()));

    VBox tarjeta = new VBox(14, nombre, ubicacion, descripcion, espacio, botonProductos);
    tarjeta.getStyleClass().add("tarjeta-restaurante");
    tarjeta.setMinWidth(250);
    tarjeta.setPrefWidth(250);
    tarjeta.setMaxWidth(250);
    tarjeta.setMinHeight(230);

    final String estiloNormal = "-fx-background-color: white; -fx-background-radius: 24;"
            + " -fx-border-color: #e5e7eb; -fx-border-radius: 24; -fx-border-width: 1;"
            + " -fx-effect: dropshadow(gaussian, rgba(79,70,229,0.12), 18, 0.1, 0, 5);";
    final String estiloResaltado = "-fx-background-color: white; -fx-background-radius: 24;"
            + " -fx-border-color: #a5b4fc; -fx-border-radius: 24; -fx-border-width: 1;"
            + " -fx-effect: dropshadow(gaussian, rgba(79,70,229,0.20), 20, 0.1, 0, 6);";
    tarjeta.setStyle(estiloNormal);
    tarjeta.setOnMouseEntered(e -> tarjeta.setStyle(estiloResaltado));
    tarjeta.setOnMouseExited(e -> tarjeta.setStyle(estiloNormal));
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
    private void abrirAccesoCliente(ActionEvent evento) {
        boolean autenticado = SesionUsuarioEstandar.haySesionActiva();
        NavegadorVistas.cambiarVista(evento,
                autenticado ? "/com/foodsmanager/vista/mis-pedidos.fxml"
                        : "/com/foodsmanager/vista/acceso-usuario.fxml",
                autenticado ? "FoodsManager - Mis pedidos" : "FoodsManager - Acceso de cliente");
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
