package com.foodsmanager.persistencia;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class InicializadorBaseDatos {

    private static final String RUTA_ESQUEMA = "/db/schema.sql";

    private static final String SQL_AGREGAR_UBICACION = """
            ALTER TABLE restaurante
            ADD COLUMN ubicacion TEXT NOT NULL DEFAULT 'Ubicación pendiente'
            """;

    private InicializadorBaseDatos() {
        // Evita crear objetos de esta clase.
    }

    public static void inicializar() throws SQLException {
        String esquema = leerEsquema();

        try (Connection conexion = ConexionSQLite.abrirConexion();
             Statement sentencia = conexion.createStatement()) {

            conexion.setAutoCommit(false);

            try {
                // Debe ejecutarse antes del esquema actualizado: una tabla
                // existente no cambia con CREATE TABLE IF NOT EXISTS.
                actualizarTablaRestaurante(conexion);

                int numeroInstruccion = 0;

                for (String instruccion : esquema.split(";")) {
                    String sql = instruccion.trim();

                    if (!sql.isEmpty()) {
                        numeroInstruccion++;

                        try {
                            System.out.println(
                                    "Ejecutando instrucción #" + numeroInstruccion
                                    + ":\n" + sql
                            );

                            sentencia.execute(sql);
                        } catch (SQLException excepcion) {
                            System.err.println(
                                    "Falló la instrucción #" + numeroInstruccion
                                    + ":\n" + sql
                            );
                            throw excepcion;
                        }
                    }
                }

                conexion.commit();
            } catch (SQLException excepcion) {
                conexion.rollback();
                throw excepcion;
            }
        }
    }

    private static void actualizarTablaRestaurante(Connection conexion)
            throws SQLException {

        boolean existeTabla = false;
        boolean existeUbicacion = false;

        try (Statement sentencia = conexion.createStatement();
             ResultSet columnas = sentencia.executeQuery(
                     "PRAGMA table_info(restaurante)")) {

            while (columnas.next()) {
                existeTabla = true;

                if ("ubicacion".equalsIgnoreCase(columnas.getString("name"))) {
                    existeUbicacion = true;
                }
            }
        }

        if (existeTabla && !existeUbicacion) {
            try (Statement sentencia = conexion.createStatement()) {
                sentencia.execute(SQL_AGREGAR_UBICACION);
            }
        }
    }

    private static String leerEsquema() throws SQLException {
        try (InputStream entrada = InicializadorBaseDatos.class
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
