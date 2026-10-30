package com.foodsmanager.persistencia;

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

class PedidoUsuarioMigracionTest {
    @TempDir Path carpeta;
    private String url;

    @BeforeEach
    void crearBaseAnterior() throws SQLException {
        url = "jdbc:sqlite:" + carpeta.resolve("anterior.db");
        ejecutar("""
                CREATE TABLE pedido (
                    id_pedido INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre_cliente TEXT NOT NULL, telefono TEXT NOT NULL,
                    persona_recoge TEXT NOT NULL, hora_recogida TEXT NOT NULL,
                    indicaciones TEXT, fecha_pedido TEXT NOT NULL,
                    total REAL NOT NULL, estado TEXT NOT NULL DEFAULT 'PAGADO'
                )
                """);
        ejecutar("""
                CREATE TABLE usuario_estandar (
                    id_usuario INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL,
                    telefono TEXT NOT NULL UNIQUE, usuario TEXT NOT NULL UNIQUE COLLATE NOCASE,
                    contrasena_hash TEXT NOT NULL
                )
                """);
        ejecutar("INSERT INTO usuario_estandar VALUES (1, 'Ana', '12345678', 'ana', 'hash')");
        ejecutar("""
                INSERT INTO pedido VALUES
                (10, 'Ana', '12345678', 'Ana', '12:00', 'Sin cubiertos', '2026-10-20T10:00', 45, 'PAGADO'),
                (11, 'Otro nombre', '12345678', 'Ana', '12:00', '', '2026-10-20T10:01', 40, 'LISTO'),
                (12, 'Luis', '87654321', 'Luis', '13:00', '', '2026-10-20T10:02', 18, 'PAGADO')
                """);
    }

    private Connection abrirConexion() throws SQLException {
        Connection conexion = DriverManager.getConnection(url);
        try (Statement sentencia = conexion.createStatement()) {
            sentencia.execute("PRAGMA foreign_keys = ON");
        }
        return conexion;
    }

    private void ejecutar(String sql) throws SQLException {
        try (Connection conexion = abrirConexion(); Statement sentencia = conexion.createStatement()) {
            sentencia.execute(sql);
        }
    }

    @Test
    void migraSoloCoincidenciasExactasYConservaPedidosYDetalles() throws SQLException {
        // Tablas previas del catálogo y detalle, con un producto ajeno a los datos iniciales.
        ejecutar("CREATE TABLE restaurante (id_restaurante INTEGER PRIMARY KEY, nombre TEXT, ubicacion TEXT, descripcion TEXT)");
        ejecutar("INSERT INTO restaurante VALUES (99, 'Local previo', 'Zona 1', 'Descripción')");
        ejecutar("""
                CREATE TABLE producto (id_producto INTEGER PRIMARY KEY, id_restaurante INTEGER,
                    nombre TEXT, descripcion TEXT, precio REAL, disponible INTEGER)
                """);
        ejecutar("INSERT INTO producto VALUES (99, 99, 'Producto previo', '', 45, 1)");
        ejecutar("""
                CREATE TABLE detalle_pedido (id_detalle INTEGER PRIMARY KEY, id_pedido INTEGER,
                    id_producto INTEGER, cantidad INTEGER, precio_unitario REAL, subtotal REAL)
                """);
        ejecutar("INSERT INTO detalle_pedido VALUES (50, 10, 99, 1, 45, 45)");

        InicializadorBaseDatos.inicializar(this::abrirConexion);
        InicializadorBaseDatos.inicializar(this::abrirConexion);
        try (Connection conexion = abrirConexion(); Statement sentencia = conexion.createStatement();
             ResultSet filas = sentencia.executeQuery("SELECT * FROM pedido ORDER BY id_pedido")) {
            assertTrue(filas.next());
            assertEquals(10, filas.getInt("id_pedido"));
            assertEquals(1, filas.getInt("id_usuario"));
            assertEquals("Sin cubiertos", filas.getString("indicaciones"));
            assertEquals(45, filas.getDouble("total"), 0.001);
            assertTrue(filas.next());
            assertNull(filas.getObject("id_usuario"));
            assertEquals("LISTO", filas.getString("estado"));
            assertTrue(filas.next());
            assertNull(filas.getObject("id_usuario"));
            assertFalse(filas.next());
        }
        PedidoDAO dao = new PedidoDAO(this::abrirConexion);
        assertEquals(1, dao.listarPorUsuario(1).size());
        assertEquals(50, dao.listarPorUsuario(1).get(0).getDetalles().get(0).getIdDetalle());
    }

    @Test
    void noAsignaPedidosHuerfanosACuentasRegistradasDespuesDeMigrar() throws SQLException {
        InicializadorBaseDatos.inicializar(this::abrirConexion);
        ejecutar("INSERT INTO usuario_estandar VALUES (2, 'Luis', '87654321', 'luis', 'hash')");
        InicializadorBaseDatos.inicializar(this::abrirConexion);
        assertTrue(new PedidoDAO(this::abrirConexion).listarPorUsuario(2).isEmpty());
        try (Connection conexion = abrirConexion(); Statement sentencia = conexion.createStatement();
             ResultSet filas = sentencia.executeQuery("SELECT id_usuario FROM pedido WHERE id_pedido = 12")) {
            assertTrue(filas.next());
            assertNull(filas.getObject(1));
        }
    }

    @Test
    void laRelacionMigradaRechazaCuentasInexistentes() throws SQLException {
        InicializadorBaseDatos.inicializar(this::abrirConexion);
        assertThrows(SQLException.class,
                () -> ejecutar("UPDATE pedido SET id_usuario = 999 WHERE id_pedido = 10"));
    }
}
