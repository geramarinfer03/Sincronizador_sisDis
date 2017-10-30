/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ac.cr.una.sincronizador;

/**
 *
 * @author jose
 */
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;


public class hwclient {

    public static void main(String[] args) {

        try {
            ZMQ.Context context = ZMQ.context(1);
            
            //  Socket to talk to server
            System.out.println("Connecting to hello world server…");
            
            ZMQ.Socket requester = context.socket(ZMQ.REQ);
            requester.connect("tcp://localhost:5555");
            
            /*for (int requestNbr = 0; requestNbr != 10; requestNbr++) {
            String request = "Hello";
            System.out.println("Sending Hello " + requestNbr);
            requester.send(request.getBytes(), 0);

            byte[] reply = requester.recv(0);
            System.out.println("Received " + new String(reply) + " " + requestNbr);
            }*/
            
            String path = "/home/jose/Escritorio/CarpetaSinc/Doc1.txt";
            File dirOrigen  = new File(path);
            byte[] array = Files.readAllBytes(dirOrigen.toPath());
            
            ZMsg outMsg = new ZMsg();
            outMsg.add(new ZFrame("application/xml"));
            outMsg.add(new ZFrame("abc.pdf"));
            outMsg.add(new ZFrame(array)); // here is the data from file
            outMsg.send(requester);
            
            
            
            requester.close();
            context.term();
        } catch (IOException ex) {
            Logger.getLogger(hwclient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}