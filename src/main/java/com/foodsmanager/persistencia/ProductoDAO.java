package com.foodsmanager.persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.foodsmanager.modelo.Producto;


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
     * TK-06:
     * Consulta los productos pertenecientes a un restaurante.
     */
    private static final String SQL_LISTAR_POR_RESTAURANTE = """
            SELECT
                id_producto,
                id_restaurante,
                nombre,
                descripcion,
                precio,
                disponible
            FROM producto
            WHERE id_restaurante = ?
            ORDER BY nombre COLLATE NOCASE
            """;

    public static String getSQL_BUSCAR_POR_ID() {
        return SQL_BUSCAR_POR_ID;
    }

    /**
     * Inserta un producto nuevo y devuelve el identificador generado.
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

        try (Connection conexion = ConexionSQLite.abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(
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

            try (ResultSet clavesGeneradas =
                         sentencia.getGeneratedKeys()) {

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

    /**
     * Busca un producto mediante su identificador.
     *
     * @return el producto encontrado o null si no existe
     */
    public Producto buscarPorId(int idProducto)
            throws SQLException {

        if (idProducto <= 0) {
            throw new IllegalArgumentException(
                    "El id del producto debe ser mayor que cero.");
        }

        try (Connection conexion = ConexionSQLite.abrirConexion();
             PreparedStatement sentencia =
                     conexion.prepareStatement(SQL_BUSCAR_POR_ID)) {

            sentencia.setInt(1, idProducto);

            try (ResultSet resultado = sentencia.executeQuery()) {

                if (resultado.next()) {
                    return convertirEnProducto(resultado);
                }

                return null;
            }
        }
    }

    /**
     * TK-06.
     *
     * Lista únicamente los productos que pertenecen
     * al restaurante indicado.
     *
     * Si el restaurante no tiene productos,
     * devuelve una lista vacía.
     *
     * @param idRestaurante identificador del restaurante
     * @return lista de productos del restaurante
     * @throws SQLException si ocurre un error en SQLite
     */
    public List<Producto> listarPorRestaurante(int idRestaurante)
            throws SQLException {

        if (idRestaurante <= 0) {
            throw new IllegalArgumentException(
                    "El id del restaurante debe ser mayor que cero.");
        }

        List<Producto> productos = new ArrayList<>();

        try (Connection conexion = ConexionSQLite.abrirConexion();
             PreparedStatement sentencia =
                     conexion.prepareStatement(
                             SQL_LISTAR_POR_RESTAURANTE)) {

            /*
             * El signo ? del SQL se reemplaza
             * de forma segura con el id del restaurante.
             *
             * Esto hace que la consulta sea parametrizada.
             */
            sentencia.setInt(1, idRestaurante);

            try (ResultSet resultado =
                         sentencia.executeQuery()) {

                while (resultado.next()) {

                    Producto producto =
                            convertirEnProducto(resultado);

                    productos.add(producto);
                }
            }
        }

        return productos;
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