/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ac.cr.una.test;

import ac.cr.una.app.Cliente;
import org.junit.Test;

/**
 *
 * @author jose
 */
public class TestCliente {
    
    public TestCliente() {
    }
    
    @Test
    public void ClenteTest(){
        Cliente c = new Cliente("/home/josue/Escritorio/CarpetaSinc/cliente1", "localhost");
        c.init();
       
    }  
    
}
