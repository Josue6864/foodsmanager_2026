/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.foodsmanager.persistencia;

import com.foodsmanager.modelo.DetallePedido;
import com.foodsmanager.modelo.Pedido;
import com.foodsmanager.modelo.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author certe
 */
public class PedidoDAO {

    @FunctionalInterface
    interface ProveedorConexion {
        Connection abrir() throws SQLException;
    }

    private final ProveedorConexion proveedorConexion;

    public PedidoDAO() {
        this(ConexionSQLite::abrirConexion);
    }

    PedidoDAO(ProveedorConexion proveedorConexion) {
        this.proveedorConexion = Objects.requireNonNull(proveedorConexion);
    }

    private static final String LISTAR_POR_USUARIO = """
        SELECT p.*, d.id_detalle, d.id_producto, d.cantidad, d.precio_unitario,
               pr.id_restaurante, pr.nombre AS nombre_producto,
               pr.descripcion AS descripcion_producto, pr.disponible
        FROM pedido p
        LEFT JOIN detalle_pedido d ON d.id_pedido = p.id_pedido
        LEFT JOIN producto pr ON pr.id_producto = d.id_producto
        WHERE p.id_usuario = ?
        ORDER BY p.fecha_pedido DESC, p.id_pedido DESC, d.id_detalle
        """;

    private static final String INSERTAR_PEDIDO = """
        INSERT INTO pedido (
            nombre_cliente,
            telefono,
            persona_recoge,
            hora_recogida,
            indicaciones,
            fecha_pedido,
            total,
            estado,
            id_usuario
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
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

        if (pedido.getIdUsuario() <= 0) {
            throw new IllegalArgumentException("El pedido debe estar asociado al cliente autenticado.");
        }

        pedido.calcularTotal();

        try (Connection conexion
                = proveedorConexion.abrir()) {

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
                    sentencia.setInt(9, pedido.getIdUsuario());

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

    /** Consulta exclusivamente los pedidos de la cuenta indicada por la sesión. */
    public List<Pedido> listarPorUsuario(int idUsuario) throws SQLException {
        if (idUsuario <= 0) {
            throw new IllegalArgumentException("El identificador del cliente no es válido.");
        }

        Map<Integer, Pedido> pedidos = new LinkedHashMap<>();
        try (Connection conexion = proveedorConexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(LISTAR_POR_USUARIO)) {
            sentencia.setInt(1, idUsuario);
            try (ResultSet fila = sentencia.executeQuery()) {
                while (fila.next()) {
                    int idPedido = fila.getInt("id_pedido");
                    Pedido pedido = pedidos.get(idPedido);
                    if (pedido == null) {
                        pedido = new Pedido(fila.getString("nombre_cliente"),
                                fila.getString("telefono"), fila.getString("persona_recoge"),
                                fila.getString("hora_recogida"), fila.getString("indicaciones"));
                        pedido.setIdPedido(idPedido);
                        pedido.setIdUsuario(fila.getInt("id_usuario"));
                        pedido.setFechaPedido(LocalDateTime.parse(fila.getString("fecha_pedido")));
                        pedido.setEstado(fila.getString("estado"));
                        pedidos.put(idPedido, pedido);
                    }
                    int idDetalle = fila.getInt("id_detalle");
                    if (!fila.wasNull()) {
                        Producto producto = new Producto(fila.getInt("id_producto"),
                                fila.getInt("id_restaurante"), fila.getString("nombre_producto"),
                                fila.getString("descripcion_producto"), fila.getDouble("precio_unitario"),
                                fila.getInt("disponible") == 1);
                        DetallePedido detalle = new DetallePedido(producto,
                                fila.getInt("cantidad"), fila.getDouble("precio_unitario"));
                        detalle.setIdDetalle(idDetalle);
                        detalle.setIdPedido(idPedido);
                        pedido.agregarDetalle(detalle);
                    }
                    // Mostrar el importe histórico, no el precio actual del catálogo.
                    pedido.setTotal(fila.getDouble("total"));
                }
            }
        }
        return new ArrayList<>(pedidos.values());
    }
}
