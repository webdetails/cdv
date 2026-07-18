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

import javax.servlet.http.HttpServletResponse;

public class ResponseWrapper {

  private HttpServletResponse response;

  public ResponseWrapper( HttpServletResponse response ) {
    this.response = response;
  }

  public HttpServletResponse getResponse() {
    return response;
  }

  public void setResponseHeader( final String header, String value ) {
    if ( response != null ) {
      response.setHeader( header, value );
    }
  }

  public void setOutputType( String type ) {
    setResponseHeader( "Content-Type", type );
  }
}