/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ac.cr.una.app;

import ac.cr.una.model.ArchivoInfo;
import ac.cr.una.utils.Utils_file;
import com.google.gson.Gson;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.zeromq.ZMQ;

/**
 *
 * @author josue
 */
public class Cliente {

    private String ruta;
    private String ip;
    private List<ArchivoInfo> archivos_locales;
    private List<ArchivoInfo> archivos_anteriores;
    
    private Utils_file utils_methods;
    private final String DOC_LISTA_ARCHIVOS = "44a23424ar2qadasc8448125TEMP112";

    public Cliente() {
    }

    public Cliente(String ruta, String ip) {
        archivos_locales = new ArrayList<>();
        archivos_anteriores = new ArrayList<>();
        this.ruta = ruta;
        this.ip = ip;
        this.utils_methods = Utils_file.getInstance();

    }

    public void init() {
        //hace toda la mierda con el servidor y arranca los procesos.
        
       this.archivos_locales =  utils_methods.mapearDirectorioLocal(ruta);
        //this.saveListLocalFilesTxt();
        this.cargarListaTxt();
        this.archivos_locales =  utils_methods.actualizarLista(archivos_locales, archivos_anteriores);
        
         ZMQ.Context context = ZMQ.context(1); 
         
        ZMQ.Socket accept = context.socket(ZMQ.REQ);
        accept.connect("tcp://localhost:8889");
        
        ZMQ.Socket requester = context.socket(ZMQ.REQ);
        requester.connect("tcp://localhost:5555");
        
        accept.send("connection");
        String reply = new String(accept.recv());
        System.out.println(reply);
        
        //Recibiendo la lista 
        String list_files_json = new Gson().toJson(this.archivos_locales);
        System.out.println(list_files_json);
        
        requester.send(list_files_json);
        String replySendLists = new String(requester.recv());
        
        System.out.println(replySendLists);
        
        requester.send("Sync");
       
       /* reply = new String(requester.recv());
        String bro = reply;
        System.out.println(bro);*/
       
        
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public List<ArchivoInfo> getArchivos_locales() {
        return archivos_locales;
    }

    public List<ArchivoInfo> getArchivos_anteriores() {
        return archivos_anteriores;
    }
    
   
    public boolean saveListLocalFilesTxt() throws IOException {
        ObjectOutputStream salida=new ObjectOutputStream(new FileOutputStream(this.ruta + "/"+DOC_LISTA_ARCHIVOS)); 
        //String json = new Gson().toJson(this.archivos_locales);
        salida.writeObject(this.archivos_locales); 
        salida.close();
        return true;
    }
    
    protected boolean cargarListaTxt(){
        ObjectInputStream entrada=null;
        try {
            entrada = new ObjectInputStream(new FileInputStream(this.ruta + "/"+DOC_LISTA_ARCHIVOS));
            archivos_anteriores =(List<ArchivoInfo>)entrada.readObject();
            entrada.close();
            return archivos_anteriores != null;
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
    }
    
    /*protected void actualizarLista(){
        //Actualiza archivos respecto a los anteriores
        archivos_locales.stream().forEach((ArchivoInfo local) -> {
            List<ArchivoInfo> ant = archivos_anteriores.stream()
                    .filter(anterior ->
                            (anterior.getFileName().equals(local.getFileName())))
                    .collect(Collectors.toList());
             if(ant.size() > 0){
                 ArchivoInfo fileInfo = ant.get(0);
                 if(!fileInfo.equals(local)){
                     local.setModified(true);
                     local.setVersion(local.getVersion()+1);
                 }
             }else{
                 local.setModified(true); //archivo nuevo o se renombro, no estaba en los anteriores.
             }
        });
        
        this.encontrarArchivosEliminados();
        
        //revisa si se eliminaron archivos respecto a la vez anterior
      
    }
    
    private void encontrarArchivosEliminados(){
        archivos_anteriores.stream().forEach((ArchivoInfo anterior) ->{
             List<ArchivoInfo> ant= (List<ArchivoInfo>) archivos_locales.stream()
                     .filter(local ->
                            (local.getFileName().equals(anterior.getFileName())))
                    .collect(Collectors.toList());
             
              if(ant.isEmpty()){
                  anterior.setModified(true);
                  anterior.setAction(1); //se va a eliminar en el servidor
                  archivos_locales.add(anterior);
              }
             
        });
       
    }
    */
    

}
