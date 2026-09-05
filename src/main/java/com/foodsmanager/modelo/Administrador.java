
package com.foodsmanager.modelo;


/*
  Representa un administrador autorizado de FoodsManager.
  La autenticacion pertenece al controlador y a la capa de persistencia.
 */
public class Administrador {

    private int idAdministrador;
    private String usuario;
    private String contrasenaHash;

    /*
      Construye un administrador recuperado de la base de datos.
     */
    public Administrador(int idAdministrador, String usuario, String contrasenaHash) {
        setIdAdministrador(idAdministrador);
        setUsuario(usuario);
        setContrasenaHash(contrasenaHash);
    }

    /*
      Construye un administrador nuevo que todavia no ha sido guardado.
     */
    public Administrador(String usuario, String contrasenaHash) {
        this(0, usuario, contrasenaHash);
    }

    public int getIdAdministrador() {
        return idAdministrador;
    }

    public void setIdAdministrador(int idAdministrador) {
        if (idAdministrador < 0) {
            throw new IllegalArgumentException("El id del administrador no puede ser negativo.");
        }
        this.idAdministrador = idAdministrador;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = validarTextoObligatorio(usuario, "El usuario es obligatorio.");
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public void setContrasenaHash(String contrasenaHash) {
        this.contrasenaHash = validarTextoObligatorio(
                contrasenaHash,
                "El hash de la contrasena es obligatorio.");
    }

    private static String validarTextoObligatorio(String texto, String mensaje) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException(mensaje);
        }
        return texto.trim();
    }

    @Override
    public String toString() {
        return "Administrador{" +
                "idAdministrador=" + idAdministrador +
                ", usuario='" + usuario + '\'' +
                '}';
    }
}