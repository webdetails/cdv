/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdv;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pentaho.platform.api.engine.IParameterProvider;

/**
 *
 * @author pdpi
 */
public class DummyHandler implements Handler{

    @Override
    public void call(OutputStream out, IParameterProvider pathParams, IParameterProvider requestParams) {
        try {
            out.write("Hello World".getBytes("utf-8"));
        } catch (IOException ex) {
            Logger.getLogger(DummyHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
