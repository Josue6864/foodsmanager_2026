package com.foodsmanager.persistencia;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class ConexionSQLite {

    private static final Path DIRECTORIO_DATOS =
            Path.of("data").toAbsolutePath();

    private static final Path ARCHIVO_BASE_DATOS =
            DIRECTORIO_DATOS.resolve("foodsmanager.db");

    private static final String URL =
            "jdbc:sqlite:" + ARCHIVO_BASE_DATOS;

    private ConexionSQLite() {
        // Evita crear objetos de esta clase.
    }

    public static Connection abrirConexion() throws SQLException {
        crearDirectorioDatos();

        Connection conexion = DriverManager.getConnection(URL);

        try (Statement sentencia = conexion.createStatement()) {
            sentencia.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException excepcion) {
            conexion.close();
            throw excepcion;
        }

        return conexion;
    }

    private static void crearDirectorioDatos() throws SQLException {
        try {
            Files.createDirectories(DIRECTORIO_DATOS);
        } catch (IOException excepcion) {
            throw new SQLException(
                    "No se pudo crear el directorio de la base de datos.",
                    excepcion
            );
        }
    }

    public static Path obtenerRutaBaseDatos() {
        return ARCHIVO_BASE_DATOS;
    }
}