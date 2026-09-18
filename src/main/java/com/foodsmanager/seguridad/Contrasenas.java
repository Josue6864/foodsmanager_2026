package com.foodsmanager.seguridad;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class Contrasenas {

    private static final int ITERACIONES = 600_000;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private Contrasenas() {
    }

    public static String crearHash(String contrasena) {
        if (contrasena == null || contrasena.isBlank()) {
            throw new IllegalArgumentException(
                    "La contraseña es obligatoria."
            );
        }

        byte[] sal = new byte[16];
        ALEATORIO.nextBytes(sal);

        byte[] hash = derivar(contrasena, sal, ITERACIONES);

        return "pbkdf2-sha256$" + ITERACIONES + "$"
                + Base64.getEncoder().encodeToString(sal) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verificar(
            String contrasena,
            String hashGuardado) {

        if (contrasena == null
                || hashGuardado == null
                || hashGuardado.length() > 200) {
            return false;
        }

        try {
            String[] partes = hashGuardado.split("\\$", -1);

            if (partes.length != 4
                    || !"pbkdf2-sha256".equals(partes[0])) {
                return false;
            }

            int iteraciones = Integer.parseInt(partes[1]);

            if (iteraciones < 1 || iteraciones > 2_000_000) {
                return false;
            }

            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);

            if (sal.length != 16 || esperado.length != 32) {
                return false;
            }

            byte[] obtenido = derivar(
                    contrasena,
                    sal,
                    iteraciones
            );

            return MessageDigest.isEqual(esperado, obtenido);

        } catch (IllegalArgumentException excepcion) {
            return false;
        }
    }

    private static byte[] derivar(
            String contrasena,
            byte[] sal,
            int iteraciones) {

        char[] caracteres = contrasena.toCharArray();

        PBEKeySpec especificacion = new PBEKeySpec(
                caracteres,
                sal,
                iteraciones,
                256
        );

        try {
            return SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(especificacion)
                    .getEncoded();

        } catch (GeneralSecurityException excepcion) {
            throw new IllegalStateException(
                    "No se pudo procesar la contraseña.",
                    excepcion
            );
        } finally {
            especificacion.clearPassword();
            Arrays.fill(caracteres, '\0');
        }
    }
}