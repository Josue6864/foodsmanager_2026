package com.foodsmanager.seguridad;

import com.foodsmanager.modelo.Administrador;

public final class SesionAdministrador {

    private static int idAdministrador;
    private static String usuario;

    private SesionAdministrador() {
    }

    public static void iniciar(Administrador administrador) {
        if (administrador == null
                || administrador.getIdAdministrador() <= 0) {
            throw new IllegalArgumentException(
                    "El administrador no es válido."
            );
        }

        idAdministrador = administrador.getIdAdministrador();
        usuario = administrador.getUsuario();
    }

    public static boolean haySesionActiva() {
        return idAdministrador > 0;
    }

    public static void exigirSesion() {
        if (!haySesionActiva()) {
            throw new SecurityException(
                    "Debe iniciar sesión como administrador."
            );
        }
    }

    public static String obtenerUsuario() {
        exigirSesion();
        return usuario;
    }

    public static void cerrar() {
        idAdministrador = 0;
        usuario = null;
    }
}