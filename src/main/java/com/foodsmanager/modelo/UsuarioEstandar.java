package com.foodsmanager.modelo;

public class UsuarioEstandar {

    private int idUsuario;
    private String nombre;
    private String telefono;
    private String usuario;
    private String contrasenaHash;

    public UsuarioEstandar(
            String nombre,
            String telefono,
            String usuario,
            String contrasenaHash) {

        setNombre(nombre);
        setTelefono(telefono);
        setUsuario(usuario);
        setContrasenaHash(contrasenaHash);
    }

    public UsuarioEstandar(
            int idUsuario,
            String nombre,
            String telefono,
            String usuario,
            String contrasenaHash) {

        if (idUsuario <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor que cero.");
        }

        this.idUsuario = idUsuario;
        setNombre(nombre);
        setTelefono(telefono);
        setUsuario(usuario);
        setContrasenaHash(contrasenaHash);
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        if (idUsuario <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor que cero.");
        }
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        this.nombre = nombre.trim();
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        String limpio = telefono == null ? "" : telefono.trim();
        if (!limpio.matches("\\d{8}")) {
            throw new IllegalArgumentException("El teléfono debe contener 8 dígitos.");
        }
        this.telefono = limpio;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        if (usuario == null || usuario.isBlank()) {
            throw new IllegalArgumentException("El usuario es obligatorio.");
        }
        this.usuario = usuario.trim();
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public void setContrasenaHash(String contrasenaHash) {
        if (contrasenaHash == null || contrasenaHash.isBlank()) {
            throw new IllegalArgumentException("El hash de contraseña es obligatorio.");
        }
        this.contrasenaHash = contrasenaHash;
    }
}
