package com.foodsmanager.controlador;

import com.foodsmanager.controlador.ControladorAdministrador;
import com.foodsmanager.modelo.Administrador;
import com.foodsmanager.persistencia.AdministradorDAO;
import com.foodsmanager.seguridad.Contrasenas;
import com.foodsmanager.seguridad.SesionAdministrador;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class ControladorAdministradorTest {

    @TempDir
    Path carpeta;

    private AdministradorDAO dao;
    private ControladorAdministrador controlador;

    @BeforeEach
    void preparar() throws Exception {
        SesionAdministrador.cerrar();

        String url = "jdbc:sqlite:"
                + carpeta.resolve("autenticacion.db");

        try (Connection conexion = DriverManager.getConnection(url);
             Statement sentencia = conexion.createStatement()) {

            sentencia.execute("""
                    CREATE TABLE administrador (
                        id_administrador INTEGER PRIMARY KEY,
                        usuario TEXT NOT NULL COLLATE NOCASE UNIQUE
                            CHECK (length(trim(usuario)) > 0),
                        contrasena_hash TEXT NOT NULL
                            CHECK (length(trim(contrasena_hash)) > 0)
                    )
                    """);
        }

        dao = new AdministradorDAO(
                () -> DriverManager.getConnection(url)
        );

        controlador = new ControladorAdministrador(dao);
    }

    @AfterEach
    void limpiarSesion() {
        SesionAdministrador.cerrar();
    }

    @Test
    void debeRegistrarUnaVezYConservarLaContrasena()
            throws Exception {

        assertTrue(
                dao.crearSiNoExiste("josue", "JosueDemo2026!")
        );

        Administrador original = dao.buscarPorUsuario("josue");

        assertNotEquals(
                "JosueDemo2026!",
                original.getContrasenaHash()
        );

        assertFalse(
                dao.crearSiNoExiste("JOSUE", "OtraContrasena!")
        );

        Administrador conservado = dao.buscarPorUsuario("josue");

        assertEquals(
                original.getIdAdministrador(),
                conservado.getIdAdministrador()
        );

        assertEquals(
                original.getContrasenaHash(),
                conservado.getContrasenaHash()
        );

        Administrador autenticado = controlador.autenticar(
                " JOSUE ",
                "JosueDemo2026!"
        );

        assertNotNull(autenticado);
        assertTrue(autenticado.getIdAdministrador() > 0);

        assertNull(
                controlador.autenticar(
                        "josue",
                        "OtraContrasena!"
                )
        );
    }

    @Test
    void debeRechazarCredencialesIncorrectas() throws Exception {
        dao.crearSiNoExiste("josue", "JosueDemo2026!");

        assertNull(
                controlador.autenticar("josue", "incorrecta")
        );

        assertNull(
                controlador.autenticar(
                        "inexistente",
                        "JosueDemo2026!"
                )
        );

        assertNull(
                controlador.autenticar(
                        "' OR 1=1 --",
                        "JosueDemo2026!"
                )
        );

        assertNull(
                controlador.autenticar(
                        "josue",
                        " JosueDemo2026!"
                )
        );
    }

    @Test
    void debeRechazarCamposVacios() {
        assertThrows(
                IllegalArgumentException.class,
                () -> controlador.autenticar(" ", "clave")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> controlador.autenticar("josue", "")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> controlador.autenticar("josue", null)
        );
    }

    @Test
    void debeVerificarUnHashDeReferenciaYUsarSalesDiferentes() {
        String referencia = "pbkdf2-sha256$600000$"
                + "AAECAwQFBgcICQoLDA0ODw==$"
                + "mH91rp8I3DYj5ELwFLaJ8o8E4aeQNS3KRCBSmKT1RCg=";

        assertTrue(
                Contrasenas.verificar("Prueba2026!", referencia)
        );

        assertFalse(
                Contrasenas.verificar("incorrecta", referencia)
        );

        assertFalse(
                Contrasenas.verificar(
                        "Prueba2026!",
                        "hash-invalido"
                )
        );

        String primero = Contrasenas.crearHash("Prueba2026!");
        String segundo = Contrasenas.crearHash("Prueba2026!");

        assertNotEquals(primero, segundo);

        assertTrue(
                Contrasenas.verificar("Prueba2026!", primero)
        );

        assertTrue(
                Contrasenas.verificar("Prueba2026!", segundo)
        );
    }

    @Test
    void debeExigirSesionYPermitirCerrarla() throws Exception {
        assertThrows(
                SecurityException.class,
                SesionAdministrador::exigirSesion
        );

        dao.crearSiNoExiste("josue", "JosueDemo2026!");

        Administrador administrador = controlador.autenticar(
                "josue",
                "JosueDemo2026!"
        );

        SesionAdministrador.iniciar(administrador);

        assertTrue(SesionAdministrador.haySesionActiva());

        assertEquals(
                "josue",
                SesionAdministrador.obtenerUsuario()
        );

        SesionAdministrador.cerrar();

        assertFalse(SesionAdministrador.haySesionActiva());

        assertThrows(
                SecurityException.class,
                SesionAdministrador::exigirSesion
        );
    }
}