package com.foodsmanager.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.foodsmanager.modelo.Restaurante;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RestauranteDAOTest {

    @BeforeAll
    static void prepararBaseDatos() throws SQLException {
        InicializadorBaseDatos.inicializar();
    }

    @Test
    void debeListarLosRestaurantesIniciales() throws SQLException {
        RestauranteDAO restauranteDAO = new RestauranteDAO();

        List<Restaurante> restaurantes
                = restauranteDAO.listarTodos();

        Set<String> nombres = restaurantes.stream()
                .map(Restaurante::getNombre)
                .collect(Collectors.toSet());

        Set<String> nombresEsperados = Set.of(
                "Sabor Chapín",
                "Pizzería Central",
                "Burger House"
        );

        assertEquals(nombresEsperados, nombres);

        assertTrue(
                restaurantes.stream()
                        .allMatch(restaurante
                                -> restaurante.getIdRestaurante() > 0)
        );
    }
}
