package com.foodsmanager.persistencia;

import com.foodsmanager.modelo.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/*
  Gestiona la persistencia de productos en SQLite.
 */
public class ProductoDAO {

    private static final String SQL_INSERTAR = """
            INSERT INTO producto (
                id_restaurante,
                nombre,
                descripcion,
                precio,
                disponible
            ) VALUES (?, ?, ?, ?, ?)
            """;

    private static final String SQL_BUSCAR_POR_ID = """
            SELECT
                id_producto,
                id_restaurante,
                nombre,
                descripcion,
                precio,
                disponible
            FROM producto
            WHERE id_producto = ?
            """;

    /*
     Inserta un producto nuevo y devuelve el identificador generado.
     */
    public int insertar(Producto producto) throws SQLException {
        if (producto == null) {
            throw new IllegalArgumentException(
                    "El producto es obligatorio.");
        }

        if (producto.getIdProducto() != 0) {
            throw new IllegalArgumentException(
                    "El producto ya tiene un identificador asignado.");
        }

        try (Connection conexion = ConexionSQLite.abrirConexion(); PreparedStatement sentencia = conexion.prepareStatement(
                SQL_INSERTAR,
                Statement.RETURN_GENERATED_KEYS)) {

            sentencia.setInt(
                    1,
                    producto.getIdRestaurante()
            );
            sentencia.setString(
                    2,
                    producto.getNombre()
            );
            sentencia.setString(
                    3,
                    producto.getDescripcion()
            );
            sentencia.setDouble(
                    4,
                    producto.getPrecio()
            );
            sentencia.setBoolean(
                    5,
                    producto.isDisponible()
            );

            int filasInsertadas = sentencia.executeUpdate();

            if (filasInsertadas != 1) {
                throw new SQLException(
                        "No se pudo registrar el producto.");
            }

            try (ResultSet clavesGeneradas
                    = sentencia.getGeneratedKeys()) {

                if (!clavesGeneradas.next()) {
                    throw new SQLException(
                            "SQLite no devolvió el id del producto.");
                }

                int idGenerado = clavesGeneradas.getInt(1);
                producto.setIdProducto(idGenerado);

                return idGenerado;
            }
        }
    }

    /*
      Busca un producto mediante su identificador.
     
      @return el producto encontrado o null si no existe
     */
    public Producto buscarPorId(int idProducto)
            throws SQLException {

        if (idProducto <= 0) {
            throw new IllegalArgumentException(
                    "El id del producto debe ser mayor que cero.");
        }

        try (Connection conexion = ConexionSQLite.abrirConexion(); PreparedStatement sentencia
                = conexion.prepareStatement(SQL_BUSCAR_POR_ID)) {

            sentencia.setInt(1, idProducto);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return convertirEnProducto(resultado);
                }

                return null;
            }
        }
    }

    private Producto convertirEnProducto(ResultSet resultado)
            throws SQLException {

        return new Producto(
                resultado.getInt("id_producto"),
                resultado.getInt("id_restaurante"),
                resultado.getString("nombre"),
                resultado.getString("descripcion"),
                resultado.getDouble("precio"),
                resultado.getBoolean("disponible")
        );
    }
}
