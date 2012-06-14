/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdv;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletRequest;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IParameterProvider;
import pt.webdetails.cpf.InterPluginCall;
import pt.webdetails.cpf.RestContentGenerator;
import pt.webdetails.cpf.RestRequestHandler;
import pt.webdetails.cpf.SimpleContentGenerator;
import pt.webdetails.cpf.annotations.AccessLevel;
import pt.webdetails.cpf.annotations.Exposed;

/**
 *
 * @author pdpi
 */
public class CdvContentGenerator extends RestContentGenerator {

    private static final long serialVersionUID = 1L;
    public static final String CDW_EXTENSION = ".cdw";
    public static final String PLUGIN_NAME = "cdv";
    public static final String PLUGIN_PATH = "system/" + CdvContentGenerator.PLUGIN_NAME + "/";
    
    @Override
    public String getPluginName() {
      return PLUGIN_NAME;
    }

//    @Override
//    public void createContent() {//throws Exception {
//
//      try{
//      
//        final IParameterProvider requestParams = parameterProviders.get(IParameterProvider.SCOPE_REQUEST);
//        final IParameterProvider pathParams = parameterProviders.get("path");
//        OutputStream out = getResponseOutputStream(MimeType.HTML);//outputHandler.getOutputContentItem("response", "content", "", instanceId, MimeType.HTML).getOutputStream(null);
//        final String path = pathParams.getStringParameter("path", null);
//
//        SavedRequestAwareWrapper wrapper = (SavedRequestAwareWrapper) pathParams.getParameter("httprequest");
//        HttpMethod method = HttpMethod.valueOf(wrapper.getMethod());
////        if ("/refresh".equals(path)) {
////            refresh(out, pathParams, requestParams);
////        } else if ("/home".equals(path)) {
////            home(out, pathParams, requestParams);
////        } else if ("/tests".equals(path)) {
////            validations(out, pathParams, requestParams);
////        } else if ("/cdaErrors".equals(path)) {
////            cdaErrors(out, pathParams, requestParams);
////        } else if ("/slowQueries".equals(path)) {
////            slowQueries(out, pathParams, requestParams);
////        } else {
////            Router.getBaseRouter().route(method, path, out, pathParams, requestParams);
////        }
//      } catch(Exception e){
//        logger.error(e);
//      }
//    }

    @Exposed(accessLevel = AccessLevel.PUBLIC)
    public void refresh(OutputStream out) {
        CdvLifecycleListener.reInit();
    }

    @Exposed(accessLevel = AccessLevel.PUBLIC)
    public void home(OutputStream out) throws UnsupportedEncodingException, IOException {
        callCDE("home.wcdf", out);
    }

    @Exposed(accessLevel = AccessLevel.PUBLIC)
    public void validations(OutputStream out) throws UnsupportedEncodingException, IOException {
        callCDE("validations.wcdf", out);
    }

    @Exposed(accessLevel = AccessLevel.PUBLIC)
    public void cdaErrors(OutputStream out) throws UnsupportedEncodingException, IOException {
        callCDE("cdaErrors.wcdf", out);
    }

    @Exposed(accessLevel = AccessLevel.PUBLIC)
    public void slowQueries(OutputStream out) throws UnsupportedEncodingException, IOException {
        callCDE("slowQueries.wcdf", out);
    }

    private void callCDE(String file, OutputStream out) throws UnsupportedEncodingException, IOException {

        ServletRequest wrapper = getRequest();
        String root = wrapper.getServerName() + ":" + wrapper.getServerPort();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("solution", "system");
        params.put("path", "cdv/presentation/");
        params.put("file", file);
        params.put("absolute", "true");
        params.put("root", root);
        parseParameters(params);

        IParameterProvider requestParams = getRequestParameters();
        if (requestParams.hasParameter("mode") && requestParams.getStringParameter("mode", "Render").equals("edit")) {
            redirectToCdeEditor(out, params);
            return;
        }

        InterPluginCall pluginCall = new InterPluginCall(InterPluginCall.CDE, "Render", params);
        pluginCall.setResponse(getResponse());
        pluginCall.setOutputStream(out);
        pluginCall.run();
    }

    private void parseParameters(Map<String, Object> params) {
        //add request parameters
        ServletRequest request = getRequest();
        @SuppressWarnings("unchecked")//should always be String
        Enumeration<String> originalParams = request.getParameterNames();
        // Iterate and put the values there
        while (originalParams.hasMoreElements()) {
            String originalParam = originalParams.nextElement();
            params.put(originalParam, request.getParameter(originalParam));
        }
    }

    private void redirectToCdeEditor(OutputStream out, Map<String, Object> params) throws IOException {
      
      StringBuilder urlBuilder = new StringBuilder();
      urlBuilder.append("../pentaho-cdf-dd/edit");
      if(params.size() > 0) urlBuilder.append("?");
      
      List<String> paramArray = new ArrayList<String>();
      for(String key : params.keySet()){
        Object value = params.get(key);
        if (value instanceof String) {
            paramArray.add(key + "=" + URLEncoder.encode((String) value, getEncoding()));
        }
      }

      urlBuilder.append(StringUtils.join(paramArray, "&"));
      redirect(urlBuilder.toString());
    }

    @Override
    public RestRequestHandler getRequestHandler() {
      return Router.getBaseRouter();
    }
}
