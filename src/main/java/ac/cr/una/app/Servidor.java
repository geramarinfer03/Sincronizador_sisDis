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
import java.util.ArrayList;
import java.util.List;
import org.zeromq.ZMQ;

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

            System.out.println("Servidor: " + new String(reply));
            solicitudArchivos.send("recibido. conexion");
            if (reply.length > 0) {
                archivosServer = new Gson().fromJson(replyJson, new TypeToken<ArrayList<ArchivoInfo>>() {
                }.getType());

               /* if (archivosServer != null) {
                      System.out.println("File:  " + archivosServer.get(0).getFileName());
                }*/
            }
            reply = solicitudArchivos.recv();
             String msg = new String(reply);
             System.out.println(msg);
             
             
             //Comienza con los metodos para la sincronizacion......
            
             //solicitudArchivos.send("SIII :D");
             
        }

        solicitudArchivos.close();
        context.term();
    }
    
    private void sincronizacion(ZMQ.Socket solicitudArchivos){
        
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
