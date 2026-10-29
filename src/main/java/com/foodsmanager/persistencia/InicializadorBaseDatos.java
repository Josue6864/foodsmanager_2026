package com.foodsmanager.persistencia;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class InicializadorBaseDatos {

    private static final String RUTA_ESQUEMA = "/db/schema.sql";

    private static final String SQL_AGREGAR_USUARIO_PEDIDO = """
            ALTER TABLE pedido
            ADD COLUMN id_usuario INTEGER REFERENCES usuario_estandar(id_usuario)
            """;

    // El flujo anterior copiaba nombre y teléfono de la cuenta autenticada.
    // Solo se recuperan coincidencias exactas y únicas durante esta migración.
    private static final String SQL_VINCULAR_PEDIDOS_ANTERIORES = """
            UPDATE pedido
            SET id_usuario = (
                SELECT u.id_usuario FROM usuario_estandar u
                WHERE u.telefono = pedido.telefono AND u.nombre = pedido.nombre_cliente
            )
            WHERE id_usuario IS NULL AND (
                SELECT COUNT(*) FROM usuario_estandar u
                WHERE u.telefono = pedido.telefono AND u.nombre = pedido.nombre_cliente
            ) = 1
            """;

    private static final String SQL_INDICE_PEDIDOS_USUARIO = """
            CREATE INDEX IF NOT EXISTS idx_pedido_usuario_fecha
            ON pedido(id_usuario, fecha_pedido DESC, id_pedido DESC)
            """;

    private static final String SQL_AGREGAR_UBICACION = """
            ALTER TABLE restaurante
            ADD COLUMN ubicacion TEXT NOT NULL DEFAULT 'Ubicación pendiente'
            """;

    private static final String SQL_AGREGAR_DESCRIPCION = """
            ALTER TABLE restaurante
            ADD COLUMN descripcion TEXT NOT NULL DEFAULT ''
            """;

    // Se ejecuta solo cuando se agrega la columna a una base anterior.
    // No se repite en cada arranque, para respetar las ediciones posteriores.
    private static final String SQL_DESCRIPCIONES_INICIALES = """
            UPDATE restaurante
            SET descripcion = CASE id_restaurante
                WHEN 1 THEN 'Comida tradicional guatemalteca.'
                WHEN 2 THEN 'Pizzas y especialidades para compartir.'
                WHEN 3 THEN 'Hamburguesas, acompañamientos y bebidas.'
            END
            WHERE trim(descripcion) = '' AND (
                (id_restaurante = 1 AND nombre IN ('Comida guatemalteca', 'Sabor Chapín'))
                OR (id_restaurante = 2 AND nombre IN ('Pizzería', 'Pizzería Central'))
                OR (id_restaurante = 3 AND nombre IN ('Hamburguesería', 'Burger House'))
            )
            """;

    @FunctionalInterface
    interface ProveedorConexion {
        Connection abrir() throws SQLException;
    }

    private InicializadorBaseDatos() {
    }

    public static void inicializar() throws SQLException {
        inicializar(ConexionSQLite::abrirConexion);
    }

    // Acceso de paquete para probar la misma inicialización con una base temporal.
    static void inicializar(ProveedorConexion proveedorConexion) throws SQLException {
        String esquema = leerEsquema();

        try (Connection conexion = proveedorConexion.abrir();
             Statement sentencia = conexion.createStatement()) {
            conexion.setAutoCommit(false);

            try {
                // Las tablas existentes deben actualizarse antes del esquema.
                actualizarTablaRestaurante(conexion);

                int numeroInstruccion = 0;
                for (String instruccion : esquema.split(";")) {
                    String sql = instruccion.trim();
                    if (!sql.isEmpty()) {
                        numeroInstruccion++;
                        try {
                            System.out.println("Ejecutando instrucción #"
                                    + numeroInstruccion + ":\n" + sql);
                            sentencia.execute(sql);
                        } catch (SQLException excepcion) {
                            System.err.println("Falló la instrucción #"
                                    + numeroInstruccion + ":\n" + sql);
                            throw excepcion;
                        }
                    }
                }
                actualizarTablaPedido(conexion);
                conexion.commit();
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

    private static void actualizarTablaRestaurante(Connection conexion)
            throws SQLException {
        boolean existeTabla = false;
        boolean existeUbicacion = false;
        boolean existeDescripcion = false;

        try (Statement sentencia = conexion.createStatement();
             ResultSet columnas = sentencia.executeQuery("PRAGMA table_info(restaurante)")) {
            while (columnas.next()) {
                existeTabla = true;
                String nombreColumna = columnas.getString("name");
                if ("ubicacion".equalsIgnoreCase(nombreColumna)) {
                    existeUbicacion = true;
                }
                if ("descripcion".equalsIgnoreCase(nombreColumna)) {
                    existeDescripcion = true;
                }
            }
        }

        if (!existeTabla) {
            return;
        }
        try (Statement sentencia = conexion.createStatement()) {
            if (!existeUbicacion) {
                sentencia.execute(SQL_AGREGAR_UBICACION);
            }
            if (!existeDescripcion) {
                sentencia.execute(SQL_AGREGAR_DESCRIPCION);
                sentencia.execute(SQL_DESCRIPCIONES_INICIALES);
            }
        }
    }

    private static String leerEsquema() throws SQLException {
        try (InputStream entrada = InicializadorBaseDatos.class
                .getResourceAsStream(RUTA_ESQUEMA)) {
            if (entrada == null) {
                throw new SQLException("No se encontró el archivo " + RUTA_ESQUEMA);
            }
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException excepcion) {
            throw new SQLException("No se pudo leer el esquema de la base de datos.", excepcion);
        }
    }

    private static void actualizarTablaPedido(Connection conexion) throws SQLException {
        boolean tieneUsuario = false;
        try (Statement sentencia = conexion.createStatement();
             ResultSet columnas = sentencia.executeQuery("PRAGMA table_info(pedido)")) {
            while (columnas.next()) {
                if ("id_usuario".equalsIgnoreCase(columnas.getString("name"))) {
                    tieneUsuario = true;
                }
            }
        }
        try (Statement sentencia = conexion.createStatement()) {
            if (!tieneUsuario) {
                sentencia.execute(SQL_AGREGAR_USUARIO_PEDIDO);
                sentencia.execute(SQL_VINCULAR_PEDIDOS_ANTERIORES);
            }
            // Los pedidos sin coincidencia quedan conservados, sin asignarse a otra cuenta.
            // La vinculación no se repite al crear cuentas nuevas ni en futuros arranques.
            sentencia.execute(SQL_INDICE_PEDIDOS_USUARIO);
        }
    }
}
