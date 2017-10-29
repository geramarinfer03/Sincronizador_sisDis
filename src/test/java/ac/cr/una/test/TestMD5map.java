/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ac.cr.una.test;

import ac.cr.una.model.ArchivoInfo;
import ac.cr.una.utils.Utils_file;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;

/**
 *
 * @author jose
 */
public class TestMD5map {
    
    private final Utils_file utils_methods;
    public TestMD5map() {
            
        utils_methods = new Utils_file();
    }
    
    @Test
    public void scanFolder(){
        String path = "/home/jose/Escritorio/CarpetaSinc";
        File dirOrigen  = new File(path);
        
        ArchivoInfo carpeta = new ArchivoInfo(dirOrigen.getName(), new Date(dirOrigen.lastModified()), 0
                , dirOrigen.isDirectory(), 0);
        
        carpeta.toString();
        
        File archivosOrigen[]  = dirOrigen.listFiles();
        ArrayList<ArchivoInfo> lista_archivos = new ArrayList();
        
        for( File file: archivosOrigen){
            ArchivoInfo filefromDir = new ArchivoInfo();
            
            filefromDir.setFileName(file.getName());
            filefromDir.setFecha_modificacion(new Date(file.lastModified()));
            if(!file.isDirectory()){
                try {
                    filefromDir.setIs_dir(false);
                    filefromDir.setVersion(1);
                    filefromDir.setFile_size(file.getTotalSpace());
                    String md5 = utils_methods.MD5_file(file.getAbsolutePath());
                    filefromDir.setMD5_File(md5);
                } catch (IOException ex) {
                    Logger.getLogger(TestMD5map.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
            filefromDir.toString();
            lista_archivos.add(filefromDir);
        }
    }
}
