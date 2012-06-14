/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

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
public class DummyHandler implements RequestHandler{

    @Override
    public void call(OutputStream out, IParameterProvider pathParams, IParameterProvider requestParams) {
        try {
            out.write("Hello World".getBytes("utf-8"));
        } catch (IOException ex) {
            Logger.getLogger(DummyHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
