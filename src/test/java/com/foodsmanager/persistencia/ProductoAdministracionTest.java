package com.foodsmanager.persistencia;

import com.foodsmanager.controlador.ControladorListadoProductos;
import com.foodsmanager.modelo.Administrador;
import com.foodsmanager.modelo.Producto;
import com.foodsmanager.seguridad.SesionAdministrador;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class ProductoAdministracionTest {

    @TempDir Path carpeta;
    private String url;
    private ProductoDAO dao;

    @BeforeEach
    void preparar() throws Exception {
        SesionAdministrador.cerrar();
        url = "jdbc:sqlite:" + carpeta.resolve("administracion.db");
        dao = new ProductoDAO(this::abrirConexion);
        try (Connection conexion = abrirConexion(); Statement sentencia = conexion.createStatement()) {
            sentencia.execute("CREATE TABLE restaurante (id_restaurante INTEGER PRIMARY KEY)");
            sentencia.execute("INSERT INTO restaurante VALUES (1), (2)");
            sentencia.execute("""
                    CREATE TABLE producto (
                        id_producto INTEGER PRIMARY KEY AUTOINCREMENT,
                        id_restaurante INTEGER NOT NULL REFERENCES restaurante(id_restaurante),
                        nombre TEXT NOT NULL, descripcion TEXT,
                        precio REAL NOT NULL CHECK (precio >= 0),
                        disponible INTEGER NOT NULL CHECK (disponible IN (0, 1))
                    )
                    """);
            sentencia.execute("""
                    INSERT INTO producto VALUES
                        (1, 1, 'Pepián', 'Descripción original', 45.50, 1),
                        (2, 2, 'Pizza', 'Otra descripción', 65.00, 0)
                    """);
            sentencia.execute("""
                    CREATE TABLE detalle_prueba (
                        id_producto INTEGER NOT NULL REFERENCES producto(id_producto),
                        precio_unitario REAL NOT NULL
                    )
                    """);
            sentencia.execute("INSERT INTO detalle_prueba VALUES (1, 45.50)");
        }
    }

    @AfterEach
    void limpiar() {
        SesionAdministrador.cerrar();
    }

    private Connection abrirConexion() throws SQLException {
        Connection conexion = DriverManager.getConnection(url);
        try (Statement sentencia = conexion.createStatement()) {
            sentencia.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException error) {
            conexion.close();
            throw error;
        }
        return conexion;
    }

    @Test
    void listaTodosLosRestaurantesYAmbosEstados() throws Exception {
        List<Producto> productos = dao.listarTodos();
        assertEquals(2, productos.size());
        assertTrue(productos.stream().anyMatch(p -> p.getIdRestaurante() == 1 && p.isDisponible()));
        assertTrue(productos.stream().anyMatch(p -> p.getIdRestaurante() == 2 && !p.isDisponible()));
    }

    @Test
    void editaSinBorrarNiCambiarOtrosCamposNiReferencias() throws Exception {
        dao.actualizarNombreYDisponibilidad(1, "  Pepián especial  ", false);
        Producto producto = dao.buscarPorId(1);
        assertEquals("Pepián especial", producto.getNombre());
        assertFalse(producto.isDisponible());
        assertEquals(1, producto.getIdRestaurante());
        assertEquals("Descripción original", producto.getDescripcion());
        assertEquals(45.50, producto.getPrecio(), 0.0001);
        assertEquals(2, dao.listarTodos().size());
        assertEquals("Pizza", dao.buscarPorId(2).getNombre());
        try (Connection conexion = abrirConexion(); Statement sentencia = conexion.createStatement();
             var filas = sentencia.executeQuery("SELECT * FROM detalle_prueba")) {
            assertTrue(filas.next());
            assertEquals(1, filas.getInt("id_producto"));
            assertEquals(45.50, filas.getDouble("precio_unitario"), 0.0001);
            assertFalse(filas.next());
        }
    }

    @Test
    void permiteVolverADisponibleYConservaLaConsultaPublica() throws Exception {
        dao.actualizarNombreYDisponibilidad(2, "Pizza", true);
        assertTrue(dao.buscarPorId(2).isDisponible());
        dao.actualizarNombreYDisponibilidad(2, "Pizza", false);
        List<Producto> productos = dao.listarPorRestaurante(2);
        assertEquals(1, productos.size());
        assertFalse(productos.get(0).isDisponible());
    }

    @Test
    void rechazaNombreVacioSinModificarLaFila() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> dao.actualizarNombreYDisponibilidad(1, "  ", false));
        assertThrows(IllegalArgumentException.class,
                () -> dao.actualizarNombreYDisponibilidad(1, null, false));
        assertEquals("Pepián", dao.buscarPorId(1).getNombre());
        assertTrue(dao.buscarPorId(1).isDisponible());
    }

    @Test
    void informaCuandoElProductoNoExiste() {
        assertThrows(SQLException.class,
                () -> dao.actualizarNombreYDisponibilidad(999, "No existe", false));
    }

    @Test
    void controladorExigeSesionParaConsultarYEditar() {
        ControladorListadoProductos controlador = new ControladorListadoProductos(dao, new RestauranteDAO());
        assertThrows(SecurityException.class, controlador::listarProductos);
        assertThrows(SecurityException.class,
                () -> controlador.actualizarProducto(1, "No autorizado", false));
    }

    @Test
    void controladorValidaYGuardaConSesionSinNecesitarVentanas() throws Exception {
        SesionAdministrador.iniciar(new Administrador(1, "admin_prueba", "hash_prueba"));
        ControladorListadoProductos controlador = new ControladorListadoProductos(dao, new RestauranteDAO());
        assertEquals(2, controlador.listarProductos().size());
        assertThrows(IllegalArgumentException.class,
                () -> controlador.actualizarProducto(1, " ", false));
        Producto actualizado = controlador.actualizarProducto(1, "  Pepián nuevo  ", false);
        assertEquals("Pepián nuevo", actualizado.getNombre());
        assertFalse(actualizado.isDisponible());
        assertEquals("Pepián nuevo", dao.buscarPorId(1).getNombre());
        SesionAdministrador.cerrar();
        assertThrows(SecurityException.class,
                () -> controlador.actualizarProducto(1, "Después de cerrar", true));
    }
}
