package com.foodsmanager.persistencia;

import com.foodsmanager.modelo.Restaurante;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class RestauranteDescripcionMigracionTest {

    @TempDir Path carpeta;
    private String url;
    private RestauranteDAO dao;

    @BeforeEach
    void preparar() {
        url = "jdbc:sqlite:" + carpeta.resolve("migracion.db");
        dao = new RestauranteDAO(this::abrirConexion);
    }

    private Connection abrirConexion() throws SQLException {
        Connection conexion = DriverManager.getConnection(url);
        try (Statement sentencia = conexion.createStatement()) {
            sentencia.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException excepcion) {
            conexion.close();
            throw excepcion;
        }
        return conexion;
    }

    @Test
    void debeCrearUnaBaseNuevaConLasDescripcionesOriginales() throws SQLException {
        InicializadorBaseDatos.inicializar(this::abrirConexion);

        assertEquals(3, dao.listarTodos().size());
        assertEquals("Comida tradicional guatemalteca.", dao.buscarPorId(1).getDescripcion());
        assertEquals("Pizzas y especialidades para compartir.", dao.buscarPorId(2).getDescripcion());
        assertEquals("Hamburguesas, acompañamientos y bebidas.", dao.buscarPorId(3).getDescripcion());
    }

    @Test
    void debeMigrarSinPerderElRestauranteAgregadoNiSusProductos() throws SQLException {
        try (Connection conexion = abrirConexion();
             Statement sentencia = conexion.createStatement()) {
            sentencia.execute("""
                    CREATE TABLE restaurante (
                        id_restaurante INTEGER PRIMARY KEY AUTOINCREMENT,
                        nombre TEXT NOT NULL,
                        ubicacion TEXT NOT NULL
                    )
                    """);
            sentencia.execute("""
                    INSERT INTO restaurante VALUES
                        (1, 'Sabor Chapín', 'Zona 1'),
                        (2, 'Pizzería Central', 'Zona 4'),
                        (3, 'Burger House', 'Zona 10'),
                        (4, 'El Comal', 'Zona 7')
                    """);
            sentencia.execute("""
                    CREATE TABLE producto (
                        id_producto INTEGER PRIMARY KEY AUTOINCREMENT,
                        id_restaurante INTEGER NOT NULL,
                        nombre TEXT NOT NULL,
                        descripcion TEXT,
                        precio REAL NOT NULL,
                        disponible INTEGER NOT NULL DEFAULT 1,
                        FOREIGN KEY (id_restaurante) REFERENCES restaurante(id_restaurante)
                    )
                    """);
            sentencia.execute("""
                    INSERT INTO producto VALUES (99, 4, 'Almuerzo', 'Menú del día', 30, 1)
                    """);
        }

        InicializadorBaseDatos.inicializar(this::abrirConexion);
        InicializadorBaseDatos.inicializar(this::abrirConexion);

        Restaurante agregado = dao.buscarPorId(4);
        assertEquals(4, dao.listarTodos().size());
        assertEquals("El Comal", agregado.getNombre());
        assertEquals("Zona 7", agregado.getUbicacion());
        assertEquals("", agregado.getDescripcion());
        assertEquals("Comida tradicional guatemalteca.", dao.buscarPorId(1).getDescripcion());
        try (Connection conexion = abrirConexion();
             Statement sentencia = conexion.createStatement();
             ResultSet fila = sentencia.executeQuery("SELECT * FROM producto WHERE id_producto = 99")) {
            assertTrue(fila.next());
            assertEquals(4, fila.getInt("id_restaurante"));
            assertEquals("Almuerzo", fila.getString("nombre"));
            assertEquals("Menú del día", fila.getString("descripcion"));
            assertEquals(30.0, fila.getDouble("precio"), 0.0001);
            assertEquals(1, fila.getInt("disponible"));
        }
    }

    @Test
    void debeRespetarLasDescripcionesEditadasYVaciadasAlReiniciar() throws SQLException {
        InicializadorBaseDatos.inicializar(this::abrirConexion);
        dao.actualizarDescripcion(1, "Descripción personalizada.");
        dao.actualizarDescripcion(2, "");
        int id = dao.insertar("Nuevo", "Zona 6", "Café y postres.");

        InicializadorBaseDatos.inicializar(this::abrirConexion);
        InicializadorBaseDatos.inicializar(this::abrirConexion);

        assertEquals("Descripción personalizada.", dao.buscarPorId(1).getDescripcion());
        assertEquals("", dao.buscarPorId(2).getDescripcion());
        assertEquals("Café y postres.", dao.buscarPorId(id).getDescripcion());
    }

    @Test
    void debeActualizarTambienUnaBaseAntiguaSinUbicacion() throws SQLException {
        try (Connection conexion = abrirConexion();
             Statement sentencia = conexion.createStatement()) {
            sentencia.execute("""
                    CREATE TABLE restaurante (
                        id_restaurante INTEGER PRIMARY KEY AUTOINCREMENT,
                        nombre TEXT NOT NULL
                    )
                    """);
            sentencia.execute("INSERT INTO restaurante VALUES (1, 'Comida guatemalteca')");
        }

        InicializadorBaseDatos.inicializar(this::abrirConexion);

        Restaurante restaurante = dao.buscarPorId(1);
        assertEquals("Sabor Chapín", restaurante.getNombre());
        assertEquals("Zona 1", restaurante.getUbicacion());
        assertEquals("Comida tradicional guatemalteca.", restaurante.getDescripcion());
    }
}
