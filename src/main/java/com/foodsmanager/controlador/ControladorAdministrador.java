package com.foodsmanager.controlador;

import com.foodsmanager.modelo.Administrador;
import com.foodsmanager.persistencia.AdministradorDAO;
import com.foodsmanager.seguridad.Contrasenas;
import com.foodsmanager.seguridad.SesionAdministrador;

import java.sql.SQLException;
import java.util.Objects;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ControladorAdministrador {

    private final AdministradorDAO administradorDAO;
    private boolean procesando;

    @FXML
    private TextField campoUsuario;

    @FXML
    private PasswordField campoContrasena;

    @FXML
    private Button botonIniciarSesion;

    @FXML
    private Button botonRegresar;

    @FXML
    private Label etiquetaMensaje;

    public ControladorAdministrador() {
        this(new AdministradorDAO());
    }

    public ControladorAdministrador(
            AdministradorDAO administradorDAO) {

        this.administradorDAO = Objects.requireNonNull(
                administradorDAO
        );
    }

    @FXML
    private void initialize() {
        SesionAdministrador.cerrar();
    }

    /**
      Valida las credenciales sin depender de controles JavaFX.
     
      Devuelve el administrador si las credenciales coinciden,
      o null si son incorrectas.
     */
    public Administrador autenticar(
            String usuario,
            String contrasena) throws SQLException {

        if (usuario == null || usuario.isBlank()) {
            throw new IllegalArgumentException(
                    "Ingrese su usuario."
            );
        }

        if (contrasena == null || contrasena.isBlank()) {
            throw new IllegalArgumentException(
                    "Ingrese su contraseña."
            );
        }

        Administrador administrador =
                administradorDAO.buscarPorUsuario(usuario);

        if (administrador == null
                || !Contrasenas.verificar(
                        contrasena,
                        administrador.getContrasenaHash())) {
            return null;
        }

        return administrador;
    }

    @FXML
    private void iniciarSesion(ActionEvent evento) {
        if (procesando) {
            return;
        }

        SesionAdministrador.cerrar();

        String usuario = campoUsuario.getText();
        String contrasena = campoContrasena.getText();

        campoContrasena.clear();
        cambiarEstadoProcesando(true);
        mostrarMensaje("Verificando credenciales...");

        Task<Administrador> tarea = new Task<Administrador>() {
            @Override
            protected Administrador call() throws SQLException {
                return autenticar(usuario, contrasena);
            }
        };

        tarea.setOnSucceeded(resultado -> {
            cambiarEstadoProcesando(false);

            Administrador administrador = tarea.getValue();

            if (administrador == null) {
                mostrarMensaje(
                        "Usuario o contraseña incorrectos."
                );
                campoContrasena.requestFocus();
                return;
            }

            SesionAdministrador.iniciar(administrador);
            mostrarMensaje("");

            NavegadorVistas.cambiarVista(
                    evento,
                    "/com/foodsmanager/vista/administrador.fxml",
                    "FoodsManager - Gestión de productos"
            );
        });

        tarea.setOnFailed(resultado -> {
            cambiarEstadoProcesando(false);
            SesionAdministrador.cerrar();

            Throwable error = tarea.getException();

            if (error instanceof IllegalArgumentException) {
                mostrarMensaje(error.getMessage());
            } else {
                mostrarMensaje(
                        "No se pudo iniciar sesión. Inténtelo nuevamente."
                );
                error.printStackTrace();
            }
        });

        Thread hilo = new Thread(
                tarea,
                "foodsmanager-autenticacion"
        );

        hilo.setDaemon(true);
        hilo.start();
    }

    @FXML
    private void regresarARestaurantes(ActionEvent evento) {
        if (procesando) {
            return;
        }

        SesionAdministrador.cerrar();

        NavegadorVistas.cambiarVista(
                evento,
                "/com/foodsmanager/vista/restaurantes.fxml",
                "FoodsManager - Restaurantes"
        );
    }

    private void cambiarEstadoProcesando(boolean valor) {
        procesando = valor;

        botonIniciarSesion.setDisable(valor);
        botonRegresar.setDisable(valor);
        campoUsuario.setDisable(valor);
        campoContrasena.setDisable(valor);
    }

    private void mostrarMensaje(String mensaje) {
        etiquetaMensaje.setText(mensaje);
        etiquetaMensaje.setStyle(
                "-fx-text-fill: #B91C1C;"
        );
    }
}