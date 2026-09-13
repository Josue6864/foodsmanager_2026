package com.foodsmanager.persistencia;

import com.foodsmanager.modelo.Restaurante;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RestauranteDAO {

    private static final String SQL_LISTAR_TODOS = """
            SELECT id_restaurante, nombre, ubicacion
            FROM restaurante
            ORDER BY nombre COLLATE NOCASE
            """;

    /**
      Obtiene todos los restaurantes registrados.
     
      @return lista de restaurantes ordenada por nombre
      @throws SQLException si ocurre un error al consultar SQLite
     **/
    public List<Restaurante> listarTodos() throws SQLException {
        List<Restaurante> restaurantes = new ArrayList<>();

        try (Connection conexion = ConexionSQLite.abrirConexion(); PreparedStatement sentencia
                = conexion.prepareStatement(SQL_LISTAR_TODOS); ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                Restaurante restaurante = new Restaurante(
                        resultado.getInt("id_restaurante"),
                        resultado.getString("nombre"),
                        resultado.getString("ubicacion")
                );

                restaurantes.add(restaurante);
            }
        }

        return restaurantes;
    }
}
