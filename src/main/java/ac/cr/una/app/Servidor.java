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
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
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
    //private static final String hostConnections = "tcp://*:8889";
    private static final String hostFiles = "tcp://localhost:8889";
    private ZMQ.Context context;
    private Utils_file utils_methods;
    private List<ArchivoInfo> archivosServer;
    private List<ArchivoInfo> temporalesServer;
    private Date fecha_modificacion;

    public Servidor(String directorio) {
        this.directorio = directorio;
        utils_methods = Utils_file.getInstance();
        context = ZMQ.context(1);
        archivosServer = new ArrayList<>();
        temporalesServer = new ArrayList<>();
        this.fecha_modificacion = new Date();
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
        //Runnable thread = new ConnectionsReply(hostConnections);
        //thread.run(); //Corrre hilo para hacer conexiones

        // new Thread(thread).start();
        this.archivosServer = utils_methods.mapearDirectorioLocal(directorio);

        iniciarListenerReplies4Files();

    }

    public void iniciarListenerReplies4Files() {
        System.out.println("esperando solicitudes.");
        ZMQ.Socket solicitudArchivos = context.socket(ZMQ.REP);
        solicitudArchivos.bind(this.hostFiles);

        while (!Thread.currentThread().isInterrupted()) {
            // ZMsg inMsg = ZMsg.recvMsg(responder);
            System.out.println("Esperando mensaje.");
            byte[] reply = solicitudArchivos.recv(0);
            String replyJson = new String(reply);
            List<ArchivoInfo> archivosCliente;

           
            solicitudArchivos.send("recibido. conexion");

            String list_files_json = new Gson().toJson(this.archivosServer);
           

            if (reply.length > 0) { //probar si llega vacio
                archivosCliente = new Gson().fromJson(replyJson, new TypeToken<ArrayList<ArchivoInfo>>() {
                }.getType());

               
                reply = solicitudArchivos.recv();
                String msg = new String(reply);
               

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

//        int size = archivosServer.size();
//        solicitudArchivos.send("" + size);
//        String mensajeCliente = new String(solicitudArchivos.recv());
       
        archivosServer.forEach((ArchivoInfo server_file) -> {
            ZMsg outMsg = new ZMsg();
            List<ArchivoInfo> files = (List<ArchivoInfo>) cliente.stream()
                    .filter(local
                            -> (local.getFileName().equals(server_file.getFileName())))
                    .collect(Collectors.toList());
            if (!files.isEmpty()) {
                ArchivoInfo archivo = files.get(0);
                if (archivo.equals(server_file)) {

                    if (archivo.getAction() == 1) {
                        //Elimina Archivo
                        boolean flag = deleteFile(server_file, outMsg, solicitudArchivos);
                       
                    }

                } else {

                    if (server_file.getVersion() > archivo.getVersion()) {
                        enviarActualizacion(server_file, outMsg, solicitudArchivos);
                    }
                    if (server_file.getVersion() < archivo.getVersion()) {
                        //Update Server
                        recibirActualizacion(server_file, archivo, outMsg, solicitudArchivos);
                    } else if (server_file.getVersion() == archivo.getVersion()) {
                       
                        resolverConflicto(server_file, archivo, outMsg, solicitudArchivos);
                    }
                }

            } else {
                this.enviarNuevoArchivo(server_file, outMsg, solicitudArchivos);
            }

        });

        this.ArchivosNuevos(solicitudArchivos, cliente);
        deleteFromArray();
        solicitudArchivos.send("termine");

        if (!this.temporalesServer.isEmpty()) {
            this.archivosServer.addAll(temporalesServer);
            this.temporalesServer = new ArrayList();
        }

        this.fecha_modificacion = new Date();

    }

    public boolean ArchivosNuevos(ZMQ.Socket solicitudArchivos, List<ArchivoInfo> cliente) {
        ZMsg outMsg = new ZMsg();

        cliente.forEach(client -> {
            if (client.getAction() != 1) {
                boolean nuevo = archivosServer.stream().filter(aS -> (aS.getFileName().equals(client.getFileName()))).collect(Collectors.toList()).isEmpty();
                if (nuevo) {
                   
                    this.addFile(client, outMsg, solicitudArchivos);
                }
            }
        });

        return true;
    }

    protected void addFile(ArchivoInfo archivo, ZMsg outMsg, ZMQ.Socket solicitudArchivos) {
        boolean crear = this.fecha_modificacion.compareTo(archivo.getFecha_modificacion()) == -1;
        int a = this.fecha_modificacion.compareTo(archivo.getFecha_modificacion());
       

        if (crear) {
            try {
                outMsg.add(new ZFrame("Create"));
                outMsg.add(new ZFrame(archivo.getFileName()));
                outMsg.send(solicitudArchivos);

                ZMsg inMsg = ZMsg.recvMsg(solicitudArchivos);
                String fileName = inMsg.pop().toString();
                byte[] fileData = inMsg.pop().getData();

                Files.write(Paths.get(directorio + "/" + fileName), fileData);
                this.archivosServer.add(archivo);
                System.out.println("Creando Archivo en servidor");
               

            } catch (IOException ex) {
                Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {

            outMsg.add(new ZFrame("DeleteC"));
            outMsg.add(new ZFrame(archivo.getFileName()));
            outMsg.send(solicitudArchivos);
            System.out.println("Eliminando Archivo en cliente");

            solicitudArchivos.recv();
        }
    }

    protected boolean deleteFile(ArchivoInfo archivo, ZMsg outMsg, ZMQ.Socket solicitudArchivos) {
        File file = new File(this.directorio + "/" + archivo.getFileName());
        boolean deleted = file.delete();
        String msg = archivo.getFileName();

        outMsg.add(new ZFrame("Deleted"));
        outMsg.add(new ZFrame(msg));
        outMsg.send(solicitudArchivos);
       

        archivo.setAction(1);
        System.out.println("Eliminando Archivo en servidor");

        String mensajeCliente = new String(solicitudArchivos.recv());
        return deleted;
    }

    protected void enviarActualizacion(ArchivoInfo archivo, ZMsg outMsg, ZMQ.Socket solicitudArchivos) {
        File file = new File(this.directorio + "/" + archivo.getFileName());
        try {
            byte[] array = Files.readAllBytes(file.toPath());

            String object = new Gson().toJson(archivo);

            outMsg.add(new ZFrame("UpdateC"));
            outMsg.add(new ZFrame(archivo.getFileName()));
            outMsg.add(new ZFrame(array));
            outMsg.add(new ZFrame(object));
            outMsg.send(solicitudArchivos);
            System.out.println("Actualizando Archivo en cliente");

            String mensajeCliente = new String(solicitudArchivos.recv());
        } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void enviarNuevoArchivo(ArchivoInfo archivo, ZMsg outMsg, ZMQ.Socket solicitudArchivos) {

        File file = new File(this.directorio + "/" + archivo.getFileName());
        try {
            byte[] array = Files.readAllBytes(file.toPath());

            String object = new Gson().toJson(archivo);

            outMsg.add(new ZFrame("CreateC"));
            outMsg.add(new ZFrame(archivo.getFileName()));
            outMsg.add(new ZFrame(array));
            outMsg.add(new ZFrame(object));
            outMsg.send(solicitudArchivos);
            System.out.println("Creando Archivo en cliente");

            String mensajeCliente = new String(solicitudArchivos.recv());
        } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void recibirActualizacion(ArchivoInfo server_file, ArchivoInfo client_file, ZMsg outMsg, ZMQ.Socket solicitudArchivos) {
        outMsg.add(new ZFrame("Update"));
        outMsg.add(new ZFrame(server_file.getFileName()));
        outMsg.send(solicitudArchivos);

        ZMsg inMsg = ZMsg.recvMsg(solicitudArchivos);
        String fileName = inMsg.pop().toString();
        byte[] fileData = inMsg.pop().getData();

        try {

            Files.write(Paths.get(directorio + "/" + fileName), fileData);
            //server_file.setVersion(server_file.getVersion() + 1);
            server_file.copy(client_file);
           System.out.println("Actualizando Archivo en servidor");

        } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    protected void resolverConflicto(ArchivoInfo server, ArchivoInfo cliente, ZMsg outMsg, ZMQ.Socket solicitudArchivos) {
        try {
            File file = new File(this.directorio + "/" + server.getFileName());
            byte[] array = Files.readAllBytes(file.toPath());
            String object = new Gson().toJson(server);
            Date d = new Date();
            String newName = server.getFileName() + "-conf-"+d.toString();

            outMsg.add(new ZFrame("Conflict"));
            outMsg.add(new ZFrame(server.getFileName()));
            outMsg.add(new ZFrame(array));
            outMsg.add(new ZFrame(object));
            outMsg.add(new ZFrame(newName));
            outMsg.send(solicitudArchivos);

            ZMsg inMsg = ZMsg.recvMsg(solicitudArchivos);
            byte[] fileData = inMsg.pop().getData();

            

            Files.write(Paths.get(directorio + "/" + newName), fileData);
            ArchivoInfo copia = new ArchivoInfo();
            copia.copy(cliente);
            copia.setVersion(1);
            copia.setFileName(newName);
            copia.setFecha_modificacion(new Date());
            System.out.println("Se creo conflicto de Archivos");

            this.temporalesServer.add(copia);

        } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    protected boolean deleteFromArray() {
        Predicate<ArchivoInfo> arch = a -> a.getAction() == 1;
        boolean band = this.archivosServer.removeIf(arch);
        return band;
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
           
            while (!Thread.currentThread().isInterrupted()) {
                // ZMsg inMsg = ZMsg.recvMsg(responder);
                byte[] reply = responder.recv(0);
               
                responder.send("ok");
            }

            responder.close();
            context.term();
        }

    }
//</editor-fold>

}
