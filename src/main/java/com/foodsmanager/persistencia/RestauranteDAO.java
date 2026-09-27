package com.foodsmanager.persistencia;

import com.foodsmanager.modelo.Restaurante;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RestauranteDAO {

    @FunctionalInterface
    public interface ProveedorConexion {
        Connection abrir() throws SQLException;
    }

    private final ProveedorConexion proveedorConexion;

    public RestauranteDAO() {
        this(ConexionSQLite::abrirConexion);
    }

    public RestauranteDAO(ProveedorConexion proveedorConexion) {
        this.proveedorConexion = Objects.requireNonNull(proveedorConexion);
    }

    private static final String SQL_INSERTAR = """
            INSERT INTO restaurante (nombre, ubicacion, descripcion)
            VALUES (?, ?, ?)
            """;

    private static final String SQL_BUSCAR_POR_ID = """
            SELECT id_restaurante, nombre, ubicacion, descripcion
            FROM restaurante
            WHERE id_restaurante = ?
            """;

    private static final String SQL_LISTAR_TODOS = """
            SELECT id_restaurante, nombre, ubicacion, descripcion
            FROM restaurante
            ORDER BY nombre COLLATE NOCASE, id_restaurante
            """;

    private static final String SQL_ACTUALIZAR_DESCRIPCION = """
            UPDATE restaurante
            SET descripcion = ?
            WHERE id_restaurante = ?
            """;

    /** Conserva las llamadas existentes que no proporcionan una descripción. */
    public int insertar(String nombre, String ubicacion) throws SQLException {
        return insertar(nombre, ubicacion, "");
    }

    /**
     * Registra un restaurante y devuelve el identificador asignado por SQLite.
     * El controlador administrativo debe exigir una sesion antes de llamarlo.
     */
    public int insertar(String nombre, String ubicacion, String descripcion)
            throws SQLException {
        String nombreValidado = validarTextoObligatorio(
                nombre, "El nombre del restaurante es obligatorio.");
        String ubicacionValidada = validarTextoObligatorio(
                ubicacion, "La ubicacion del restaurante es obligatoria.");

        try (Connection conexion = proveedorConexion.abrir()) {
            conexion.setAutoCommit(false);

            try {
                int idRestaurante;

                try (PreparedStatement sentencia = conexion.prepareStatement(
                        SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
                    sentencia.setString(1, nombreValidado);
                    sentencia.setString(2, ubicacionValidada);
                    sentencia.setString(3, normalizarDescripcion(descripcion));

                    if (sentencia.executeUpdate() != 1) {
                        throw new SQLException("No se pudo registrar el restaurante.");
                    }

                    try (ResultSet claves = sentencia.getGeneratedKeys()) {
                        if (!claves.next()) {
                            throw new SQLException("SQLite no devolvio el id del restaurante.");
                        }

                        idRestaurante = claves.getInt(1);
                        if (idRestaurante <= 0) {
                            throw new SQLException("SQLite devolvio un id de restaurante invalido.");
                        }
                    }
                }

                conexion.commit();
                return idRestaurante;
            } catch (SQLException excepcion) {
                try {
                    conexion.rollback();
                } catch (SQLException errorRollback) {
                    excepcion.addSuppressed(errorRollback);
                }
                throw excepcion;
            }
        }
    }

    /** Actualiza solo la descripción, conservando el restaurante y sus relaciones. */
    public void actualizarDescripcion(int idRestaurante, String descripcion)
            throws SQLException {
        validarId(idRestaurante);
        try (Connection conexion = proveedorConexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ACTUALIZAR_DESCRIPCION)) {
            sentencia.setString(1, normalizarDescripcion(descripcion));
            sentencia.setInt(2, idRestaurante);
            if (sentencia.executeUpdate() != 1) {
                throw new SQLException("El restaurante ya no existe o no se pudo actualizar.");
            }
        }
    }

    /** Devuelve el restaurante encontrado, o null si el id no existe. */
    public Restaurante buscarPorId(int idRestaurante) throws SQLException {
        validarId(idRestaurante);

        try (Connection conexion = proveedorConexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_BUSCAR_POR_ID)) {
            sentencia.setInt(1, idRestaurante);

            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? convertirEnRestaurante(resultado) : null;
            }
        }
    }

    /** Obtiene todos los restaurantes registrados, ordenados por nombre. */
    public List<Restaurante> listarTodos() throws SQLException {
        List<Restaurante> restaurantes = new ArrayList<>();

        try (Connection conexion = proveedorConexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_LISTAR_TODOS);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                restaurantes.add(convertirEnRestaurante(resultado));
            }
        }

        return restaurantes;
    }

    private Restaurante convertirEnRestaurante(ResultSet resultado) throws SQLException {
        return new Restaurante(
                resultado.getInt("id_restaurante"),
                resultado.getString("nombre"),
                resultado.getString("ubicacion"),
                resultado.getString("descripcion")
        );
    }

    private static void validarId(int idRestaurante) {
        if (idRestaurante <= 0) {
            throw new IllegalArgumentException(
                    "El id del restaurante debe ser mayor que cero.");
        }
    }

    private static String normalizarDescripcion(String descripcion) {
        return descripcion == null ? "" : descripcion.trim();
    }

    private static String validarTextoObligatorio(String texto, String mensaje) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException(mensaje);
        }
        return texto.trim();
    }
}
