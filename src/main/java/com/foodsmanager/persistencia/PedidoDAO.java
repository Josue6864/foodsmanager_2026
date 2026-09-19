/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.foodsmanager.persistencia;

import com.foodsmanager.modelo.DetallePedido;
import com.foodsmanager.modelo.Pedido;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author certe
 */
public class PedidoDAO {

    private static final String INSERTAR_PEDIDO = """
        INSERT INTO pedido (
            nombre_cliente,
            telefono,
            persona_recoge,
            hora_recogida,
            indicaciones,
            fecha_pedido,
            total,
            estado
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String INSERTAR_DETALLE = """
        INSERT INTO detalle_pedido (
            id_pedido,
            id_producto,
            cantidad,
            precio_unitario,
            subtotal
        ) VALUES (?, ?, ?, ?, ?)
        """;

    public int registrarPedido(Pedido pedido)
            throws SQLException {

        if (pedido == null || pedido.getDetalles().isEmpty()) {
            throw new IllegalArgumentException(
                    "El pedido debe contener al menos un producto."
            );
        }

        pedido.calcularTotal();

        try (Connection conexion
                = ConexionSQLite.abrirConexion()) {

            // SQLite necesita activar las claves foráneas
            // en cada conexión.
            try (Statement sentencia
                    = conexion.createStatement()) {

                sentencia.execute(
                        "PRAGMA foreign_keys = ON"
                );
            }

            conexion.setAutoCommit(false);

            try {
                int idPedido
                        = insertarPedido(conexion, pedido);

                insertarDetalles(
                        conexion,
                        idPedido,
                        pedido
                );

                conexion.commit();

                pedido.setIdPedido(idPedido);
                return idPedido;

            } catch (SQLException excepcion) {
                conexion.rollback();
                throw excepcion;

            } finally {
                conexion.setAutoCommit(true);
            }
        }
    }

    private int insertarPedido(
            Connection conexion,
            Pedido pedido
    ) throws SQLException {

        try (PreparedStatement sentencia
                = conexion.prepareStatement(
                        INSERTAR_PEDIDO,
                        Statement.RETURN_GENERATED_KEYS
                )) {

                    sentencia.setString(
                            1,
                            pedido.getNombreCliente()
                    );

                    sentencia.setString(
                            2,
                            pedido.getTelefono()
                    );

                    sentencia.setString(
                            3,
                            pedido.getPersonaRecoge()
                    );

                    sentencia.setString(
                            4,
                            pedido.getHoraRecogida()
                    );

                    sentencia.setString(
                            5,
                            pedido.getIndicaciones()
                    );

                    sentencia.setString(
                            6,
                            pedido.getFechaPedido().toString()
                    );

                    sentencia.setDouble(
                            7,
                            pedido.getTotal()
                    );

                    sentencia.setString(
                            8,
                            pedido.getEstado()
                    );

                    sentencia.executeUpdate();

                    try (ResultSet claves
                            = sentencia.getGeneratedKeys()) {

                        if (claves.next()) {
                            return claves.getInt(1);
                        }
                    }

                    throw new SQLException(
                            "No se pudo obtener el ID del pedido."
                    );
                }
    }

    private void insertarDetalles(
            Connection conexion,
            int idPedido,
            Pedido pedido
    ) throws SQLException {

        try (PreparedStatement sentencia
                = conexion.prepareStatement(
                        INSERTAR_DETALLE
                )) {

                    for (DetallePedido detalle
                            : pedido.getDetalles()) {

                        sentencia.setInt(1, idPedido);

                        sentencia.setInt(
                                2,
                                detalle.getProducto().getIdProducto()
                        );

                        sentencia.setInt(
                                3,
                                detalle.getCantidad()
                        );

                        sentencia.setDouble(
                                4,
                                detalle.getPrecioUnitario()
                        );

                        sentencia.setDouble(
                                5,
                                detalle.getSubtotal()
                        );

                        sentencia.addBatch();
                    }

                    sentencia.executeBatch();
                }
    }
}
