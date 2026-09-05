package com.foodsmanager;

import com.foodsmanager.persistencia.ConexionSQLite;
import com.foodsmanager.persistencia.InicializadorBaseDatos;
import java.sql.SQLException;

public class FoodsManagerApp {

    public static void main(String[] args) {
        try {
            InicializadorBaseDatos.inicializar();

            System.out.println("Base de datos preparada correctamente.");
            System.out.println(
                    "Ubicación: "
                    + ConexionSQLite.obtenerRutaBaseDatos()
            );
        } catch (SQLException excepcion) {
            System.err.println(
                    "No se pudo preparar la base de datos: "
                    + excepcion.getMessage()
            );

            excepcion.printStackTrace();
        }
    }
}
