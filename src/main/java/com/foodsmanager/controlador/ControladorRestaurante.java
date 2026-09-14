package com.foodsmanager.controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class ControladorRestaurante {

    @FXML
    private void abrirLogin(ActionEvent evento) {
        NavegadorVistas.cambiarVista(
                evento,
                "/com/foodsmanager/vista/login.fxml",
                "FoodsManager - Acceso administrativo"
        );
    }
}