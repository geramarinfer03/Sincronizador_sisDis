/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ac.cr.una.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jose
 */
public class Utils_file {

    private MessageDigest messageDigest;

    public String MD5_file(String path_file) throws IOException {

        try {
            this.messageDigest = MessageDigest.getInstance("MD5");

            InputStream archivo;

            archivo = new FileInputStream(path_file);
            byte[] buffer = new byte[1];
            int fin_archivo = -1;
            int caracter;

            caracter = archivo.read(buffer);

            while (caracter != fin_archivo) {

                messageDigest.update(buffer); // Pasa texto claro a la función resumen
                caracter = archivo.read(buffer);
            }

            archivo.close();
            byte[] resumen = messageDigest.digest(); // Genera el resumen MD5

            return this.resumeToHexa(resumen);

        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Utils_file.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String resumeToHexa(byte[] resumen) {
        String s = "";
        for (int i = 0; i < resumen.length; i++) {
            s += Integer.toHexString((resumen[i] >> 4) & 0xf);
            s += Integer.toHexString(resumen[i] & 0xf);
        }
        return s;
    }
    
    //https://webprogramacion.com/163/java/hash-md5-y-sha1.aspx
}
