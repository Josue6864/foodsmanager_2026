package com.foodsmanager.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class InicializadorBaseDatosTest {

    @BeforeAll
    static void prepararBaseDatos() throws SQLException {
        InicializadorBaseDatos.inicializar();
    }

    @Test
    void debeCrearLasTablasEsperadas() throws SQLException {
        Set<String> tablasEncontradas = new HashSet<>();

        String sql = """
                SELECT name
                FROM sqlite_master
                WHERE type = 'table'
                  AND name IN (
                      'restaurante',
                      'producto',
                      'administrador'
                  )
                """;

        try (Connection conexion = ConexionSQLite.abrirConexion(); Statement sentencia = conexion.createStatement(); ResultSet resultado = sentencia.executeQuery(sql)) {

            while (resultado.next()) {
                tablasEncontradas.add(resultado.getString("name"));
            }
        }

        Set<String> tablasEsperadas = Set.of(
                "restaurante",
                "producto",
                "administrador"
        );

        assertEquals(tablasEsperadas, tablasEncontradas);
    }

    @Test
    void debeActivarLasLlavesForaneas() throws SQLException {
        try (Connection conexion = ConexionSQLite.abrirConexion(); Statement sentencia = conexion.createStatement(); ResultSet resultado
                = sentencia.executeQuery("PRAGMA foreign_keys")) {

            assertTrue(resultado.next());
            assertEquals(1, resultado.getInt(1));
        }
    }

    @Test
    void debeCrearLosRestaurantesInicialesSinDuplicarlos()
            throws SQLException {

        // Volvemos a inicializar para comprobar que no se duplican.
        InicializadorBaseDatos.inicializar();
        InicializadorBaseDatos.inicializar();

        Set<String> restaurantesEncontrados = new HashSet<>();

        String sql = """
            SELECT id_restaurante, nombre, ubicacion
            FROM restaurante
            WHERE id_restaurante IN (1, 2, 3)
            ORDER BY id_restaurante
            """;

        try (Connection conexion = ConexionSQLite.abrirConexion(); Statement sentencia = conexion.createStatement(); ResultSet resultado = sentencia.executeQuery(sql)) {

            while (resultado.next()) {
                restaurantesEncontrados.add(
                        resultado.getInt("id_restaurante")
                        + "|"
                        + resultado.getString("nombre")
                        + "|"
                        + resultado.getString("ubicacion")
                );
            }
        }

        Set<String> restaurantesEsperados = Set.of(
                "1|Sabor Chapín|Zona 1",
                "2|Pizzería Central|Zona 4",
                "3|Burger House|Zona 10"
        );

        assertEquals(
                restaurantesEsperados,
                restaurantesEncontrados
        );
    }
}
