package com.foodsmanager.persistencia;

import com.foodsmanager.modelo.Restaurante;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class RestauranteDAOTest {

    @TempDir
    Path carpeta;

    private String url;
    private RestauranteDAO dao;

    @BeforeEach
    void prepararBaseDatos() throws SQLException {
        url = "jdbc:sqlite:" + carpeta.resolve("restaurantes.db");
        dao = new RestauranteDAO(this::abrirConexion);

        try (Connection conexion = abrirConexion();
             Statement sentencia = conexion.createStatement()) {
            sentencia.execute("""
                    CREATE TABLE restaurante (
                        id_restaurante INTEGER PRIMARY KEY AUTOINCREMENT,
                        nombre TEXT NOT NULL,
                        ubicacion TEXT NOT NULL
                    )
                    """);
        }
    }

    private Connection abrirConexion() throws SQLException {
        return DriverManager.getConnection(url);
    }

    @Test
    void debeRegistrarYConservarElRestauranteAlAbrirOtraConexion() throws SQLException {
        int id = dao.insertar("  Antojitos de O'Brien  ", "  Zona 12  ");

        RestauranteDAO otroDao = new RestauranteDAO(this::abrirConexion);
        Restaurante guardado = otroDao.buscarPorId(id);

        assertTrue(id > 0);
        assertNotNull(guardado);
        assertEquals(id, guardado.getIdRestaurante());
        assertEquals("Antojitos de O'Brien", guardado.getNombre());
        assertEquals("Zona 12", guardado.getUbicacion());
        assertEquals(1, otroDao.listarTodos().size());
    }

    @Test
    void debeAgregarUnRestauranteSinReemplazarLosIniciales() throws SQLException {
        try (Connection conexion = abrirConexion();
             Statement sentencia = conexion.createStatement()) {
            sentencia.execute("""
                    INSERT INTO restaurante (id_restaurante, nombre, ubicacion)
                    VALUES
                        (1, 'Sabor Chapín', 'Zona 1'),
                        (2, 'Pizzería Central', 'Zona 4'),
                        (3, 'Burger House', 'Zona 10')
                    """);
        }

        int idNuevo = dao.insertar("El Comal", "Zona 7");

        assertTrue(idNuevo > 3);
        assertEquals(4, dao.listarTodos().size());
        assertEquals("El Comal", dao.buscarPorId(idNuevo).getNombre());
        assertEquals("Sabor Chapín", dao.buscarPorId(1).getNombre());
        assertEquals("Zona 1", dao.buscarPorId(1).getUbicacion());
        assertEquals("Pizzería Central", dao.buscarPorId(2).getNombre());
        assertEquals("Zona 4", dao.buscarPorId(2).getUbicacion());
        assertEquals("Burger House", dao.buscarPorId(3).getNombre());
        assertEquals("Zona 10", dao.buscarPorId(3).getUbicacion());
    }

    @Test
    void debeListarPorNombreYDesempatarPorIdentificador() throws SQLException {
        int idBeta = dao.insertar("beta", "Zona 1");
        int idAlfa = dao.insertar("alfa", "Zona 2");
        int idOtraAlfa = dao.insertar("Alfa", "Zona 3");

        List<Integer> ids = dao.listarTodos().stream()
                .map(Restaurante::getIdRestaurante)
                .toList();

        assertEquals(List.of(idAlfa, idOtraAlfa, idBeta), ids);
    }

    @Test
    void debeRechazarDatosVaciosSinGuardarFilas() throws SQLException {
        String[] textosInvalidos = {null, "", "   ", "\t\n"};

        for (String texto : textosInvalidos) {
            assertThrows(IllegalArgumentException.class,
                    () -> dao.insertar(texto, "Zona 1"));
            assertThrows(IllegalArgumentException.class,
                    () -> dao.insertar("El Comal", texto));
        }

        assertTrue(dao.listarTodos().isEmpty());
    }

    @Test
    void debeDevolverNullSiElRestauranteNoExiste() throws SQLException {
        assertNull(dao.buscarPorId(999));
    }

    @Test
    void debeRechazarIdentificadoresInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> dao.buscarPorId(0));
        assertThrows(IllegalArgumentException.class, () -> dao.buscarPorId(-1));
    }
}
