/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utilidades;

/**
 *
 * @author rafam
 */
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Clase para encriptación de contraseñas con el algoritmo SHA-256
 * @author rafam
 */
public class SHA {
    public static String encrypt(String cadena) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

            byte[] inputBytes = cadena.getBytes();

            byte[] hashBytes = sha256.digest(inputBytes);

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            //Devolver el resumen (hash) en forma de cadena hexadecimal
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    
}

