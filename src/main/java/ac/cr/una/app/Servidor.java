/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ac.cr.una.app;

import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

/**
 *
 * @author josue
 */
public class Servidor {

    private String directorio;
    private static final String host = "tcp://*:8889";

    public Servidor(String directorio) {
        this.directorio = directorio;
    }

    public String getDirectorio() {
        return directorio;
    }

    public void setDirectorio(String directorio) {
        this.directorio = directorio;
    }

    public void init() {
        RunnableTask thread = new RunnableTask(host);
        thread.run();
    }

    class RunnableTask implements Runnable {

        private String host;

        public RunnableTask(String host) {
            this.host = host;
        }

        @Override
        public void run() {
            ZMQ.Context context = ZMQ.context(1);

            //  Socket to talk to clients
            ZMQ.Socket responder = context.socket(ZMQ.REP);
            responder.bind(this.host);

            while (!Thread.currentThread().isInterrupted()) {
                ZMsg inMsg = ZMsg.recvMsg(responder);

                responder.send("ok");
            }

            responder.close();
            context.term();
        }

    }

}
