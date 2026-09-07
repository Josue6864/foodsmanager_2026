package com.foodsmanager.modelo;

/*
 Representa un restaurante disponible en FoodsManager.
 
  Los restaurantes se encuentran precargados en la base de datos.
  Los productos asociados se consultaran mediante ProductoDAO utilizando
  el identificador del restaurante.
 */
public class Restaurante {

    private int idRestaurante;
    private String nombre;
    private String ubicacion;

    public Restaurante(
            int idRestaurante,
            String nombre,
            String ubicacion) {
        setIdRestaurante(idRestaurante);
        setNombre(nombre);
        setUbicacion(ubicacion);
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
        this.nombre = validarTextoObligatorio(
                nombre,
                "El nombre del restaurante es obligatorio.");
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = validarTextoObligatorio(
                ubicacion,
                "La ubicacion del restaurante es obligatoria.");
    }

    private static String validarTextoObligatorio(
            String texto,
            String mensaje) {
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