package com.foodsmanager.persistencia;

import com.foodsmanager.modelo.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProductoDAO {

    @FunctionalInterface
    public interface ProveedorConexion {
        Connection abrir() throws SQLException;
    }

    private final ProveedorConexion proveedorConexion;

    public ProductoDAO() {
        this(ConexionSQLite::abrirConexion);
    }

    public ProductoDAO(ProveedorConexion proveedorConexion) {
        this.proveedorConexion = Objects.requireNonNull(proveedorConexion);
    }

    private static final String SQL_INSERTAR = """
            INSERT INTO producto (
                id_restaurante, nombre, descripcion, precio, disponible
            ) VALUES (?, ?, ?, ?, ?)
            """;

    private static final String SQL_BUSCAR_POR_ID = """
            SELECT id_producto, id_restaurante, nombre, descripcion, precio, disponible
            FROM producto
            WHERE id_producto = ?
            """;

    private static final String SQL_LISTAR_POR_RESTAURANTE = """
            SELECT id_producto, id_restaurante, nombre, descripcion, precio, disponible
            FROM producto
            WHERE id_restaurante = ?
            ORDER BY nombre COLLATE NOCASE, id_producto
            """;

    private static final String SQL_LISTAR_TODOS = """
            SELECT id_producto, id_restaurante, nombre, descripcion, precio, disponible
            FROM producto
            ORDER BY nombre COLLATE NOCASE, id_producto
            """;

    private static final String SQL_ACTUALIZAR_NOMBRE_Y_DISPONIBILIDAD = """
            UPDATE producto
            SET nombre = ?, disponible = ?
            WHERE id_producto = ?
            """;

    public static String getSQL_BUSCAR_POR_ID() {
        return SQL_BUSCAR_POR_ID;
    }

    public int insertar(Producto producto) throws SQLException {
        if (producto == null) {
            throw new IllegalArgumentException("El producto es obligatorio.");
        }
        if (producto.getIdProducto() != 0) {
            throw new IllegalArgumentException("El producto ya tiene un identificador asignado.");
        }

        try (Connection conexion = proveedorConexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(
                     SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setInt(1, producto.getIdRestaurante());
            sentencia.setString(2, producto.getNombre());
            sentencia.setString(3, producto.getDescripcion());
            sentencia.setDouble(4, producto.getPrecio());
            sentencia.setBoolean(5, producto.isDisponible());

            if (sentencia.executeUpdate() != 1) {
                throw new SQLException("No se pudo registrar el producto.");
            }
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException("SQLite no devolvió el id del producto.");
                }
                int id = claves.getInt(1);
                producto.setIdProducto(id);
                return id;
            }
        }
    }

    public Producto buscarPorId(int idProducto) throws SQLException {
        validarId(idProducto);
        try (Connection conexion = proveedorConexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_BUSCAR_POR_ID)) {
            sentencia.setInt(1, idProducto);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? convertirEnProducto(resultado) : null;
            }
        }
    }

    public List<Producto> listarPorRestaurante(int idRestaurante) throws SQLException {
        if (idRestaurante <= 0) {
            throw new IllegalArgumentException("El id del restaurante debe ser mayor que cero.");
        }
        try (Connection conexion = proveedorConexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_LISTAR_POR_RESTAURANTE)) {
            sentencia.setInt(1, idRestaurante);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return convertirLista(resultado);
            }
        }
    }

    /** Incluye todos los restaurantes y ambos estados de disponibilidad. */
    public List<Producto> listarTodos() throws SQLException {
        try (Connection conexion = proveedorConexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_LISTAR_TODOS);
             ResultSet resultado = sentencia.executeQuery()) {
            return convertirLista(resultado);
        }
    }

    /** Actualiza solo estos dos campos, conservando el resto y las referencias. */
    public void actualizarNombreYDisponibilidad(
            int idProducto, String nombre, boolean disponible) throws SQLException {
        validarId(idProducto);
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
        try (Connection conexion = proveedorConexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(
                     SQL_ACTUALIZAR_NOMBRE_Y_DISPONIBILIDAD)) {
            sentencia.setString(1, nombre.trim());
            sentencia.setBoolean(2, disponible);
            sentencia.setInt(3, idProducto);
            if (sentencia.executeUpdate() != 1) {
                throw new SQLException("El producto ya no existe o no se pudo actualizar.");
            }
        }
    }

    private static void validarId(int idProducto) {
        if (idProducto <= 0) {
            throw new IllegalArgumentException("El id del producto debe ser mayor que cero.");
        }
    }

    private List<Producto> convertirLista(ResultSet resultado) throws SQLException {
        List<Producto> productos = new ArrayList<>();
        while (resultado.next()) {
            productos.add(convertirEnProducto(resultado));
        }
        return productos;
    }

    private Producto convertirEnProducto(ResultSet resultado) throws SQLException {
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