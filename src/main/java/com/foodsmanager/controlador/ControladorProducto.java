package com.foodsmanager.controlador;

import com.foodsmanager.modelo.Producto;
import com.foodsmanager.modelo.Restaurante;
import com.foodsmanager.persistencia.ProductoDAO;
import com.foodsmanager.persistencia.RestauranteDAO;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
  Coordina las operaciones relacionadas con los productos.
 */
public class ControladorProducto {

    private final ProductoDAO productoDAO;
    private final RestauranteDAO restauranteDAO;

    /**
      Constructor utilizado normalmente por la aplicación.
     */
    public ControladorProducto() {
        this(
                new ProductoDAO(),
                new RestauranteDAO()
        );
    }

    /**
      Constructor que permite proporcionar los DAO.
      Será útil para pruebas y futuras ampliaciones.
     */
    public ControladorProducto(
            ProductoDAO productoDAO,
            RestauranteDAO restauranteDAO) {

        this.productoDAO = Objects.requireNonNull(
                productoDAO,
                "ProductoDAO es obligatorio."
        );

        this.restauranteDAO = Objects.requireNonNull(
                restauranteDAO,
                "RestauranteDAO es obligatorio."
        );
    }

    /**
      Obtiene los restaurantes que pueden seleccionarse
      al registrar un producto.
     */
    public List<Restaurante> listarRestaurantes()
            throws SQLException {

        return restauranteDAO.listarTodos();
    }

    /**
      Registra un producto convirtiendo el precio ingresado
      desde el formulario.
     */
    public Producto registrarProducto(
            int idRestaurante,
            String nombre,
            String descripcion,
            String precioTexto,
            boolean disponible) throws SQLException {

        double precio = convertirPrecio(precioTexto);

        return registrarProducto(
                idRestaurante,
                nombre,
                descripcion,
                precio,
                disponible
        );
    }

    /**
      Registra un producto cuando el precio ya fue convertido.
     */
    public Producto registrarProducto(
            int idRestaurante,
            String nombre,
            String descripcion,
            double precio,
            boolean disponible) throws SQLException {

        Producto producto = new Producto(
                idRestaurante,
                nombre,
                descripcion,
                precio,
                disponible
        );

        productoDAO.insertar(producto);

        return producto;
    }

    /**
      Convierte el precio ingresado en el formulario.
     
      Acepta punto o coma como separador decimal.
     */
    private double convertirPrecio(String precioTexto) {
        if (precioTexto == null || precioTexto.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El precio del producto es obligatorio.");
        }

        String precioNormalizado = precioTexto
                .trim()
                .replace(',', '.');

        try {
            double precio = Double.parseDouble(precioNormalizado);

            if (!Double.isFinite(precio) || precio < 0) {
                throw new IllegalArgumentException(
                        "El precio debe ser un número mayor o igual a cero.");
            }

            return precio;
        } catch (NumberFormatException excepcion) {
            throw new IllegalArgumentException(
                    "El precio ingresado no es válido.",
                    excepcion
            );
        }
    }
}
