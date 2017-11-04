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
        System.out.println(args.length);
        if (args.length == 2) {
            // parametros: ruta carpeta, ip del servidor
            Cliente cliente = new Cliente(args[0], args[1]);
            cliente.init();

        } else {
            if (args.length == 1) {
                // /home/josue/Escritorio/CarpetaSinc/server
                //parametros: ruta carpeta
                Servidor server = new Servidor(args[0]);
                server.init();
            } else {
                System.out.println("Ingresar parametros para iniciar sistema");
            }

        }

    }
}
