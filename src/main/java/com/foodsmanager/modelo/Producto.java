package com.foodsmanager.modelo;

/**
 *
 * @author david_oavv7c4
 */
public class Producto {

    private int idProducto;
    private int idRestaurante;
    private String nombre;
    private String descripcion;
    private double precio;
    private boolean disponible;

    public Producto(
            int idProducto,
            int idRestaurante,
            String nombre,
            String descripcion,
            double precio,
            boolean disponible) {
        setIdProducto(idProducto);
        setIdRestaurante(idRestaurante);
        setNombre(nombre);
        setDescripcion(descripcion);
        setPrecio(precio);
        setDisponible(disponible);
    }

    /**
     * Construye un producto nuevo que todavia no ha sido guardado. El
     * identificador cero representa que la base de datos aun no le ha asignado
     * una llave primaria.
     */
    public Producto(
            int idRestaurante,
            String nombre,
            String descripcion,
            double precio,
            boolean disponible) {
        this(0, idRestaurante, nombre, descripcion, precio, disponible);
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        if (idProducto < 0) {
            throw new IllegalArgumentException("El id del producto no puede ser negativo.");
        }
        this.idProducto = idProducto;
    }

    public int getIdRestaurante() {
        return idRestaurante;
    }

    public void setIdRestaurante(int idRestaurante) {
        if (idRestaurante <= 0) {
            throw new IllegalArgumentException("El producto debe pertenecer a un restaurante valido.");
        }
        this.idRestaurante = idRestaurante;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = validarTextoObligatorio(nombre, "El nombre del producto es obligatorio.");
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion == null ? "" : descripcion.trim();
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        if (!Double.isFinite(precio) || precio < 0) {
            throw new IllegalArgumentException("El precio debe ser un numero finito mayor o igual a cero.");
        }
        this.precio = precio;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    private static String validarTextoObligatorio(String texto, String mensaje) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException(mensaje);
        }
        return texto.trim();
    }

    @Override
    public String toString() {
        return "Producto{"
                + "idProducto=" + idProducto
                + ", idRestaurante=" + idRestaurante
                + ", nombre='" + nombre + '\''
                + ", descripcion='" + descripcion + '\''
                + ", precio=" + precio
                + ", disponible=" + disponible
                + '}';
    }
}
