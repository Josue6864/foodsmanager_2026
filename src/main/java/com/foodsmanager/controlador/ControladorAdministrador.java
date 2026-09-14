package com.foodsmanager.controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class ControladorAdministrador {

    @FXML
    private void regresarARestaurantes(ActionEvent evento) {
        NavegadorVistas.cambiarVista(
                evento,
                "/com/foodsmanager/vista/restaurantes.fxml",
                "FoodsManager - Restaurantes"
        );
    }
}