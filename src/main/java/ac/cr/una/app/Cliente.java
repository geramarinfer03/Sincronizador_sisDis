/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ac.cr.una.app;

import ac.cr.una.model.ArchivoInfo;
import ac.cr.una.utils.Utils_file;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

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

        this.crearArchivoNoExistente();
        this.archivos_locales = utils_methods.mapearDirectorioLocal(ruta);
        this.cargarListaTxt();
        this.archivos_locales = utils_methods.actualizarLista(archivos_locales, archivos_anteriores);

        ZMQ.Context context = ZMQ.context(1);

        /* ZMQ.Socket accept = context.socket(ZMQ.REQ);
        accept.connect("tcp://localhost:8889");*/
        ZMQ.Socket requester = context.socket(ZMQ.REQ);
        requester.connect("tcp://localhost:8889");


        String list_files_json = new Gson().toJson(this.archivos_locales);
        

        requester.send(list_files_json);

        String replySendLists = new String(requester.recv());

       

        requester.send("Sync");


        ZMsg inMsg = ZMsg.recvMsg(requester);
        String action = inMsg.pop().toString();

        while (!action.equals("termine")) {
           
            switch (action) {

                case "Deleted":
                    this.wasDeleted(inMsg, requester);
                    System.out.println("Eliminando Archivo en servidor");
                    break;
                case "Update":
                    this.sendupdate(inMsg, requester);
                    System.out.println("Actualizando Archivo en servidor");
                    break;

                case "Create":
                    this.sendcreate(inMsg, requester);
                    System.out.println("Creando Archivo en servidor");
                    break;

                case "UpdateC":

                    this.updateCliente(inMsg, requester);
                    System.out.println("Actualizando Archivo en cliente");
                    break;

                case "CreateC":
                    this.createCliente(inMsg, requester);
                    System.out.println("Creando Archivo en cliente");
                    break;

                case "DeleteC":
                    this.DeleteCliente(inMsg, requester);
                    System.out.println("Eliminando Archivo en cliente");
                    break;

                case "Conflict":
                    this.conflict(inMsg, requester);
                    System.out.println("Se creo conflicto de Archivos");
                    break;

                default:
                    requester.send("finish");
                    break;

            }
            inMsg = ZMsg.recvMsg(requester);
            action = inMsg.pop().toString();
        }

        this.saveListLocalFilesTxt();
    }

    private void wasDeleted(ZMsg inMsg, ZMQ.Socket requester) {
        String msg = inMsg.pop().toString();
       

        Predicate<ArchivoInfo> arch = a -> a.getFileName().equals(msg);
        this.archivos_locales.removeIf(arch);
        requester.send("OK");

    }

    private void sendupdate(ZMsg inMsg, ZMQ.Socket requester) {
        try {
            String fileName = inMsg.pop().toString();

            File file = new File(ruta + "/" + fileName);

            byte[] array = Files.readAllBytes(file.toPath());

            ZMsg outMsg = new ZMsg();
            outMsg.add(new ZFrame(fileName));
            outMsg.add(new ZFrame(array));
            outMsg.send(requester);

        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendcreate(ZMsg inMsg, ZMQ.Socket requester) {
        try {
            String fileName = inMsg.pop().toString();

            File file = new File(ruta + "/" + fileName);

            byte[] array = Files.readAllBytes(file.toPath());

            ZMsg outMsg = new ZMsg();
            outMsg.add(new ZFrame(fileName));
            outMsg.add(new ZFrame(array));
            outMsg.send(requester);

        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void createCliente(ZMsg inMsg, ZMQ.Socket requester) {
        try {
            String fileName = inMsg.pop().toString();
            byte[] fileData = inMsg.pop().getData();
            String archivo = inMsg.pop().toString();
            ArchivoInfo archivo_server = new Gson().fromJson(archivo, new TypeToken<ArchivoInfo>() {
            }.getType());
            archivos_locales.add(archivo_server);
            Files.write(Paths.get(ruta + "/" + fileName), fileData);

          
            requester.send("ok");
        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void updateCliente(ZMsg inMsg, ZMQ.Socket requester) {

        try {
            String fileName = inMsg.pop().toString();
            byte[] fileData = inMsg.pop().getData();
            String archivo = inMsg.pop().toString();
            ArchivoInfo archivo_server = new Gson().fromJson(archivo, new TypeToken<ArchivoInfo>() {
            }.getType());

            ArchivoInfo archivo_cliente = this.utils_methods.encontrarArchivoNombre(archivos_locales, fileName);

            archivo_cliente.copy(archivo_server);

            Files.write(Paths.get(ruta + "/" + fileName), fileData);

           
            requester.send("ok");
        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void DeleteCliente(ZMsg inMsg, ZMQ.Socket requester) {
        String fileName = inMsg.pop().toString();

        ArchivoInfo archivo_server = this.utils_methods.encontrarArchivoNombre(archivos_locales, fileName);

        File file = new File(this.ruta + "/" + fileName);
        boolean deleted = file.delete();
        Predicate<ArchivoInfo> arch = a -> a.getFileName().equals(fileName);
        this.archivos_locales.removeIf(arch);
       
        requester.send("ok");
    }

    private void conflict(ZMsg inMsg, ZMQ.Socket requester) {
        try {
            String fileName = inMsg.pop().toString();
            byte[] fileData = inMsg.pop().getData();
            String archivo = inMsg.pop().toString();
            String newName = inMsg.pop().toString();
            ArchivoInfo archivo_server = new Gson().fromJson(archivo, new TypeToken<ArchivoInfo>() {
            }.getType());

            File file = new File(ruta + "/" + fileName);
            byte[] array = Files.readAllBytes(file.toPath());
            ZMsg outMsg = new ZMsg();
            outMsg.add(new ZFrame(array));
            outMsg.send(requester);
            
            boolean deleted = file.delete();
            Files.write(Paths.get(ruta + "/" + fileName), fileData);

            
            Files.write(Paths.get(ruta + "/" + newName), array);

            ArchivoInfo a = this.utils_methods.encontrarArchivoNombre(archivos_locales, fileName);
            ArchivoInfo copia = new ArchivoInfo();
            
            copia.copy(a);
            copia.setFileName(newName);
            copia.setVersion(1);
            copia.setFecha_modificacion(new Date());
            this.archivos_locales.add(copia);
                    
            
            a.copy(archivo_server);

            

        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }

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

    public boolean saveListLocalFilesTxt() {
        try {
            ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(this.ruta + "/" + DOC_LISTA_ARCHIVOS));
            //String json = new Gson().toJson(this.archivos_locales);
           
            salida.writeObject(this.archivos_locales);
            salida.close();
            return true;
        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean crearArchivoNoExistente() {
        String Fichero = this.ruta + "/" + DOC_LISTA_ARCHIVOS;
        File fichero = new File(Fichero);

        if (!fichero.exists()) {
            try {
                ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(fichero));
               
                salida.writeObject(this.archivos_locales);
                salida.close();
                return true;
            } catch (IOException ex) {
                Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }

        return false;
    }

    protected boolean cargarListaTxt() {
        ObjectInputStream entrada = null;
        try {
            entrada = new ObjectInputStream(new FileInputStream(this.ruta + "/" + DOC_LISTA_ARCHIVOS));
            archivos_anteriores = (List<ArchivoInfo>) entrada.readObject();
            entrada.close();
            return archivos_anteriores != null;
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

    }

}
