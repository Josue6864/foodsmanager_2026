package com.foodsmanager.controlador;

import com.foodsmanager.modelo.Restaurante;
import com.foodsmanager.persistencia.RestauranteDAO;
import com.foodsmanager.seguridad.SesionAdministrador;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ControladorGestionRestaurantesTest {

    private ControladorGestionRestaurantes controlador;

    @BeforeEach
    void preparar() {
        SesionAdministrador.cerrar();
        controlador = new ControladorGestionRestaurantes(new RestauranteDAO() {
            @Override
            public int insertar(String nombre, String ubicacion, String descripcion)
                    throws SQLException {
                throw new AssertionError("No se debe insertar sin sesión.");
            }

            @Override
            public void actualizarDescripcion(int idRestaurante, String descripcion)
                    throws SQLException {
                throw new AssertionError("No se debe actualizar sin sesión.");
            }

            @Override
            public List<Restaurante> listarTodos() throws SQLException {
                throw new AssertionError("No se debe cargar el listado administrativo sin sesión.");
            }
        });
    }

    @AfterEach
    void limpiarSesion() {
        SesionAdministrador.cerrar();
    }

    @Test
    void debeBloquearElRegistroAntesDeAccederAlDao() {
        assertThrows(SecurityException.class,
                () -> controlador.registrarRestaurante("El Comal", "Zona 7"));
    }

    @Test
    void debeBloquearElListadoAdministrativoAntesDeAccederAlDao() {
        assertThrows(SecurityException.class, () -> controlador.listarRestaurantes());
    }

    @Test
    void debeBloquearElRegistroConDescripcionAntesDeAccederAlDao() {
        assertThrows(SecurityException.class,
                () -> controlador.registrarRestaurante("El Comal", "Zona 7", "Comida casera."));
    }

    @Test
    void debeBloquearLaEdicionDeDescripcionAntesDeAccederAlDao() {
        assertThrows(SecurityException.class,
                () -> controlador.actualizarDescripcion(1, "Nueva descripción."));
    }
}
