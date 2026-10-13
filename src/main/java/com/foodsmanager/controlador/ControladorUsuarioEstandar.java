package com.foodsmanager.controlador;

import com.foodsmanager.modelo.UsuarioEstandar;
import com.foodsmanager.persistencia.UsuarioEstandarDAO;
import com.foodsmanager.seguridad.Contrasenas;
import com.foodsmanager.seguridad.SesionUsuarioEstandar;
import java.io.IOException;
import java.text.Normalizer;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.application.Platform;
import javafx.scene.control.ScrollPane;

public class ControladorUsuarioEstandar {

    @FXML
    private Button botonTengoCuenta;
    @FXML
    private Button botonCrearCuenta;
    @FXML
    private VBox panelInicioSesion;
    @FXML
    private VBox panelRegistro;
    @FXML
    private TextField campoUsuarioLogin;
    @FXML
    private PasswordField campoContrasenaLogin;
    @FXML
    private TextField campoNombreRegistro;
    @FXML
    private TextField campoTelefonoRegistro;
    @FXML
    private TextField campoUsuarioGenerado;
    @FXML
    private PasswordField campoContrasenaRegistro;
    @FXML
    private PasswordField campoConfirmarContrasena;
    @FXML
    private Label etiquetaMensaje;
    @FXML
    private ScrollPane scrollContenido;

    private final UsuarioEstandarDAO usuarioDAO = new UsuarioEstandarDAO();
    private UsuarioEstandar usuarioAutenticado;
    private Stage ventana;

    @FXML
    private void initialize() {
        mostrarInicioSesion();

        campoNombreRegistro.textProperty().addListener(
                (observable, anterior, nuevo) -> actualizarUsuarioGenerado());
        campoTelefonoRegistro.textProperty().addListener(
                (observable, anterior, nuevo) -> actualizarUsuarioGenerado());
    }

    public static Optional<UsuarioEstandar> mostrar(
            Window propietaria,
            String nombre,
            String telefono) throws IOException {

        FXMLLoader cargador = new FXMLLoader(
                ControladorUsuarioEstandar.class.getResource(
                        "/com/foodsmanager/vista/acceso-usuario.fxml"));
        Parent raiz = cargador.load();

        ControladorUsuarioEstandar controlador = cargador.getController();
        controlador.prepararDatos(nombre, telefono);

        Stage ventana = new Stage();
        controlador.ventana = ventana;
        ventana.initModality(Modality.WINDOW_MODAL);
        if (propietaria != null) {
            ventana.initOwner(propietaria);
        }
        ventana.setTitle("FoodsManager - Acceso requerido");
        ventana.setScene(new Scene(raiz));
        ventana.setResizable(false);
        ventana.showAndWait();

        return Optional.ofNullable(controlador.usuarioAutenticado);
    }

    public void prepararDatos(String nombre, String telefono) {
        campoNombreRegistro.setText(nombre == null ? "" : nombre.trim());
        campoTelefonoRegistro.setText(telefono == null ? "" : telefono.trim());
        actualizarUsuarioGenerado();
    }

    @FXML
    private void mostrarInicioSesion() {
        cambiarPanel(true);
    }

    @FXML
    private void mostrarRegistro() {
        cambiarPanel(false);
    }

    private void cambiarPanel(boolean inicioSesion) {
        panelInicioSesion.setVisible(inicioSesion);
        panelInicioSesion.setManaged(inicioSesion);

        panelRegistro.setVisible(!inicioSesion);
        panelRegistro.setManaged(!inicioSesion);

        botonTengoCuenta
                .getStyleClass()
                .remove("selector-activo");

        botonCrearCuenta
                .getStyleClass()
                .remove("selector-activo");

        Button botonActivo = inicioSesion
                ? botonTengoCuenta
                : botonCrearCuenta;

        botonActivo
                .getStyleClass()
                .add("selector-activo");

        limpiarMensaje();

        Platform.runLater(() -> {
            if (scrollContenido != null) {
                scrollContenido.setVvalue(0);
            }
        });
    }

    @FXML
    private void iniciarSesion() {
        String usuario = campoUsuarioLogin.getText().trim();
        String contrasena = campoContrasenaLogin.getText();

        if (usuario.isBlank() || contrasena.isBlank()) {
            mostrarError("Ingresa tu usuario y contraseña.");
            return;
        }

        try {
            Optional<UsuarioEstandar> resultado
                    = usuarioDAO.autenticar(usuario, contrasena);

            if (resultado.isEmpty()) {
                mostrarError("Usuario o contraseña incorrectos.");
                return;
            }

            completarAcceso(resultado.get());
        } catch (Exception excepcion) {
            mostrarError("No se pudo iniciar sesión: " + excepcion.getMessage());
        }
    }

    @FXML
    private void crearCuenta() {
        String nombre = campoNombreRegistro.getText().trim();
        String telefono = campoTelefonoRegistro.getText().trim();
        String usuario = campoUsuarioGenerado.getText().trim();
        String contrasena = campoContrasenaRegistro.getText();
        String confirmacion = campoConfirmarContrasena.getText();

        if (nombre.isBlank()) {
            mostrarError("El nombre es obligatorio.");
            return;
        }
        if (!telefono.matches("\\d{8}")) {
            mostrarError("El teléfono debe contener exactamente 8 dígitos.");
            return;
        }
        if (contrasena.length() < 8) {
            mostrarError("La contraseña debe tener al menos 8 caracteres.");
            return;
        }
        if (!contrasena.equals(confirmacion)) {
            mostrarError("Las contraseñas no coinciden.");
            return;
        }

        try {
            if (usuarioDAO.buscarPorTelefono(telefono).isPresent()) {
                mostrarError("Ese teléfono ya tiene una cuenta. Usa 'Ya tengo cuenta'.");
                return;
            }

            UsuarioEstandar nuevo = new UsuarioEstandar(
                    nombre,
                    telefono,
                    usuario,
                    Contrasenas.crearHash(contrasena));

            completarAcceso(usuarioDAO.insertar(nuevo));
        } catch (Exception excepcion) {
            mostrarError("No se pudo crear la cuenta: " + excepcion.getMessage());
        }
    }

    private void completarAcceso(UsuarioEstandar usuario) {
        usuarioAutenticado = usuario;
        SesionUsuarioEstandar.iniciar(usuario);
        ventana.close();
    }

    @FXML
    private void cancelar() {
        usuarioAutenticado = null;
        ventana.close();
    }

    private void actualizarUsuarioGenerado() {
        String nombre = campoNombreRegistro.getText();
        String telefono = campoTelefonoRegistro.getText().replaceAll("\\D", "");
        campoUsuarioGenerado.setText(generarUsuario(nombre, telefono));
    }

    private String generarUsuario(String nombre, String telefono) {
        String limpio = Normalizer.normalize(
                nombre == null ? "" : nombre.trim().toLowerCase(),
                Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");

        if (limpio.isBlank() || telefono.isBlank()) {
            return "";
        }
        return limpio + "-" + telefono;
    }

    private void mostrarError(String mensaje) {
        etiquetaMensaje.setText(mensaje);
        etiquetaMensaje.getStyleClass().remove("mensaje-exito");
        if (!etiquetaMensaje.getStyleClass().contains("mensaje-error")) {
            etiquetaMensaje.getStyleClass().add("mensaje-error");
        }
    }

    private void limpiarMensaje() {
        etiquetaMensaje.setText("");
    }
}
