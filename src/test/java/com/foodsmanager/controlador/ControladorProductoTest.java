package com.foodsmanager.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.foodsmanager.modelo.Producto;
import com.foodsmanager.persistencia.ConexionSQLite;
import com.foodsmanager.persistencia.InicializadorBaseDatos;
import com.foodsmanager.persistencia.ProductoDAO;
import com.foodsmanager.persistencia.RestauranteDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ControladorProductoTest {

    @BeforeAll
    static void prepararBaseDatos() throws SQLException {
        InicializadorBaseDatos.inicializar();
    }

    @Test
    void debeRegistrarUnProducto() throws SQLException {
        ControladorProducto controlador
                = new ControladorProducto(
                        new ProductoDAO(),
                        new RestauranteDAO()
                );

        Producto producto = null;

        try {
            producto = controlador.registrarProducto(
                    1,
                    "Tamal colorado",
                    "Tamal de prueba",
                    "18,50",
                    true
            );

            assertTrue(producto.getIdProducto() > 0);
            assertEquals(1, producto.getIdRestaurante());
            assertEquals("Tamal colorado", producto.getNombre());
            assertEquals(18.50, producto.getPrecio(), 0.001);
            assertTrue(producto.isDisponible());

            Producto productoGuardado
                    = new ProductoDAO().buscarPorId(
                            producto.getIdProducto()
                    );

            assertNotNull(productoGuardado);
            assertEquals(
                    producto.getNombre(),
                    productoGuardado.getNombre()
            );
        } finally {
            if (producto != null
                    && producto.getIdProducto() > 0) {

                eliminarProductoDePrueba(
                        producto.getIdProducto()
                );
            }
        }
    }

    @Test
    void debeRechazarUnPrecioInvalido() {
        ControladorProducto controlador
                = new ControladorProducto();

        IllegalArgumentException excepcion
                = assertThrows(
                        IllegalArgumentException.class,
                        () -> controlador.registrarProducto(
                                1,
                                "Producto inválido",
                                "",
                                "precio incorrecto",
                                true
                        )
                );

        assertEquals(
                "El precio ingresado no es válido.",
                excepcion.getMessage()
        );
    }

    private static void eliminarProductoDePrueba(int idProducto)
            throws SQLException {

        String sql = """
                DELETE FROM producto
                WHERE id_producto = ?
                """;

        try (Connection conexion
                = ConexionSQLite.abrirConexion(); PreparedStatement sentencia
                = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idProducto);
            sentencia.executeUpdate();
        }
    }
}
