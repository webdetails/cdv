/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company. All rights reserved.
*
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

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
