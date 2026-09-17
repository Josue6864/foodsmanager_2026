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

    @FXML
    private void verProductosSaborChapin(ActionEvent evento) {
        abrirProductos(evento, 1);
    }

    @FXML
    private void verProductosPizzeriaCentral(ActionEvent evento) {
        abrirProductos(evento, 2);
    }

    @FXML
    private void verProductosBurgerHouse(ActionEvent evento) {
        abrirProductos(evento, 3);
    }

    private void abrirProductos(
            ActionEvent evento,
            int idRestaurante) {

        NavegadorVistas.cambiarVistaProductos(
                evento,
                "/com/foodsmanager/vista/productos.fxml",
                "FoodsManager - Productos",
                idRestaurante
        );
    }
}