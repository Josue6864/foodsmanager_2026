package com.foodsmanager.modelo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Restaurante {
    private int idRestaurante;
    private String nombre;
    private String ubicacion;
    private final List<Producto> productos;


    public Restaurante(int idRestaurante, String nombre, String ubicacion) {
        setIdRestaurante(idRestaurante);
        setNombre(nombre);
        setUbicacion(ubicacion);
        this.productos = new ArrayList<>();
    }

    public Restaurante(String nombre, String ubicacion) {
        this(0, nombre, ubicacion);
    }

    public int getIdRestaurante() {
        return idRestaurante;
    }

    public void setIdRestaurante(int idRestaurante) {
        if (idRestaurante < 0) {
            throw new IllegalArgumentException("El id del restaurante no puede ser negativo.");
        }
        this.idRestaurante = idRestaurante;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = validarTextoObligatorio(nombre, "El nombre del restaurante es obligatorio.");
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = validarTextoObligatorio(ubicacion, "La ubicacion del restaurante es obligatoria.");
    }

    public List<Producto> getProductos() {
        return Collections.unmodifiableList(productos);
    }

    public void agregarProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto es obligatorio.");
        }
        if (idRestaurante > 0 && producto.getIdRestaurante() != idRestaurante) {
            throw new IllegalArgumentException("El producto pertenece a otro restaurante.");
        }
        if (producto.getIdProducto() > 0 && buscarProductoPorId(producto.getIdProducto()) != null) {
            throw new IllegalArgumentException("Ya existe un producto con el mismo id.");
        }
        productos.add(producto);
    }

    public boolean eliminarProducto(int idProducto) {
        return productos.removeIf(producto -> producto.getIdProducto() == idProducto);
    }

    public Producto buscarProductoPorId(int idProducto) {
        for (Producto producto : productos) {
            if (producto.getIdProducto() == idProducto) {
                return producto;
            }
        }
        return null;
    }

    private static String validarTextoObligatorio(String texto, String mensaje) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException(mensaje);
        }
        return texto.trim();
    }

    @Override
    public String toString() {
        return "Restaurante{" +
                "idRestaurante=" + idRestaurante +
                ", nombre='" + nombre + '\'' +
                ", ubicacion='" + ubicacion + '\'' +
                ", cantidadProductos=" + productos.size() +
                '}';
    }
}
