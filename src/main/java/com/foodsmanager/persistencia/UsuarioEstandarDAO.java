package com.foodsmanager.persistencia;

import com.foodsmanager.modelo.UsuarioEstandar;
import com.foodsmanager.seguridad.Contrasenas;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class UsuarioEstandarDAO {

    private static final String INSERTAR = """
        INSERT INTO usuario_estandar
            (nombre, telefono, usuario, contrasena_hash)
        VALUES (?, ?, ?, ?)
        """;

    private static final String BUSCAR_POR_USUARIO = """
        SELECT id_usuario, nombre, telefono, usuario, contrasena_hash
        FROM usuario_estandar
        WHERE usuario = ? COLLATE NOCASE
        """;

    private static final String BUSCAR_POR_TELEFONO = """
        SELECT id_usuario, nombre, telefono, usuario, contrasena_hash
        FROM usuario_estandar
        WHERE telefono = ?
        """;

    public UsuarioEstandar insertar(UsuarioEstandar usuario) throws SQLException {
        if (buscarPorTelefono(usuario.getTelefono()).isPresent()) {
            throw new SQLException("Ya existe una cuenta asociada a ese teléfono.");
        }

        try (Connection conexion = ConexionSQLite.abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(
                     INSERTAR,
                     Statement.RETURN_GENERATED_KEYS)) {

            sentencia.setString(1, usuario.getNombre());
            sentencia.setString(2, usuario.getTelefono());
            sentencia.setString(3, usuario.getUsuario());
            sentencia.setString(4, usuario.getContrasenaHash());
            sentencia.executeUpdate();

            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    usuario.setIdUsuario(claves.getInt(1));
                    return usuario;
                }
            }
        }

        throw new SQLException("No se pudo obtener el ID del usuario.");
    }

    public Optional<UsuarioEstandar> buscarPorUsuario(String usuario)
            throws SQLException {
        return buscar(BUSCAR_POR_USUARIO, usuario);
    }

    public Optional<UsuarioEstandar> buscarPorTelefono(String telefono)
            throws SQLException {
        return buscar(BUSCAR_POR_TELEFONO, telefono);
    }

    public Optional<UsuarioEstandar> autenticar(
            String usuario,
            String contrasena) throws SQLException {

        Optional<UsuarioEstandar> encontrado = buscarPorUsuario(usuario);

        if (encontrado.isPresent()
                && Contrasenas.verificar(
                        contrasena,
                        encontrado.get().getContrasenaHash())) {
            return encontrado;
        }

        return Optional.empty();
    }

    private Optional<UsuarioEstandar> buscar(String sql, String valor)
            throws SQLException {

        try (Connection conexion = ConexionSQLite.abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, valor == null ? "" : valor.trim());

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return Optional.of(new UsuarioEstandar(
                            resultado.getInt("id_usuario"),
                            resultado.getString("nombre"),
                            resultado.getString("telefono"),
                            resultado.getString("usuario"),
                            resultado.getString("contrasena_hash")
                    ));
                }
            }
        }

        return Optional.empty();
    }
}
