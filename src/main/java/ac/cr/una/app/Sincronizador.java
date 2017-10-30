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
        //Servidor server = new Servidor("/home/jose/Escritorio/CarpetaSinc/server");
        Cliente cliente = new Cliente("/home/jose/Escritorio/CarpetaSinc/cliente1", "localhost");
       // server.init();
        cliente.init();
    }
}
