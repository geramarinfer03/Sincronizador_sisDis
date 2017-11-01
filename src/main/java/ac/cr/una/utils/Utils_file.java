/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ac.cr.una.utils;

import ac.cr.una.app.Cliente;
import ac.cr.una.model.ArchivoInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author jose
 */
public class Utils_file {

    private MessageDigest messageDigest;
    private static Utils_file instance;

    private Utils_file() {
        try {
            this.messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Utils_file.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static Utils_file getInstance() {
        if (instance == null) {
            instance = new Utils_file();
        }
        return instance;
    }

    public String MD5_file(String path_file) throws IOException {

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
    public List<ArchivoInfo> mapearDirectorioLocal(String ruta) {
        String DOC_LISTA_ARCHIVOS = "44a23424ar2qadasc8448125TEMP112";
        File dirLocal = new File(ruta);
        List<ArchivoInfo> archivos_locales = new ArrayList<>();
        ArchivoInfo carpetaLocal = new ArchivoInfo(dirLocal.getName(), new Date(dirLocal.lastModified()), 0,
                dirLocal.isDirectory(), 0, false);

        carpetaLocal.toString();

        File archivosLocales[] = dirLocal.listFiles();

        for (File file : archivosLocales) {
            if (!file.getName().equals(DOC_LISTA_ARCHIVOS)) {
                ArchivoInfo filefromDir = new ArchivoInfo();

                filefromDir.setFileName(file.getName());
                filefromDir.setFecha_modificacion(new Date(file.lastModified()));
                if (!file.isDirectory()) {
                    try {
                        filefromDir.setIs_dir(false);
                        filefromDir.setVersion(1);
                        filefromDir.setFile_size(file.getTotalSpace());
                        String md5 = this.MD5_file(file.getAbsolutePath());
                        filefromDir.setMD5_File(md5);
                    } catch (IOException ex) {
                        Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }//else metodo recursivo
                filefromDir.toString();
                archivos_locales.add(filefromDir);
            }

        }

        return archivos_locales;
    }

    public List<ArchivoInfo> actualizarLista(List<ArchivoInfo> archivos_locales, List<ArchivoInfo> archivos_anteriores) {
        //Actualiza archivos respecto a los anteriores
        archivos_locales.stream().forEach((ArchivoInfo local) -> {
            List<ArchivoInfo> ant = archivos_anteriores.stream()
                    .filter(anterior
                            -> (anterior.getFileName().equals(local.getFileName())))
                    .collect(Collectors.toList());
            if (ant.size() > 0) {
                ArchivoInfo fileInfo = ant.get(0);
                System.out.println("VERSION: " + fileInfo.getVersion());
                if (!fileInfo.equals(local)) {
                    local.setModified(true);
                    local.setVersion(fileInfo.getVersion() + 1);
                } else {
                    local.setVersion(fileInfo.getVersion());
                }
            } else {
                local.setModified(true); //archivo nuevo o se renombro, no estaba en los anteriores.
                local.setFecha_modificacion(new Date());
            }
        });

        this.encontrarArchivosEliminados(archivos_locales, archivos_anteriores);

        return archivos_locales;

    }

    private void encontrarArchivosEliminados(List<ArchivoInfo> archivos_locales, List<ArchivoInfo> archivos_anteriores) {
        archivos_anteriores.stream().forEach((ArchivoInfo anterior) -> {
            List<ArchivoInfo> ant = (List<ArchivoInfo>) archivos_locales.stream()
                    .filter(local
                            -> (local.getFileName().equals(anterior.getFileName())))
                    .collect(Collectors.toList());

            if (ant.isEmpty()) {
                anterior.setModified(true);
                anterior.setAction(1); //se va a eliminar en el servidor
                archivos_locales.add(anterior);
            }

        });

    }

    public ArchivoInfo encontrarArchivoNombre(List<ArchivoInfo> archivos, String nombre) {
        List<ArchivoInfo> encontrados = archivos.stream().filter(a -> (a.getFileName().equals(nombre)))
                .collect(Collectors.toList());

        return encontrados.get(0);

    }
}
