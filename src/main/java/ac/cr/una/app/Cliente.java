/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ac.cr.una.app;

/**
 *
 * @author josue
 */
public class Cliente {

    private String ruta;
    private String ip;

    public Cliente() {
    }

    public Cliente(String ruta, String ip) {
        this.ruta = ruta;
        this.ip = ip;
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

}
