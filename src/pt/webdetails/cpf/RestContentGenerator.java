package pt.webdetails.cpf;

import pt.webdetails.cpf.RestRequestHandler;
import pt.webdetails.cpf.RestRequestHandler.HttpMethod;
import pt.webdetails.cpf.SimpleContentGenerator;
import pt.webdetails.cpf.http.ICommonParameterProvider;

import javax.servlet.http.HttpServletRequest;

public abstract class RestContentGenerator extends SimpleContentGenerator {

  private static final long serialVersionUID = 1L;

  public abstract RestRequestHandler getRequestHandler();
  
  @Override
  public void createContent() throws Exception {

    RestRequestHandler router = getRequestHandler();
    String path = getPathParameters().getStringParameter("path", null);
    if(router.canHandle(getHttpMethod(), path)) {
      router.route(getHttpMethod(), path, getResponseOutputStream(router.getResponseMimeType()), getPathParameters(),
        getRequestParameters());
    }
    else{
      super.createContent();
    }
  }
  
  public HttpMethod getHttpMethod(){
    HttpServletRequest request = getRequest();
    String method = (request == null)? null : getRequest().getMethod();
    return (method != null)? HttpMethod.valueOf(method) : HttpMethod.GET;
  }

}
