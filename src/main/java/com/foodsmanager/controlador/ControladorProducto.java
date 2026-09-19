package com.foodsmanager.controlador;

import com.foodsmanager.modelo.Producto;
import com.foodsmanager.modelo.Restaurante;
import com.foodsmanager.persistencia.ProductoDAO;
import com.foodsmanager.persistencia.RestauranteDAO;
import com.foodsmanager.modelo.CarritoCompra;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import com.foodsmanager.modelo.ContextoNavegacion;

public class ControladorProducto {

    private final ProductoDAO productoDAO;
    private final RestauranteDAO restauranteDAO;

    private final CarritoCompra carrito = CarritoCompra.getInstancia();
    private final ContextoNavegacion contextoNavegacion = ContextoNavegacion.getInstancia();
    // Estos controles pertenecen exclusivamente al catálogo público.
    @FXML
    private Label etiquetaRestaurante;

    @FXML
    private Label etiquetaSinProductos;

    @FXML
    private FlowPane contenedorProductos;

    @FXML
    private Button btnCarrito;

    @FXML
    private void initialize() {
        actualizarBotonCarrito();
    }

    private void actualizarBotonCarrito() {
        if (btnCarrito == null) {
            return;
        }

        int cantidad = carrito.getCantidadTotal();

        if (cantidad == 0) {
            btnCarrito.setText("Carrito");
        } else {
            btnCarrito.setText(
                    "Carrito (" + cantidad + ")"
            );
        }
    }
    private int idRestauranteActual;

    public ControladorProducto() {
        this(new ProductoDAO(), new RestauranteDAO());
    }

    public ControladorProducto(ProductoDAO productoDAO) {
        this(productoDAO, new RestauranteDAO());
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
     * Registra un producto sin acceder a controles de JavaFX. Conserva la firma
     * utilizada por ControladorProductoTest.
     */
    public Producto registrarProducto(
            int idRestaurante,
            String nombre,
            String descripcion,
            String precioTexto,
            boolean disponible) throws SQLException {

        double precio = convertirPrecio(precioTexto);

        // El modelo valida el id, el nombre y que el precio sea finito
        // y mayor o igual a cero. También normaliza la descripción.
        Producto producto = new Producto(
                idRestaurante,
                nombre,
                descripcion,
                precio,
                disponible
        );

        // Se utiliza el método existente del DAO; no se presupone
        // un buscarPorId() que RestauranteDAO todavía no tiene.
        boolean restauranteExiste = restauranteDAO.listarTodos()
                .stream()
                .anyMatch(restaurante
                        -> restaurante.getIdRestaurante() == idRestaurante);

        if (!restauranteExiste) {
            throw new IllegalArgumentException(
                    "El restaurante seleccionado no existe."
            );
        }

        // insertar() devuelve un int y asigna ese id al mismo objeto.
        productoDAO.insertar(producto);
        return producto;
    }

    public List<Restaurante> listarRestaurantesParaRegistro()
            throws SQLException {
        return restauranteDAO.listarTodos();
    }

    private double convertirPrecio(String precioTexto) {
        if (precioTexto == null || precioTexto.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El precio ingresado no es válido."
            );
        }

        try {
            return Double.parseDouble(
                    precioTexto.trim().replace(',', '.')
            );
        } catch (NumberFormatException excepcion) {
            throw new IllegalArgumentException(
                    "El precio ingresado no es válido.",
                    excepcion
            );
        }
    }

    public List<Producto> listarProductosPorRestaurante(
            int idRestaurante) throws SQLException {

        if (idRestaurante <= 0) {
            throw new IllegalArgumentException(
                    "El id del restaurante debe ser mayor que cero."
            );
        }

        return productoDAO.listarPorRestaurante(idRestaurante);
    }

    public void cargarProductosPorRestaurante(int idRestaurante) {
        if (idRestaurante <= 0) {
            idRestauranteActual = 0;

            etiquetaRestaurante.setText(
                    "Productos"
            );
            mostrarSinProductos(
                    "El restaurante seleccionado no es válido."
            );
            return;
        }
        idRestauranteActual = idRestaurante;

        contextoNavegacion.setIdUltimoRestaurante(
                idRestaurante
        );
        colocarNombreRestaurante(idRestaurante);
        contenedorProductos.getChildren().clear();
        try {
            List<Producto> productos
                    = listarProductosPorRestaurante(
                            idRestaurante
                    );

            if (productos.isEmpty()) {
                mostrarSinProductos(
                        "Este restaurante no tiene "
                        + "productos registrados."
                );

                return;
            }
            etiquetaSinProductos.setVisible(false);
            etiquetaSinProductos.setManaged(false);
            for (Producto producto : productos) {
                contenedorProductos
                        .getChildren()
                        .add(
                                crearTarjetaProducto(producto)
                        );
            }
        } catch (SQLException excepcion) {
            mostrarSinProductos(
                    "No se pudieron cargar los productos."
            );

            excepcion.printStackTrace();
        }
    }

    private VBox crearTarjetaProducto(Producto producto) {

        Label nombre = new Label(
                producto.getNombre()
        );

        nombre.setStyle(
                "-fx-font-size: 18px;"
                + "-fx-font-weight: bold;"
        );

        Label descripcion = new Label(
                producto.getDescripcion()
        );

        descripcion.setWrapText(true);

        Label precio = new Label(
                String.format(
                        "Q %.2f",
                        producto.getPrecio()
                )
        );

        precio.setStyle(
                "-fx-font-size: 16px;"
                + "-fx-font-weight: bold;"
        );

        Label disponibilidad = new Label(
                producto.isDisponible()
                ? "Disponible"
                : "No disponible"
        );
        Button botonAgregar
                = new Button("Agregar al carrito");

        botonAgregar.setMaxWidth(Double.MAX_VALUE);

        botonAgregar.setDisable(
                !producto.isDisponible()
        );
        botonAgregar.setStyle(
                "-fx-background-color: #173c32;"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 8;"
                + "-fx-cursor: hand;"
        );

        botonAgregar.setOnAction(evento -> {
            agregarAlCarrito(producto);
            botonAgregar.setText("Agregado");
            actualizarBotonCarrito();
        });

        VBox tarjeta = new VBox(
                12,
                nombre,
                descripcion,
                precio,
                disponibilidad,
                botonAgregar
        );

        tarjeta.setPadding(new Insets(20));
        tarjeta.setPrefWidth(250);
        tarjeta.setMinHeight(230);

        tarjeta.setStyle(
                "-fx-background-color: white;"
                + "-fx-background-radius: 12;"
                + "-fx-border-color: #dddddd;"
                + "-fx-border-radius: 12;"
                + "-fx-border-width: 1;"
        );

        return tarjeta;
    }

    private void agregarAlCarrito(
            Producto producto
    ) {
        if (producto == null) {
            return;
        }
        if (!producto.isDisponible()) {
            return;
        }
        carrito.agregarProducto(producto);
    }

    private void colocarNombreRestaurante(int idRestaurante) {
        switch (idRestaurante) {
            case 1:
                etiquetaRestaurante.setText("Productos - Sabor Chapín");
                break;
            case 2:
                etiquetaRestaurante.setText("Productos - Pizzería Central");
                break;
            case 3:
                etiquetaRestaurante.setText("Productos - Burger House");
                break;
            default:
                etiquetaRestaurante.setText("Productos");
                break;
        }
    }

    private void mostrarSinProductos(String mensaje) {
        contenedorProductos.getChildren().clear();
        etiquetaSinProductos.setText(mensaje);
        etiquetaSinProductos.setVisible(true);
        etiquetaSinProductos.setManaged(true);
    }

    @FXML
    private void volverRestaurantes(ActionEvent evento) {
        NavegadorVistas.cambiarVista(
                evento,
                "/com/foodsmanager/vista/restaurantes.fxml",
                "FoodsManager - Restaurantes"
        );
    }

    public int getIdRestauranteActual() {
        return idRestauranteActual;
    }

    @FXML
    private void abrirCarrito(ActionEvent evento) {
        NavegadorVistas.cambiarVista(
                evento,
                "/com/foodsmanager/vista/carrito.fxml",
                "FoodsManager - Carrito"
        );
    }
}
