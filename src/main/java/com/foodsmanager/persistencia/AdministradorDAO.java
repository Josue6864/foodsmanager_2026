package com.foodsmanager.persistencia;

import com.foodsmanager.modelo.Administrador;
import com.foodsmanager.persistencia.ConexionSQLite;
import com.foodsmanager.seguridad.Contrasenas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class AdministradorDAO {

    @FunctionalInterface
    public interface ProveedorConexion {
        Connection abrir() throws SQLException;
    }

    private final ProveedorConexion proveedorConexion;

    public AdministradorDAO() {
        this(ConexionSQLite::abrirConexion);
    }

    public AdministradorDAO(ProveedorConexion proveedorConexion) {
        this.proveedorConexion = Objects.requireNonNull(
                proveedorConexion
        );
    }

    public Administrador buscarPorUsuario(String usuario)
            throws SQLException {

        if (usuario == null || usuario.isBlank()) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio."
            );
        }

        String sql = """
                SELECT id_administrador, usuario, contrasena_hash
                FROM administrador
                WHERE usuario = ? COLLATE NOCASE
                """;

        try (Connection conexion = proveedorConexion.abrir();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setString(1, usuario.trim());

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) {
                    return null;
                }

                return new Administrador(
                        resultado.getInt("id_administrador"),
                        resultado.getString("usuario"),
                        resultado.getString("contrasena_hash")
                );
            }
        }
    }

    /**
      Devuelve true si se insertó la cuenta.
      Devuelve false si el usuario ya existía.
     */
    public boolean crearSiNoExiste(
            String usuario,
            String contrasena) throws SQLException {

        if (usuario == null || usuario.isBlank()) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio."
            );
        }

        if (contrasena == null || contrasena.isBlank()) {
            throw new IllegalArgumentException(
                    "La contraseña es obligatoria."
            );
        }

        if (buscarPorUsuario(usuario) != null) {
            return false;
        }

        String hash;

        try {
            hash = Contrasenas.crearHash(contrasena);
        } catch (IllegalStateException excepcion) {
            throw new SQLException(
                    "No se pudo preparar la cuenta.",
                    excepcion
            );
        }

        String sql = """
                INSERT INTO administrador (usuario, contrasena_hash)
                VALUES (?, ?)
                ON CONFLICT(usuario) DO NOTHING
                """;

        try (Connection conexion = proveedorConexion.abrir();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setString(1, usuario.trim());
            sentencia.setString(2, hash);

            return sentencia.executeUpdate() == 1;
        }
    }
}