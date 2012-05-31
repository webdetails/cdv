/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdv;

import java.io.OutputStream;
import org.pentaho.platform.api.engine.IParameterProvider;

/**
 *
 * @author pdpi
 */
public interface Handler {
    public void call(OutputStream out, IParameterProvider pathParams, IParameterProvider requestParams);
}
