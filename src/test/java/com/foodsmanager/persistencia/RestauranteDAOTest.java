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
                        ubicacion TEXT NOT NULL,
                        descripcion TEXT NOT NULL DEFAULT ''
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
        assertThrows(IllegalArgumentException.class,
                () -> dao.actualizarDescripcion(0, "Descripción"));
        assertThrows(SQLException.class, () -> dao.actualizarDescripcion(999, "Descripción"));
    }

    @Test
    void debeGuardarYListarLaDescripcionCompleta() throws SQLException {
        String descripcion = "Especialidades de O'Brien.\nCafé y comida guatemalteca.";
        int id = dao.insertar("El Comal", "Zona 7", "  " + descripcion + "  ");

        assertEquals(descripcion, dao.buscarPorId(id).getDescripcion());
        assertEquals(descripcion, dao.listarTodos().get(0).getDescripcion());
    }

    @Test
    void debeActualizarSoloLaDescripcion() throws SQLException {
        int id = dao.insertar("El Comal", "Zona 7", "Comida casera.");
        dao.actualizarDescripcion(id, "  Desayunos y almuerzos.  ");
        Restaurante restaurante = dao.buscarPorId(id);

        assertEquals(id, restaurante.getIdRestaurante());
        assertEquals("El Comal", restaurante.getNombre());
        assertEquals("Zona 7", restaurante.getUbicacion());
        assertEquals("Desayunos y almuerzos.", restaurante.getDescripcion());
        assertEquals(1, dao.listarTodos().size());
    }

    @Test
    void debePermitirOmitirYVaciarLaDescripcion() throws SQLException {
        int idAnterior = dao.insertar("Sin descripción", "Zona 1");
        int idNulo = dao.insertar("Otro", "Zona 2", null);
        int idBlanco = dao.insertar("Tercero", "Zona 3", " \t\n ");
        assertEquals("", dao.buscarPorId(idAnterior).getDescripcion());
        assertEquals("", dao.buscarPorId(idNulo).getDescripcion());
        assertEquals("", dao.buscarPorId(idBlanco).getDescripcion());

        dao.actualizarDescripcion(idAnterior, "Temporal");
        dao.actualizarDescripcion(idAnterior, null);
        assertEquals("", dao.buscarPorId(idAnterior).getDescripcion());
    }
}
