package ac.cr.una.app;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author jose
 */
public class Sincronizador {

    public static void main(String[] args) {
        System.out.println("Iniciando");
      
//        Servidor server = new Servidor("/home/josue/Escritorio/CarpetaSinc/server");
//        server.init();
        Cliente cliente = new Cliente("/home/josue/Escritorio/CarpetaSinc/cliente2", "localhost");
        cliente.init();

        
    }
}
