package com.foodsmanager.controlador;

import com.foodsmanager.modelo.Restaurante;
import com.foodsmanager.persistencia.RestauranteDAO;
import com.foodsmanager.seguridad.SesionAdministrador;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ControladorGestionRestaurantes {

    private final RestauranteDAO restauranteDAO;

    @FXML private TableView<Restaurante> tablaRestaurantes;
    @FXML private TableColumn<Restaurante, Integer> columnaId;
    @FXML private TableColumn<Restaurante, String> columnaNombre;
    @FXML private TableColumn<Restaurante, String> columnaUbicacion;
    @FXML private TableColumn<Restaurante, String> columnaDescripcion;
    @FXML private TextField campoNombre;
    @FXML private TextField campoUbicacion;
    @FXML private TextArea campoDescripcion;
    @FXML private Button botonGuardar;
    @FXML private Button botonActualizar;
    @FXML private Button botonProductos;
    @FXML private Button botonEditarDescripcion;
    @FXML private Label etiquetaResumen;
    @FXML private Label etiquetaMensaje;

    public ControladorGestionRestaurantes() {
        this(new RestauranteDAO());
    }

    public ControladorGestionRestaurantes(RestauranteDAO restauranteDAO) {
        this.restauranteDAO = Objects.requireNonNull(
                restauranteDAO, "RestauranteDAO es obligatorio.");
    }

    /** Operación administrativa independiente de los controles de la vista. */
    public int registrarRestaurante(String nombre, String ubicacion)
            throws SQLException {
        return registrarRestaurante(nombre, ubicacion, "");
    }

    public int registrarRestaurante(String nombre, String ubicacion, String descripcion)
            throws SQLException {
        SesionAdministrador.exigirSesion();
        return restauranteDAO.insertar(nombre, ubicacion, descripcion);
    }

    public void actualizarDescripcion(int idRestaurante, String descripcion)
            throws SQLException {
        SesionAdministrador.exigirSesion();
        restauranteDAO.actualizarDescripcion(idRestaurante, descripcion);
    }

    /** El catálogo público usa directamente el DAO, sin exigir una sesión. */
    public List<Restaurante> listarRestaurantes() throws SQLException {
        SesionAdministrador.exigirSesion();
        return restauranteDAO.listarTodos();
    }

    @FXML
    private void initialize() {
        columnaId.setCellValueFactory(datos -> new ReadOnlyObjectWrapper<>(
                datos.getValue().getIdRestaurante()));
        columnaNombre.setCellValueFactory(datos -> new ReadOnlyStringWrapper(
                datos.getValue().getNombre()));
        columnaUbicacion.setCellValueFactory(datos -> new ReadOnlyStringWrapper(
                datos.getValue().getUbicacion()));
        columnaDescripcion.setCellValueFactory(datos -> new ReadOnlyStringWrapper(
                datos.getValue().getDescripcion()));
        tablaRestaurantes.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, seleccionado) -> botonEditarDescripcion.setDisable(
                        seleccionado == null || !SesionAdministrador.haySesionActiva()));
        botonEditarDescripcion.setDisable(true);
        tablaRestaurantes.setPlaceholder(new Label("No hay restaurantes registrados."));
        cargarRestaurantes();
    }

    @FXML
    private void cargarRestaurantes() {
        try {
            actualizarListado();
            mostrarMensaje("", false);
        } catch (SecurityException excepcion) {
            bloquearAcceso();
        } catch (SQLException excepcion) {
            tablaRestaurantes.getItems().clear();
            tablaRestaurantes.setPlaceholder(new Label("No se pudo cargar el listado."));
            etiquetaResumen.setText("Listado no disponible.");
            mostrarMensaje("No se pudieron cargar los restaurantes. Intenta actualizar.", true);
            excepcion.printStackTrace();
        }
    }

    private void actualizarListado() throws SQLException {
        List<Restaurante> restaurantes = listarRestaurantes();
        tablaRestaurantes.getItems().setAll(restaurantes);
        tablaRestaurantes.setPlaceholder(new Label("No hay restaurantes registrados."));
        etiquetaResumen.setText(restaurantes.size() == 1
                ? "1 restaurante registrado"
                : restaurantes.size() + " restaurantes registrados");
    }

    @FXML
    private void guardarRestaurante() {
        try {
            int id = registrarRestaurante(campoNombre.getText(), campoUbicacion.getText(),
                    campoDescripcion.getText());
            campoNombre.clear();
            campoUbicacion.clear();
            campoDescripcion.clear();

            // La inserción ya terminó. Un fallo al refrescar no debe presentarse
            // como un fallo de registro, porque podría provocar un registro duplicado.
            try {
                actualizarListado();
                mostrarMensaje("Restaurante registrado con ID " + id + ".", false);
            } catch (SQLException excepcion) {
                etiquetaResumen.setText("Listado pendiente de actualizar.");
                mostrarMensaje("El restaurante se guardó con ID " + id
                        + ", pero no se pudo actualizar el listado. Pulsa Actualizar.", true);
                excepcion.printStackTrace();
            }
            campoNombre.requestFocus();
        } catch (SecurityException excepcion) {
            bloquearAcceso();
        } catch (IllegalArgumentException excepcion) {
            mostrarMensaje(excepcion.getMessage(), true);
        } catch (SQLException excepcion) {
            mostrarMensaje("No se pudo completar el registro. Actualiza el listado"
                    + " para comprobarlo antes de volver a intentarlo.", true);
            excepcion.printStackTrace();
        }
    }

    @FXML
    private void editarDescripcionSeleccionada() {
        if (!SesionAdministrador.haySesionActiva()) {
            bloquearAcceso();
            return;
        }
        Restaurante seleccionado = tablaRestaurantes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarMensaje("Selecciona un restaurante para editar su descripción.", true);
            return;
        }

        TextArea editor = new TextArea(seleccionado.getDescripcion());
        editor.setWrapText(true);
        editor.setPrefRowCount(4);
        editor.setPrefColumnCount(42);
        Label etiqueta = new Label("Descripción (opcional)");
        etiqueta.setLabelFor(editor);

        Dialog<String> dialogo = new Dialog<>();
        dialogo.initOwner(tablaRestaurantes.getScene().getWindow());
        dialogo.setTitle("Editar descripción");
        dialogo.setHeaderText(seleccionado.getNombre());
        dialogo.setResizable(true);
        dialogo.getDialogPane().setContent(new VBox(10, etiqueta, editor));
        ButtonType guardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().addAll(guardar, cancelar);
        dialogo.setResultConverter(boton -> boton == guardar ? editor.getText() : null);
        Optional<String> resultado = dialogo.showAndWait();
        if (resultado.isEmpty()) {
            return;
        }

        try {
            actualizarDescripcion(seleccionado.getIdRestaurante(), resultado.get());
            try {
                actualizarListado();
                mostrarMensaje("Descripción actualizada.", false);
            } catch (SQLException excepcion) {
                etiquetaResumen.setText("Listado pendiente de actualizar.");
                mostrarMensaje("La descripción se guardó, pero no se pudo actualizar"
                        + " el listado. Pulsa Actualizar.", true);
                excepcion.printStackTrace();
            }
        } catch (SecurityException excepcion) {
            bloquearAcceso();
        } catch (IllegalArgumentException excepcion) {
            mostrarMensaje(excepcion.getMessage(), true);
        } catch (SQLException excepcion) {
            mostrarMensaje("No se pudo completar la actualización de la descripción."
                    + " Actualiza el listado para comprobarla.", true);
            excepcion.printStackTrace();
        }
    }

    private void bloquearAcceso() {
        campoNombre.setDisable(true);
        campoUbicacion.setDisable(true);
        campoDescripcion.setDisable(true);
        botonGuardar.setDisable(true);
        botonActualizar.setDisable(true);
        botonProductos.setDisable(true);
        botonEditarDescripcion.setDisable(true);
        tablaRestaurantes.getItems().clear();
        tablaRestaurantes.setDisable(true);
        tablaRestaurantes.setPlaceholder(new Label("Acceso administrativo requerido."));
        etiquetaResumen.setText("Sin sesión administrativa.");
        mostrarMensaje("Debe iniciar sesión como administrador.", true);
    }

    private void mostrarMensaje(String mensaje, boolean error) {
        etiquetaMensaje.setText(mensaje);
        etiquetaMensaje.getStyleClass().removeAll("mensaje-error", "admin-resumen");
        etiquetaMensaje.getStyleClass().add(error ? "mensaje-error" : "admin-resumen");
        etiquetaMensaje.setVisible(!mensaje.isEmpty());
        etiquetaMensaje.setManaged(!mensaje.isEmpty());
    }

    @FXML
    private void volverAlMenu(ActionEvent evento) {
        if (!SesionAdministrador.haySesionActiva()) {
            cerrarSesion(evento);
            return;
        }
        NavegadorVistas.cambiarVista(evento,
                "/com/foodsmanager/vista/menu-administrador.fxml",
                "FoodsManager - Administración");
    }

    @FXML
    private void gestionarProductos(ActionEvent evento) {
        if (!SesionAdministrador.haySesionActiva()) {
            cerrarSesion(evento);
            return;
        }
        NavegadorVistas.cambiarVista(evento,
                "/com/foodsmanager/vista/gestion-productos.fxml",
                "FoodsManager - Gestionar productos");
    }

    @FXML
    private void cerrarSesion(ActionEvent evento) {
        SesionAdministrador.cerrar();
        bloquearAcceso();
        NavegadorVistas.cambiarVista(evento,
                "/com/foodsmanager/vista/login.fxml",
                "FoodsManager - Acceso administrativo");
    }
}
