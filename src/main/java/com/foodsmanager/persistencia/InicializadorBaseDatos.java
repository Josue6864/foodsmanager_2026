package com.foodsmanager.persistencia;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class InicializadorBaseDatos {

    private static final String RUTA_ESQUEMA = "/db/schema.sql";

    private InicializadorBaseDatos() {
        // Evita crear objetos de esta clase.
    }

    public static void inicializar() throws SQLException {
        String esquema = leerEsquema();

        try (Connection conexion = ConexionSQLite.abrirConexion();
             Statement sentencia = conexion.createStatement()) {

            conexion.setAutoCommit(false);

            try {
                for (String instruccion : esquema.split(";")) {
                    String sql = instruccion.trim();

                    if (!sql.isEmpty()) {
                        sentencia.execute(sql);
                    }
                }

                conexion.commit();
            } catch (SQLException excepcion) {
                conexion.rollback();
                throw excepcion;
            }
        }
    }

    private static String leerEsquema() throws SQLException {
        try (InputStream entrada =
                     InicializadorBaseDatos.class
                             .getResourceAsStream(RUTA_ESQUEMA)) {

            if (entrada == null) {
                throw new SQLException(
                        "No se encontró el archivo " + RUTA_ESQUEMA
                );
            }

            return new String(
                    entrada.readAllBytes(),
                    StandardCharsets.UTF_8
            );
        } catch (IOException excepcion) {
            throw new SQLException(
                    "No se pudo leer el esquema de la base de datos.",
                    excepcion
            );
        }
    }
}