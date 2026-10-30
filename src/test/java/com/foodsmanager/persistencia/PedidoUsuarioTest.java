package com.foodsmanager.persistencia;

import com.foodsmanager.modelo.DetallePedido;
import com.foodsmanager.modelo.Pedido;
import com.foodsmanager.modelo.Producto;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class PedidoUsuarioTest {
    @TempDir Path carpeta;
    private String url;
    private PedidoDAO dao;

    @BeforeEach
    void preparar() throws SQLException {
        url = "jdbc:sqlite:" + carpeta.resolve("pedidos.db");
        dao = new PedidoDAO(this::abrirConexion);
        InicializadorBaseDatos.inicializar(this::abrirConexion);
        ejecutar("""
                INSERT INTO usuario_estandar(id_usuario, nombre, telefono, usuario, contrasena_hash)
                VALUES (1, 'Ana', '12345678', 'ana', 'hash-prueba'),
                       (2, 'Luis', '87654321', 'luis', 'hash-prueba')
                """);
    }

    private Connection abrirConexion() throws SQLException {
        Connection conexion = DriverManager.getConnection(url);
        try (Statement sentencia = conexion.createStatement()) {
            sentencia.execute("PRAGMA foreign_keys = ON");
        }
        return conexion;
    }

    private void ejecutar(String sql) throws SQLException {
        try (Connection conexion = abrirConexion(); Statement sentencia = conexion.createStatement()) {
            sentencia.execute(sql);
        }
    }

    private Pedido pedido(int usuario) {
        // Ambos pedidos comparten estos datos intencionalmente: se debe filtrar por ID.
        Pedido pedido = new Pedido("Mismo nombre", "11111111", "Ana", "13:30", "Sin cubiertos");
        pedido.setIdUsuario(usuario);
        Producto producto = new Producto(1, 1, "Pepián", "", 45, true);
        pedido.agregarDetalle(new DetallePedido(producto, 2, 45));
        return pedido;
    }

    @Test
    void consultaSoloLaCuentaSolicitadaSinMezclarDetalles() throws SQLException {
        int propio = dao.registrarPedido(pedido(1));
        int ajeno = dao.registrarPedido(pedido(2));
        List<Pedido> pedidos = dao.listarPorUsuario(1);
        assertEquals(1, pedidos.size());
        Pedido resultado = pedidos.get(0);
        assertEquals(propio, resultado.getIdPedido());
        assertEquals(1, resultado.getIdUsuario());
        assertEquals(1, resultado.getDetalles().size());
        assertEquals(propio, resultado.getDetalles().get(0).getIdPedido());
        assertEquals(ajeno, dao.listarPorUsuario(2).get(0).getIdPedido());
    }

    @Test
    void recuperaTodosLosProductosSinDuplicarElPedido() throws SQLException {
        Pedido pedido = pedido(1);
        pedido.agregarDetalle(new DetallePedido(new Producto(2, 1, "Kak ik", "", 40, true), 1, 40));
        dao.registrarPedido(pedido);
        List<Pedido> pedidos = dao.listarPorUsuario(1);
        assertEquals(1, pedidos.size());
        assertEquals(2, pedidos.get(0).getDetalles().size());
        assertEquals(130, pedidos.get(0).getTotal(), 0.001);
    }

    @Test
    void conservaFechaEstadoYPreciosDeLaCompraAunqueCambieElCatalogo() throws SQLException {
        Pedido pedido = pedido(1);
        LocalDateTime fecha = LocalDateTime.of(2026, 10, 20, 10, 15);
        pedido.setFechaPedido(fecha);
        int id = dao.registrarPedido(pedido);
        ejecutar("UPDATE producto SET precio = 99 WHERE id_producto = 1");
        ejecutar("UPDATE pedido SET estado = 'EN_PREPARACION' WHERE id_pedido = " + id);
        Pedido resultado = dao.listarPorUsuario(1).get(0);
        assertEquals(fecha, resultado.getFechaPedido());
        assertEquals("EN_PREPARACION", resultado.getEstado());
        assertEquals(90, resultado.getTotal(), 0.001);
        assertEquals(45, resultado.getDetalles().get(0).getPrecioUnitario(), 0.001);
        assertEquals(90, resultado.getDetalles().get(0).getSubtotal(), 0.001);
    }

    @Test
    void ordenaPorFechaYDesempataPorNumeroDescendente() throws SQLException {
        Pedido anterior = pedido(1);
        anterior.setFechaPedido(LocalDateTime.of(2026, 10, 1, 12, 0));
        int primero = dao.registrarPedido(anterior);
        Pedido reciente = pedido(1);
        reciente.setFechaPedido(LocalDateTime.of(2026, 10, 2, 12, 0));
        int segundo = dao.registrarPedido(reciente);
        int tercero = dao.registrarPedido(reciente);
        assertEquals(List.of(tercero, segundo, primero),
                dao.listarPorUsuario(1).stream().map(Pedido::getIdPedido).toList());
    }

    @Test
    void consultarNoCreaPedidosYUnaCuentaNuevaRecibeListaVacia() throws SQLException {
        dao.registrarPedido(pedido(1));
        assertTrue(dao.listarPorUsuario(2).isEmpty());
        dao.listarPorUsuario(1);
        dao.listarPorUsuario(1);
        try (Connection conexion = abrirConexion(); Statement sentencia = conexion.createStatement();
             ResultSet filas = sentencia.executeQuery("SELECT COUNT(*) FROM pedido")) {
            assertTrue(filas.next());
            assertEquals(1, filas.getInt(1));
        }
    }

    @Test
    void rechazaIdentificadoresInvalidosYPedidosSinCuenta() {
        assertThrows(IllegalArgumentException.class, () -> dao.listarPorUsuario(0));
        assertThrows(IllegalArgumentException.class, () -> dao.listarPorUsuario(-1));
        Pedido sinCuenta = new Pedido("Ana", "12345678", "Ana", "12:00", "");
        sinCuenta.agregarDetalle(new DetallePedido(new Producto(1, 1, "Pepián", "", 45, true), 1, 45));
        assertThrows(IllegalArgumentException.class, () -> dao.registrarPedido(sinCuenta));
        assertThrows(SQLException.class, () -> dao.registrarPedido(pedido(999)));
    }

    @Test
    void revierteLaCompraCompletaSiUnProductoNoExiste() throws SQLException {
        Pedido pedido = pedido(1);
        pedido.agregarDetalle(new DetallePedido(new Producto(999, 1, "Inexistente", "", 10, true), 1, 10));
        assertThrows(SQLException.class, () -> dao.registrarPedido(pedido));
        assertTrue(dao.listarPorUsuario(1).isEmpty());
        try (Connection conexion = abrirConexion(); Statement sentencia = conexion.createStatement();
             ResultSet filas = sentencia.executeQuery("SELECT COUNT(*) FROM detalle_pedido")) {
            assertTrue(filas.next());
            assertEquals(0, filas.getInt(1));
        }
    }
}
