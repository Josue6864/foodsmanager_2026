package com.foodsmanager;

import com.foodsmanager.persistencia.ConexionSQLite;
import com.foodsmanager.persistencia.InicializadorBaseDatos;
import com.foodsmanager.persistencia.AdministradorDAO;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class FoodsManagerApp extends Application {

    @Override
    public void start(Stage escenario) {
        try {
            InicializadorBaseDatos.inicializar();

            AdministradorDAO administradorDAO = new AdministradorDAO();

            administradorDAO.crearSiNoExiste(
                    "josue",
                    "JosueDemo2026!"
            );

            administradorDAO.crearSiNoExiste(
                    "cesar",
                    "CesarDemo2026!"
            );

            administradorDAO.crearSiNoExiste(
                    "obed",
                    "ObedDemo2026!"
            );

            administradorDAO.crearSiNoExiste(
                    "santiago",
                    "SantiagoDemo2026!"
            );
            URL rutaFXML = FoodsManagerApp.class.getResource(
                    "/com/foodsmanager/vista/restaurantes.fxml"
            );

            if (rutaFXML == null) {
                throw new IOException(
                        "No se encontró la vista principal"
                );
            }

            Parent raiz = FXMLLoader.load(rutaFXML);
            Scene escena = new Scene(raiz);

            escenario.setTitle("FoodsManager - Restaurantes");
            escenario.setScene(escena);
            escenario.setMinWidth(650);
            escenario.setMinHeight(450);
            escenario.show();

            System.out.println(
                    "Base de datos preparada correctamente."
            );
            System.out.println(
                    "Ubicación: "
                    + ConexionSQLite.obtenerRutaBaseDatos()
            );
        } catch (SQLException | IOException excepcion) {
            mostrarErrorFatal(excepcion);
        }
    }

    private void mostrarErrorFatal(Exception excepcion) {
        System.err.println(
                "No se pudo iniciar FoodsManager: "
                + excepcion.getMessage()
        );

        excepcion.printStackTrace();

        Alert alerta = new Alert(
                Alert.AlertType.ERROR
        );

        alerta.setTitle("Error de inicio");
        alerta.setHeaderText(
                "No se pudo iniciar FoodsManager"
        );
        alerta.setContentText(
                excepcion.getMessage()
        );

        alerta.showAndWait();
        Platform.exit();
    }
}
