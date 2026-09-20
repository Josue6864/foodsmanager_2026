package com.foodsmanager.controlador;

import com.foodsmanager.modelo.Producto;
import com.foodsmanager.modelo.Restaurante;
import com.foodsmanager.persistencia.ProductoDAO;
import com.foodsmanager.persistencia.RestauranteDAO;
import com.foodsmanager.seguridad.SesionAdministrador;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;

public class ControladorListadoProductos {

    private final ProductoDAO productoDAO;
    private final RestauranteDAO restauranteDAO;
    private final Map<Integer, String> nombresRestaurantes = new HashMap<>();
    private boolean procesando;

    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, Number> columnaId;
    @FXML private TableColumn<Producto, String> columnaNombre;
    @FXML private TableColumn<Producto, String> columnaRestaurante;
    @FXML private TableColumn<Producto, Number> columnaPrecio;
    @FXML private TableColumn<Producto, String> columnaEstado;
    @FXML private Label etiquetaResumen;
    @FXML private Label etiquetaMensaje;
    @FXML private Button botonAgregar;
    @FXML private Button botonEditar;
    @FXML private Button botonActualizar;
    @FXML private Button botonMenu;
    @FXML private Button botonCerrarSesion;

    public ControladorListadoProductos() {
        this(new ProductoDAO(), new RestauranteDAO());
    }

    public ControladorListadoProductos(ProductoDAO productoDAO, RestauranteDAO restauranteDAO) {
        this.productoDAO = Objects.requireNonNull(productoDAO);
        this.restauranteDAO = Objects.requireNonNull(restauranteDAO);
    }

    @FXML
    private void initialize() {
        columnaId.setCellValueFactory(c -> new ReadOnlyIntegerWrapper(c.getValue().getIdProducto()));
        columnaNombre.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getNombre()));
        columnaRestaurante.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                nombreRestaurante(c.getValue().getIdRestaurante())));
        columnaPrecio.setCellValueFactory(c -> new ReadOnlyDoubleWrapper(c.getValue().getPrecio()));
        columnaEstado.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().isDisponible() ? "Disponible" : "No disponible"));

        columnaPrecio.setCellFactory(columna -> new TableCell<Producto, Number>() {
            @Override
            protected void updateItem(Number precio, boolean vacia) {
                super.updateItem(precio, vacia);
                setText(vacia || precio == null ? null
                        : String.format(Locale.US, "Q %.2f", precio.doubleValue()));
            }
        });
        columnaEstado.setCellFactory(columna -> new TableCell<Producto, String>() {
            @Override
            protected void updateItem(String estado, boolean vacia) {
                super.updateItem(estado, vacia);
                setText(null);
                if (vacia || estado == null) {
                    setGraphic(null);
                    return;
                }
                Label etiqueta = new Label(estado);
                etiqueta.getStyleClass().add("Disponible".equals(estado)
                        ? "admin-estado-disponible" : "admin-estado-no-disponible");
                setGraphic(etiqueta);
            }
        });

        tablaProductos.setPlaceholder(new Label("No hay productos registrados."));
        tablaProductos.getSelectionModel().selectedItemProperty()
                .addListener((observable, anterior, actual) -> actualizarControles());
        tablaProductos.setRowFactory(tabla -> {
            TableRow<Producto> fila = new TableRow<>();
            fila.setOnMouseClicked(evento -> {
                if (!fila.isEmpty() && evento.getButton() == MouseButton.PRIMARY
                        && evento.getClickCount() == 2 && !procesando) {
                    tabla.getSelectionModel().select(fila.getItem());
                    editarSeleccionado();
                }
            });
            return fila;
        });
        cargarProductos();
    }

    /** Operación administrativa comprobable sin abrir una ventana. */
    public List<Producto> listarProductos() throws SQLException {
        SesionAdministrador.exigirSesion();
        return productoDAO.listarTodos();
    }

    /** Valida con el modelo y guarda únicamente nombre y disponibilidad. */
    public Producto actualizarProducto(int idProducto, String nombre, boolean disponible)
            throws SQLException {
        SesionAdministrador.exigirSesion();
        Producto producto = productoDAO.buscarPorId(idProducto);
        if (producto == null) {
            throw new IllegalArgumentException("El producto seleccionado ya no existe.");
        }
        producto.setNombre(nombre);
        producto.setDisponible(disponible);
        productoDAO.actualizarNombreYDisponibilidad(
                producto.getIdProducto(), producto.getNombre(), producto.isDisponible());
        return producto;
    }

    @FXML
    private void cargarProductos() {
        if (procesando || !comprobarSesion()) {
            return;
        }
        procesando = true;
        actualizarControles();
        mostrarMensaje("Cargando productos...", false);
        tablaProductos.setPlaceholder(new Label("Cargando productos..."));

        Task<Catalogo> tarea = new Task<>() {
            @Override
            protected Catalogo call() throws SQLException {
                return new Catalogo(listarProductos(), restauranteDAO.listarTodos());
            }
        };
        tarea.setOnSucceeded(evento -> {
            procesando = false;
            if (!comprobarSesion()) {
                return;
            }
            Catalogo catalogo = tarea.getValue();
            nombresRestaurantes.clear();
            for (Restaurante restaurante : catalogo.restaurantes) {
                nombresRestaurantes.put(restaurante.getIdRestaurante(), restaurante.getNombre());
            }
            tablaProductos.getItems().setAll(catalogo.productos);
            tablaProductos.sort();
            tablaProductos.setPlaceholder(new Label("No hay productos registrados."));
            actualizarResumen();
            actualizarControles();
            mostrarMensaje("", false);
        });
        tarea.setOnFailed(evento -> {
            procesando = false;
            if (!comprobarSesion()) {
                return;
            }
            tablaProductos.getItems().clear();
            tablaProductos.setPlaceholder(new Label("No se pudieron cargar los productos."));
            etiquetaResumen.setText("Listado no disponible");
            actualizarControles();
            mostrarMensaje("No se pudieron cargar los productos. Pulsa Actualizar para reintentar.", true);
            tarea.getException().printStackTrace();
        });
        Thread hilo = new Thread(tarea, "foodsmanager-listado-administrativo");
        hilo.setDaemon(true);
        hilo.start();
    }

    @FXML
    private void editarSeleccionado() {
        if (procesando || !comprobarSesion()) {
            return;
        }
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarMensaje("Selecciona un producto para editarlo.", true);
            return;
        }

        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.initOwner(tablaProductos.getScene().getWindow());
        dialogo.setTitle("FoodsManager - Editar producto");
        dialogo.setHeaderText("Editar producto");
        dialogo.setResizable(true);
        dialogo.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(getClass().getResource(
                        "/com/foodsmanager/vista/tema.css")).toExternalForm());
        dialogo.getDialogPane().getStyleClass().add("admin-dialogo");

        Label contexto = new Label(nombreRestaurante(seleccionado.getIdRestaurante())
                + " · Producto #" + seleccionado.getIdProducto());
        contexto.setWrapText(true);
        contexto.getStyleClass().add("subtitulo");
        TextField nombre = new TextField(seleccionado.getNombre());
        nombre.setPromptText("Nombre del producto");
        CheckBox disponible = new CheckBox("Producto disponible");
        disponible.setSelected(seleccionado.isDisponible());
        Label ayuda = new Label("Si no está disponible, seguirá visible en el catálogo y no se podrá agregar al carrito.");
        ayuda.setWrapText(true);
        ayuda.setMaxWidth(400);
        ayuda.getStyleClass().add("subtitulo");
        Label error = new Label();
        error.setWrapText(true);
        error.setMaxWidth(400);
        error.getStyleClass().add("mensaje-error");
        VBox contenido = new VBox(14, contexto, new Label("Nombre del producto"),
                nombre, disponible, ayuda, error);
        contenido.setPadding(new Insets(12, 4, 8, 4));
        contenido.setPrefWidth(420);
        dialogo.getDialogPane().setContent(contenido);

        ButtonType guardar = new ButtonType("Guardar cambios", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().addAll(cancelar, guardar);
        Button botonGuardar = (Button) dialogo.getDialogPane().lookupButton(guardar);
        botonGuardar.getStyleClass().add("boton-principal");
        dialogo.getDialogPane().lookupButton(cancelar).getStyleClass().add("boton-regresar");
        botonGuardar.addEventFilter(ActionEvent.ACTION, evento -> {
            try {
                Producto actualizado = actualizarProducto(seleccionado.getIdProducto(),
                        nombre.getText(), disponible.isSelected());
                int indice = tablaProductos.getItems().indexOf(seleccionado);
                if (indice >= 0) {
                    tablaProductos.getItems().set(indice, actualizado);
                }
                tablaProductos.sort();
                tablaProductos.getSelectionModel().select(actualizado);
                actualizarResumen();
                mostrarMensaje("Producto actualizado correctamente.", false);
            } catch (IllegalArgumentException | SecurityException excepcion) {
                error.setText(excepcion.getMessage());
                evento.consume();
            } catch (SQLException excepcion) {
                error.setText("No se pudieron guardar los cambios. Inténtalo nuevamente.");
                excepcion.printStackTrace();
                evento.consume();
            }
        });
        dialogo.setOnShown(evento -> Platform.runLater(nombre::requestFocus));
        dialogo.showAndWait();
    }

    @FXML
    private void agregarProducto(ActionEvent evento) {
        if (!procesando && comprobarSesion()) {
            NavegadorVistas.cambiarVista(evento,
                    "/com/foodsmanager/vista/administrador.fxml",
                    "FoodsManager - Registrar producto");
        }
    }

    @FXML
    private void volverAlMenu(ActionEvent evento) {
        if (!procesando && comprobarSesion()) {
            NavegadorVistas.cambiarVista(evento,
                    "/com/foodsmanager/vista/menu-administrador.fxml",
                    "FoodsManager - Administración");
        }
    }

    @FXML
    private void cerrarSesion(ActionEvent evento) {
        if (procesando) {
            return;
        }
        SesionAdministrador.cerrar();
        comprobarSesion();
        NavegadorVistas.cambiarVista(evento,
                "/com/foodsmanager/vista/login.fxml",
                "FoodsManager - Acceso administrativo");
    }

    private boolean comprobarSesion() {
        if (SesionAdministrador.haySesionActiva()) {
            return true;
        }
        tablaProductos.getItems().clear();
        etiquetaResumen.setText("Sin sesión administrativa");
        tablaProductos.setPlaceholder(new Label("Inicia sesión para consultar los productos."));
        mostrarMensaje("Debe iniciar sesión como administrador.", true);
        actualizarControles();
        return false;
    }

    private void actualizarControles() {
        boolean bloquear = procesando || !SesionAdministrador.haySesionActiva();
        tablaProductos.setDisable(bloquear);
        botonAgregar.setDisable(bloquear);
        botonActualizar.setDisable(bloquear);
        botonMenu.setDisable(bloquear);
        botonEditar.setDisable(bloquear
                || tablaProductos.getSelectionModel().getSelectedItem() == null);
        botonCerrarSesion.setDisable(procesando);
    }

    private String nombreRestaurante(int id) {
        return nombresRestaurantes.getOrDefault(id, "Restaurante #" + id);
    }

    private void actualizarResumen() {
        List<Producto> productos = tablaProductos.getItems();
        long disponibles = productos.stream().filter(Producto::isDisponible).count();
        etiquetaResumen.setText(productos.size() + " productos · " + disponibles
                + " disponibles · " + (productos.size() - disponibles) + " no disponibles");
    }

    private void mostrarMensaje(String mensaje, boolean error) {
        etiquetaMensaje.setText(mensaje);
        etiquetaMensaje.setStyle(error ? "-fx-text-fill: #B91C1C;" : "-fx-text-fill: #2F5D50;");
    }

    private static final class Catalogo {
        private final List<Producto> productos;
        private final List<Restaurante> restaurantes;

        private Catalogo(List<Producto> productos, List<Restaurante> restaurantes) {
            this.productos = productos;
            this.restaurantes = restaurantes;
        }
    }
}