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

import com.sun.jersey.api.representation.Form;
import com.sun.jersey.spi.container.ContainerRequest;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import pt.webdetails.cpf.WrapperUtils;
import pt.webdetails.cpf.http.ICommonParameterProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class RestApiUtils {

  public static Map<String, Map<String, Object>> buildBloatedMap( HttpServletRequest request,
                                                                  HttpServletResponse response,
                                                                  HttpHeaders headers ) {
    Map<String, Map<String, Object>> mainMap = new HashMap<String, Map<String, Object>>();

    mainMap.put( "request", buildRequestMap( request, headers ) );
    mainMap.put( "path", buildPathMap( request, response, headers ) );

    return mainMap;

  }

  protected static Map<String, Object> buildRequestMap( HttpServletRequest request, HttpHeaders headers ) {
    Map<String, Object> requestMap = new HashMap<String, Object>();
    Enumeration e = request.getParameterNames();
    while ( e.hasMoreElements() ) {
      Object o = e.nextElement();

      String[] value = request.getParameterValues( o.toString() );

      if ( value.length == 1 ) {
        requestMap.put( o.toString(), value[ 0 ] );
      } else {
        requestMap.put( o.toString(), value );
      }
    }

    if ( headers != null ) {
      Form form =
        ( (ContainerRequest) headers ).getFormParameters();
      for ( String next : form.keySet() ) {
        requestMap.put( next, form.get( next ).get( 0 ) );
      }
    }
    return requestMap;
  }

  protected static Map<String, Object> buildPathMap( HttpServletRequest request, HttpServletResponse response,
                                                     HttpHeaders headers ) {
    Map<String, Object> pathMap = new HashMap<String, Object>();
    pathMap.put( "httprequest", request );
    pathMap.put( "httpresponse", response );
    if ( headers != null ) {
      pathMap.put( "contentType", headers.getRequestHeader( "contentType" ) );
    }
    return pathMap;
  }

  public static IParameterProvider getRequestParameters( Map<String, Map<String, Object>> bloatedMap ) {
    return new SimpleParameterProvider( bloatedMap.get( "request" ) );
  }

  public static IParameterProvider getPathParameters( Map<String, Map<String, Object>> bloatedMap ) {
    return new SimpleParameterProvider( bloatedMap.get( "path" ) );
  }

  public static ICommonParameterProvider getRequestCommonParameters( Map<String, Map<String, Object>> bloatedMap ) {
    return WrapperUtils.wrapParamProvider( getRequestParameters( bloatedMap ) );
  }

  public static ICommonParameterProvider getPathCommonParameters( Map<String, Map<String, Object>> bloatedMap ) {
    return WrapperUtils.wrapParamProvider( getPathParameters( bloatedMap ) );
  }
}
