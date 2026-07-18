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


package pt.webdetails.cdv.utils;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

public class JsonUtils {

  private static final int INDENT = 2;

  public static JSON readJsonFromInputStream( final InputStream input ) throws IOException {

    String contents = StringUtils.trim( IOUtils.toString( input, "UTF-8" ) );
    return (JSON) JSONSerializer.toJSON( contents );
  }

  public static void buildJsonResult( final OutputStream out, final Boolean sucess, final Object result ) {

    final JSONObject jsonResult = new JSONObject();

    jsonResult.put( "status", sucess.toString() );
    if ( result != null ) {
      jsonResult.put( "result", result );
    }

    PrintWriter pw = null;
    try {
      pw = new PrintWriter( out );
      pw.print( jsonResult.toString( INDENT ) );
      pw.flush();
    } finally {
      IOUtils.closeQuietly( pw );
    }
  }

  public static String getJsonResult( boolean success, Object result ) {
    final JSONObject jsonResult = new JSONObject();
    jsonResult.put( "status", Boolean.toString( success ) );
    if ( result != null ) {
      jsonResult.put( "result", result );
    }
    return jsonResult.toString( INDENT );
  }

  public static String toJsString( String text ) {
    String content;
    if ( text == null ) {
      content = "";
    } else {
      content = text.replaceAll( "\"", "\\\\\"" )  // replaceBy: |\\"|
        .replaceAll( "\n", "\\\\n" )  // replaceBy: |\\n|
        .replaceAll( "\r", "\\\\r" ); // replaceBy: |\\r|
    }

    return "\"" + content + "\"";
  }
}
