package com.foodsmanager.controlador;

import java.io.IOException;
import java.net.URL;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public final class NavegadorVistas {

    private NavegadorVistas() {
    }

    public static void cambiarVista(
            ActionEvent evento,
            String rutaFXML,
            String tituloVentana) {

        try {
            URL recurso = NavegadorVistas.class.getResource(rutaFXML);

            if (recurso == null) {
                throw new IOException(
                        "No se encontró el archivo FXML: " + rutaFXML
                );
            }

            Parent nuevaVista = FXMLLoader.load(recurso);

            Node origen = (Node) evento.getSource();
            Stage escenario = (Stage) origen.getScene().getWindow();
            Scene escenaActual = escenario.getScene();

            escenaActual.setRoot(nuevaVista);
            escenario.setTitle(tituloVentana);

        } catch (IOException excepcion) {
            mostrarErrorNavegacion(
                    excepcion.getMessage()
            );
        }
    }

    public static void cambiarVistaProductos(
            ActionEvent evento,
            String rutaFXML,
            String tituloVentana,
            int idRestaurante) {

        try {
            URL recurso = NavegadorVistas.class.getResource(rutaFXML);

            if (recurso == null) {
                throw new IOException(
                        "No se encontró el archivo FXML: " + rutaFXML
                );
            }

            FXMLLoader cargador = new FXMLLoader(recurso);

            Parent nuevaVista = cargador.load();

            ControladorProducto controladorProducto =
                    cargador.getController();

            controladorProducto.cargarProductosPorRestaurante(
                    idRestaurante
            );

            Node origen = (Node) evento.getSource();

            Stage escenario =
                    (Stage) origen.getScene().getWindow();

            Scene escenaActual =
                    escenario.getScene();

            escenaActual.setRoot(nuevaVista);

            escenario.setTitle(
                    tituloVentana
            );

        } catch (IOException excepcion) {
            mostrarErrorNavegacion(
                    excepcion.getMessage()
            );
        }
    }

    private static void mostrarErrorNavegacion(
            String mensaje) {

        Alert alerta =
                new Alert(Alert.AlertType.ERROR);

        alerta.setTitle(
                "Error de navegación"
        );

        alerta.setHeaderText(
                "No se pudo abrir la vista"
        );

        alerta.setContentText(
                mensaje
        );

        alerta.showAndWait();
    }
}