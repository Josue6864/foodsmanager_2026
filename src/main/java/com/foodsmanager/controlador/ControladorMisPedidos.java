package com.foodsmanager.controlador;

import com.foodsmanager.modelo.DetallePedido;
import com.foodsmanager.modelo.Pedido;
import com.foodsmanager.modelo.UsuarioEstandar;
import com.foodsmanager.persistencia.PedidoDAO;
import com.foodsmanager.seguridad.SesionUsuarioEstandar;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

/** Consulta de pedidos de la sesión de cliente; esta vista no registra compras. */
public class ControladorMisPedidos {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final PedidoDAO pedidoDAO = new PedidoDAO();

    @FXML private Label etiquetaCliente;
    @FXML private Label etiquetaMensaje;
    @FXML private Label etiquetaActualizacion;
    @FXML private Label etiquetaDetalle;
    @FXML private Label etiquetaEstado;
    @FXML private Label etiquetaRecogida;
    @FXML private Label etiquetaIndicaciones;
    @FXML private Label etiquetaTotal;
    @FXML private Button botonActualizar;
    @FXML private VBox panelDetalle;
    @FXML private TableView<Pedido> tablaPedidos;
    @FXML private TableColumn<Pedido, String> columnaNumero;
    @FXML private TableColumn<Pedido, String> columnaFecha;
    @FXML private TableColumn<Pedido, String> columnaEstado;
    @FXML private TableColumn<Pedido, String> columnaTotal;
    @FXML private TableView<DetallePedido> tablaDetalles;
    @FXML private TableColumn<DetallePedido, String> columnaProducto;
    @FXML private TableColumn<DetallePedido, String> columnaCantidad;
    @FXML private TableColumn<DetallePedido, String> columnaPrecio;
    @FXML private TableColumn<DetallePedido, String> columnaSubtotal;

    @FXML
    private void initialize() {
        columnaNumero.setCellValueFactory(c -> texto("#" + c.getValue().getIdPedido()));
        columnaFecha.setCellValueFactory(c -> texto(c.getValue().getFechaPedido().format(FECHA)));
        columnaEstado.setCellValueFactory(c -> texto(estadoVisible(c.getValue().getEstado())));
        columnaTotal.setCellValueFactory(c -> texto(moneda(c.getValue().getTotal())));
        columnaProducto.setCellValueFactory(c -> texto(c.getValue().getProducto().getNombre()));
        columnaCantidad.setCellValueFactory(c -> texto(Integer.toString(c.getValue().getCantidad())));
        columnaPrecio.setCellValueFactory(c -> texto(moneda(c.getValue().getPrecioUnitario())));
        columnaSubtotal.setCellValueFactory(c -> texto(moneda(c.getValue().getSubtotal())));
        tablaPedidos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaDetalles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaPedidos.setPlaceholder(new Label("Todavía no tienes pedidos registrados."));
        tablaDetalles.setPlaceholder(new Label("No hay productos para este pedido."));
        tablaPedidos.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, seleccionado) -> mostrarDetalle(seleccionado));
        actualizarPedidos();
    }

    @FXML
    private void actualizarPedidos() {
        Pedido seleccionado = tablaPedidos.getSelectionModel().getSelectedItem();
        int idSeleccionado = seleccionado == null ? 0 : seleccionado.getIdPedido();
        tablaPedidos.getItems().clear();
        mostrarDetalle(null);
        etiquetaActualizacion.setText("");

        if (!SesionUsuarioEstandar.haySesionActiva()) {
            etiquetaCliente.setText("Acceso de cliente requerido");
            tablaPedidos.setPlaceholder(new Label("Inicia sesión para consultar tus pedidos."));
            mostrarMensaje("Vuelve a restaurantes e inicia sesión como cliente.", true);
            botonActualizar.setDisable(true);
            return;
        }

        UsuarioEstandar usuario = SesionUsuarioEstandar.obtenerUsuario();
        etiquetaCliente.setText("Pedidos de " + usuario.getNombre());
        try {
            // El identificador se obtiene de la sesión, nunca de un campo editable.
            List<Pedido> pedidos = pedidoDAO.listarPorUsuario(usuario.getIdUsuario());
            tablaPedidos.getItems().setAll(pedidos);
            tablaPedidos.setPlaceholder(new Label("Todavía no tienes pedidos registrados."));
            mostrarMensaje(pedidos.isEmpty()
                    ? "Cuando realices una compra, podrás consultar aquí su estado."
                    : "Selecciona un pedido para ver sus productos y los datos de recogida.", false);
            etiquetaActualizacion.setText("Última consulta: " + LocalDateTime.now().format(FECHA));
            if (!pedidos.isEmpty()) {
                Pedido elegir = pedidos.stream()
                        .filter(p -> p.getIdPedido() == idSeleccionado)
                        .findFirst().orElse(pedidos.get(0));
                tablaPedidos.getSelectionModel().select(elegir);
            }
        } catch (SQLException | IllegalArgumentException excepcion) {
            tablaPedidos.setPlaceholder(new Label("No se pudieron cargar los pedidos."));
            mostrarMensaje("No se pudieron consultar tus pedidos. Intenta actualizar nuevamente.", true);
            excepcion.printStackTrace();
        }
    }

    private void mostrarDetalle(Pedido pedido) {
        tablaDetalles.getItems().clear();
        panelDetalle.setVisible(false);
        panelDetalle.setManaged(false);
        etiquetaDetalle.setText("");
        etiquetaEstado.setText("");
        etiquetaRecogida.setText("");
        etiquetaIndicaciones.setText("");
        etiquetaTotal.setText("");
        if (pedido == null || !SesionUsuarioEstandar.haySesionActiva()
                || pedido.getIdUsuario() != SesionUsuarioEstandar.obtenerUsuario().getIdUsuario()) {
            return;
        }
        etiquetaDetalle.setText("Detalle del pedido #" + pedido.getIdPedido());
        etiquetaEstado.setText("Estado: " + estadoVisible(pedido.getEstado()));
        etiquetaRecogida.setText("Recoge: " + pedido.getPersonaRecoge()
                + "  ·  Hora solicitada: " + pedido.getHoraRecogida());
        String indicaciones = pedido.getIndicaciones();
        etiquetaIndicaciones.setText("Indicaciones: "
                + (indicaciones == null || indicaciones.isBlank() ? "Sin indicaciones." : indicaciones));
        etiquetaTotal.setText("Total del pedido: " + moneda(pedido.getTotal()));
        tablaDetalles.getItems().setAll(pedido.getDetalles());
        panelDetalle.setVisible(true);
        panelDetalle.setManaged(true);
    }

    private void mostrarMensaje(String mensaje, boolean error) {
        etiquetaMensaje.setText(mensaje);
        etiquetaMensaje.getStyleClass().removeAll("mensaje-error", "subtitulo");
        etiquetaMensaje.getStyleClass().add(error ? "mensaje-error" : "subtitulo");
    }

    @FXML
    private void volverRestaurantes(ActionEvent evento) {
        NavegadorVistas.cambiarVista(evento, "/com/foodsmanager/vista/restaurantes.fxml",
                "FoodsManager - Restaurantes");
    }

    @FXML
    private void cerrarSesion(ActionEvent evento) {
        SesionUsuarioEstandar.cerrar();
        tablaPedidos.getItems().clear();
        mostrarDetalle(null);
        etiquetaCliente.setText("");
        etiquetaActualizacion.setText("");
        mostrarMensaje("Sesión cerrada. Vuelve a restaurantes para ingresar nuevamente.", false);
        botonActualizar.setDisable(true);
        volverRestaurantes(evento);
    }

    private static ReadOnlyStringWrapper texto(String valor) {
        return new ReadOnlyStringWrapper(valor);
    }

    private static String moneda(double importe) {
        return String.format(Locale.US, "Q %.2f", importe);
    }

    private static String estadoVisible(String estado) {
        return estado.replace('_', ' ');
    }
}
