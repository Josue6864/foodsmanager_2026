package com.foodsmanager.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.foodsmanager.modelo.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ProductoDAOTest {

    @BeforeAll
    static void prepararBaseDatos() throws SQLException {
        InicializadorBaseDatos.inicializar();
    }

    @Test
    void debeInsertarYBuscarUnProducto() throws SQLException {
        ProductoDAO productoDAO = new ProductoDAO();

        Producto productoNuevo = new Producto(
                1,
                "Producto de prueba",
                "Producto registrado desde ProductoDAOTest",
                25.50,
                true
        );

        int idGenerado = 0;

        try {
            idGenerado = productoDAO.insertar(productoNuevo);

            assertTrue(idGenerado > 0);
            assertEquals(
                    idGenerado,
                    productoNuevo.getIdProducto()
            );

            Producto productoGuardado =
                    productoDAO.buscarPorId(idGenerado);

            assertNotNull(productoGuardado);
            assertEquals(
                    1,
                    productoGuardado.getIdRestaurante()
            );
            assertEquals(
                    "Producto de prueba",
                    productoGuardado.getNombre()
            );
            assertEquals(
                    "Producto registrado desde ProductoDAOTest",
                    productoGuardado.getDescripcion()
            );
            assertEquals(
                    25.50,
                    productoGuardado.getPrecio(),
                    0.001
            );
            assertTrue(productoGuardado.isDisponible());
        } finally {
            if (idGenerado > 0) {
                eliminarProductoDePrueba(idGenerado);
            }
        }
    }

    private static void eliminarProductoDePrueba(int idProducto)
            throws SQLException {

        String sql = """
                DELETE FROM producto
                WHERE id_producto = ?
                """;

        try (Connection conexion = ConexionSQLite.abrirConexion();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idProducto);
            sentencia.executeUpdate();
        }
    }
}
