package com.foodsmanager.modelo;

/** Representa un restaurante del catálogo, con una descripción opcional. */
public class Restaurante {

    private int idRestaurante;
    private String nombre;
    private String ubicacion;
    private String descripcion;

    /** Conserva la compatibilidad con las llamadas existentes de tres argumentos. */
    public Restaurante(int idRestaurante, String nombre, String ubicacion) {
        this(idRestaurante, nombre, ubicacion, "");
    }

    public Restaurante(int idRestaurante, String nombre, String ubicacion,
            String descripcion) {
        setIdRestaurante(idRestaurante);
        setNombre(nombre);
        setUbicacion(ubicacion);
        setDescripcion(descripcion);
    }

    public int getIdRestaurante() {
        return idRestaurante;
    }

    public void setIdRestaurante(int idRestaurante) {
        if (idRestaurante <= 0) {
            throw new IllegalArgumentException(
                    "El id del restaurante debe ser mayor que cero.");
        }
        this.idRestaurante = idRestaurante;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = validarTextoObligatorio(nombre,
                "El nombre del restaurante es obligatorio.");
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = validarTextoObligatorio(ubicacion,
                "La ubicacion del restaurante es obligatoria.");
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion == null ? "" : descripcion.trim();
    }

    private static String validarTextoObligatorio(String texto, String mensaje) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException(mensaje);
        }
        return texto.trim();
    }

    @Override
    public String toString() {
        return nombre;
    }
}
