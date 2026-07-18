/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2002 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package pt.webdetails.cdv;

import pt.webdetails.cpf.RequestHandler;
import pt.webdetails.cpf.http.ICommonParameterProvider;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DummyHandler implements RequestHandler {

  @Override
  public void call( OutputStream out, ICommonParameterProvider pathParams, ICommonParameterProvider requestParams ) {
    try {
      out.write( "Hello World".getBytes( "utf-8" ) );
    } catch ( IOException ex ) {
      Logger.getLogger( DummyHandler.class.getName() ).log( Level.SEVERE, null, ex );
    }
  }
}