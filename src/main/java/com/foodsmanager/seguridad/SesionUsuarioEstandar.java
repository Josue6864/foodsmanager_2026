package com.foodsmanager.seguridad;

import com.foodsmanager.modelo.UsuarioEstandar;

public final class SesionUsuarioEstandar {

    private static UsuarioEstandar usuarioActual;

    private SesionUsuarioEstandar() {
    }

    public static void iniciar(UsuarioEstandar usuario) {
        if (usuario == null || usuario.getIdUsuario() <= 0) {
            throw new IllegalArgumentException("El usuario no es válido.");
        }
        usuarioActual = usuario;
    }

    public static boolean haySesionActiva() {
        return usuarioActual != null;
    }

    public static UsuarioEstandar obtenerUsuario() {
        if (!haySesionActiva()) {
            throw new SecurityException("Debe iniciar sesión como cliente.");
        }
        return usuarioActual;
    }

    public static void cerrar() {
        usuarioActual = null;
    }
}
