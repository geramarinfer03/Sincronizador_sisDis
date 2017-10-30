/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ac.cr.una.sincronizador;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;
import static zmq.Utils.bytes;



public class hwserver {

    public static void main(String[] args) throws Exception {
        ZMQ.Context context = ZMQ.context(1);
        //  Socket to talk to clients
        ZMQ.Socket responder = context.socket(ZMQ.REP);
        responder.bind("tcp://*:5555");
        
        ZMsg inMsg = ZMsg.recvMsg(responder);
        String contentType = inMsg.pop().toString();
        String fileName = inMsg.pop().toString();
        byte[] fileData = inMsg.pop().getData();
        
        Files.write(Paths.get("/home/jose/Escritorio/CarpetaSinc/doc2.txt"),fileData);
        System.out.print(fileName);
        
        
        
        
        
        /*while (!Thread.currentThread().isInterrupted()) {
            // Wait for next request from the client
           byte[] request = responder.recv(0);
            System.out.println("Received Hello");

            // Do some 'work'
            Thread.sleep(1000);

            // Send reply back to client
            String reply = "World";
            responder.send(reply.getBytes(), 0);
        }*/
        responder.close();
        context.term();
    }
}