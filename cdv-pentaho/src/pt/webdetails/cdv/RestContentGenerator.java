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

import pt.webdetails.cpf.RestRequestHandler;
import pt.webdetails.cpf.RestRequestHandler.HttpMethod;
import pt.webdetails.cpf.SimpleContentGenerator;
import pt.webdetails.cpf.WrapperUtils;

import javax.servlet.http.HttpServletRequest;

public abstract class RestContentGenerator extends SimpleContentGenerator {

  private static final long serialVersionUID = 1L;

  public abstract RestRequestHandler getRequestHandler();

  @Override
  public void createContent() throws Exception {
    RestRequestHandler router = getRequestHandler();
    String path = getPathParameters().getStringParameter( "path", null );
    if ( router.canHandle( getHttpMethod(), path ) ) {
      router.route( getHttpMethod(), path, getResponseOutputStream( router.getResponseMimeType() ),
        WrapperUtils.wrapParamProvider( getPathParameters() ),
        WrapperUtils.wrapParamProvider( getRequestParameters() ) );
    } else {
      super.createContent();
    }
  }

  public HttpMethod getHttpMethod() {
    HttpServletRequest request = getRequest();
    String method = ( request == null ) ? null : getRequest().getMethod();
    return ( method != null ) ? HttpMethod.valueOf( method ) : HttpMethod.GET;
  }
}
