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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

/**
 *
 * @author josue
 */
public class Servidor {

    private String directorio;
    private static final String hostConnections = "tcp://*:8889";
    private static final String hostFiles = "tcp://localhost:5555";
    private ZMQ.Context context;
    private Utils_file utils_methods;
    private List<ArchivoInfo> archivosServer;

    public Servidor(String directorio) {
        this.directorio = directorio;
        utils_methods = Utils_file.getInstance();
        context = ZMQ.context(1);
        archivosServer = new ArrayList<>();
    }

    //<editor-fold defaultstate="collapsed" desc="Set's Get's">
    public String getDirectorio() {
        return directorio;
    }

    public void setDirectorio(String directorio) {
        this.directorio = directorio;
    }
    //</editor-fold>

    public void init() {
        Runnable thread = new ConnectionsReply(hostConnections);
        //thread.run(); //Corrre hilo para hacer conexiones

        new Thread(thread).start();

        this.archivosServer = utils_methods.mapearDirectorioLocal(directorio);

        iniciarListenerReplies4Files();

    }

    public void iniciarListenerReplies4Files() {
        System.out.println("esperando solicitudes.");
        ZMQ.Socket solicitudArchivos = context.socket(ZMQ.REP);
        solicitudArchivos.bind(this.hostFiles);

        while (!Thread.currentThread().isInterrupted()) {
            // ZMsg inMsg = ZMsg.recvMsg(responder);
            byte[] reply = solicitudArchivos.recv(0);
            String replyJson = new String(reply);
            List<ArchivoInfo> archivosCliente;

            System.out.println("Servidor: " + new String(reply));
            solicitudArchivos.send("recibido. conexion");
            if (reply.length > 0) { //probar si llega vacio
                archivosCliente = new Gson().fromJson(replyJson, new TypeToken<ArrayList<ArchivoInfo>>() {
                }.getType());

                /* if (archivosServer != null) {
                      System.out.println("File:  " + archivosServer.get(0).getFileName());
                }*/
                reply = solicitudArchivos.recv();
                String msg = new String(reply);
                System.out.println(msg);

                //Comienza con los metodos para la sincronizacion......
                //solicitudArchivos.send("SIII :D");
                sincronizacion(solicitudArchivos, archivosCliente);

            }//else el cliente 
        }

        solicitudArchivos.close();
        context.term();
    }

    private void sincronizacion(ZMQ.Socket solicitudArchivos, List<ArchivoInfo> cliente) {
        //revisa archivos modificados o eliminados
      //  ZMsg outMsg = new ZMsg();
        System.out.println("En sincronizador "+ archivosServer.toString());
        archivosServer.forEach((ArchivoInfo server_file) -> {
            ZMsg outMsg = new ZMsg();
            // List<ArchivoInfo> ant= cliente.stream().filter( file ->()).collect(Collectors.toList());
            List<ArchivoInfo> files = (List<ArchivoInfo>) cliente.stream()
                    .filter(local
                            -> (local.getFileName().equals(server_file.getFileName())))
                    .collect(Collectors.toList());
            if (!files.isEmpty()) {
                // Encontro el archivo del servidor en la lista cliente
                // para revisar las propiedades del archivo.
                ArchivoInfo archivo = files.get(0);

                if (archivo.equals(server_file)) {
                    // Archivos Iguales -> Si la accion es 0 -> nada
                    //                     Si la accion es 1 -> eliminar Archivo.
                    if (archivo.getAction() == 1) {
                        //Elimina Archivo
                        boolean flag = deleteFile(archivo, outMsg, solicitudArchivos);
                        System.out.println(flag?"Archivo eliminado":"No se pudo Eliminar");
                        
                    }
                }else{ //Archivo con el mismo nombre pero version/MD5 distinto
                    if(server_file.getVersion() > archivo.getVersion()){
                        enviarActualizacion(server_file, outMsg, solicitudArchivos);
                    }
                    
                    if(server_file.getVersion() < archivo.getVersion()){
                        recibirActualizacion(server_file, outMsg, solicitudArchivos);
                    }
                
                }

            }
        });

    }

    protected boolean deleteFile(ArchivoInfo archivo, ZMsg outMsg, ZMQ.Socket solicitudArchivos) {
        File file = new File(this.directorio + "/" + archivo.getFileName());
        boolean deleted = file.delete();
        String msg =  deleted? "Eliminado del servidor"
                : ("Error al eliminar archivo " + archivo.getFileName() + " del servidor");

        outMsg.add(new ZFrame("Deleted"));
        outMsg.add(new ZFrame(msg));
        outMsg.send(solicitudArchivos);
        
        String mensajeCliente = new String(solicitudArchivos.recv());
        return deleted; 
    }
    
    protected void enviarActualizacion(ArchivoInfo archivo, ZMsg outMsg, ZMQ.Socket solicitudArchivos){
         File file = new File(this.directorio + "/" + archivo.getFileName());
        try {
            byte[] array = Files.readAllBytes(file.toPath());
        
         outMsg.add(new ZFrame("Update"));
         outMsg.add(new ZFrame(array));
         outMsg.send(solicitudArchivos);
         
         
          String mensajeCliente = new String(solicitudArchivos.recv());
          } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    protected void recibirActualizacion(ArchivoInfo archivo, ZMsg outMsg, ZMQ.Socket solicitudArchivos){
         outMsg.add(new ZFrame("Updateme"));
         outMsg.add(new ZFrame(archivo.getFileName()));
         outMsg.send(solicitudArchivos);
                
         ZMsg inMsg = ZMsg.recvMsg(solicitudArchivos);
         String fileName = inMsg.pop().toString();
         byte[] fileData = inMsg.pop().getData();

        try {
            
            Files.write(Paths.get(directorio+"/"+fileName),fileData);
            archivo.setVersion(archivo.getVersion()+1);
            System.out.println("Guardando actualizacion");
            
        } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
         
    }

    //<editor-fold defaultstate="collapsed" desc="Class ConnectionsReply">
    class ConnectionsReply implements Runnable {

        private String host;

        public ConnectionsReply(String host) {
            this.host = host;
        }

        @Override
        public void run() {
            ZMQ.Context context = ZMQ.context(1);

            //  Socket to talk to clients
            ZMQ.Socket responder = context.socket(ZMQ.REP);
            responder.bind(this.host);
            System.out.println("Inicia while del run");
            while (!Thread.currentThread().isInterrupted()) {
                // ZMsg inMsg = ZMsg.recvMsg(responder);
                byte[] reply = responder.recv(0);
                System.out.println("Servidor: " + new String(reply));
                responder.send("ok");
            }

            responder.close();
            context.term();
        }

    }
//</editor-fold>

}
