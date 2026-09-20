package com.foodsmanager.controlador;

import com.foodsmanager.seguridad.SesionAdministrador;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class ControladorMenuAdministrador {

    @FXML private Button botonProductos;
    @FXML private Label etiquetaUsuario;
    @FXML private Label etiquetaMensaje;

    @FXML
    private void initialize() {
        boolean autorizado = SesionAdministrador.haySesionActiva();
        botonProductos.setDisable(!autorizado);
        etiquetaUsuario.setText(autorizado
                ? "Sesión: " + SesionAdministrador.obtenerUsuario() : "Sin sesión");
        etiquetaMensaje.setText(autorizado ? "" : "Debe iniciar sesión como administrador.");
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
        botonProductos.setDisable(true);
        etiquetaUsuario.setText("Sin sesión");
        NavegadorVistas.cambiarVista(evento,
                "/com/foodsmanager/vista/login.fxml",
                "FoodsManager - Acceso administrativo");
    }
}
