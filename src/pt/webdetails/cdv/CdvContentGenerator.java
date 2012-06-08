/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdv;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.ServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.engine.services.solution.BaseContentGenerator;
import org.springframework.security.wrapper.SavedRequestAwareWrapper;
import pt.webdetails.cdv.Router.HttpMethod;
import pt.webdetails.cpf.InterPluginCall;

/**
 *
 * @author pdpi
 */
public class CdvContentGenerator extends BaseContentGenerator {

    public static final String CDW_EXTENSION = ".cdw";
    public static final String PLUGIN_NAME = "cdv";
    public static final String PLUGIN_PATH = "system/" + CdvContentGenerator.PLUGIN_NAME + "/";
    private static final Log logger = LogFactory.getLog(CdvContentGenerator.class);
    private static final String MIME_XML = "text/xml";
    private static final String MIME_HTML = "text/html";
    private static final String MIME_SVG = "image/svg+xml";
    private static final String MIME_PNG = "image/png";

    private enum methods {

        RUNTEST, REFRESH
    }

    private enum outputTypes {

        SVG, PNG, PDF
    }

    @Override
    public Log getLogger() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void createContent() throws Exception {

        final IParameterProvider requestParams = parameterProviders.get(IParameterProvider.SCOPE_REQUEST);
        final IParameterProvider pathParams = parameterProviders.get("path");
        OutputStream out = outputHandler.getOutputContentItem("response", "content", "", instanceId, MIME_HTML).getOutputStream(null);
        final String path = pathParams.getStringParameter("path", null);

        SavedRequestAwareWrapper wrapper = (SavedRequestAwareWrapper) pathParams.getParameter("httprequest");
        HttpMethod method = HttpMethod.valueOf(wrapper.getMethod());
        if ("/refresh".equals(path)) {
            refresh(out, pathParams, requestParams);
        } else if ("/home".equals(path)) {
            home(out, pathParams, requestParams);
        } else if ("/validations".equals(path)) {
            validations(out, pathParams, requestParams);
        } else if ("/cdaErrors".equals(path)) {
            cdaErrors(out, pathParams, requestParams);
        } else if ("/slowQueries".equals(path)) {
            slowQueries(out, pathParams, requestParams);
        } else {
            Router.getBaseRouter().route(method, path, out, pathParams, requestParams);
        }
    }

    private void refresh(OutputStream out, IParameterProvider pathParams, IParameterProvider requestParams) {
        CdvLifecycleListener.reInit();
    }

    private void home(OutputStream out, IParameterProvider pathParams, IParameterProvider requestParams) throws UnsupportedEncodingException, IOException {
        callCDE("home.wcdf", out, pathParams, requestParams);
    }

    private void validations(OutputStream out, IParameterProvider pathParams, IParameterProvider requestParams) throws UnsupportedEncodingException, IOException {
        callCDE("validations.wcdf", out, pathParams, requestParams);
    }

    private void cdaErrors(OutputStream out, IParameterProvider pathParams, IParameterProvider requestParams) throws UnsupportedEncodingException, IOException {
        callCDE("cdaErrors.wcdf", out, pathParams, requestParams);
    }

    private void slowQueries(OutputStream out, IParameterProvider pathParams, IParameterProvider requestParams) throws UnsupportedEncodingException, IOException {
        callCDE("slowQueries.wcdf", out, pathParams, requestParams);
    }

    private void callCDE(String file, OutputStream out, IParameterProvider pathParams, IParameterProvider requestParams) throws UnsupportedEncodingException, IOException {


        ServletRequestWrapper wrapper = (ServletRequestWrapper) pathParams.getParameter("httprequest");
        String root = wrapper.getServerName() + ":" + wrapper.getServerPort();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("solution", "system");
        params.put("path", "cdv/presentation/");
        params.put("file", file);
        params.put("absolute", "true");
        params.put("root", root);
        parseParameters(params);

        if (requestParams.hasParameter("mode") && requestParams.getStringParameter("mode", "Render").equals("edit")) {
            redirectToCDE(out, params);
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

    private void redirectToCDE(OutputStream out, Map<String, Object> params) throws UnsupportedEncodingException, IOException {


        StringBuilder str = new StringBuilder();
        str.append("<html><head><title>Redirecting</title>");
        str.append("<meta http-equiv=\"REFRESH\" content=\"0; url=../pentaho-cdf-dd/edit?");

        List paramArray = new ArrayList();
        for (Iterator it = params.keySet().iterator(); it.hasNext();) {

            String key = (String) it.next();
            Object value = params.get(key);
            if (value instanceof String) {
                paramArray.add(key + "=" + URLEncoder.encode((String) value, "UTF-8"));
            }

        }

        str.append(StringUtils.join(paramArray, "&"));
        str.append("\"></head>");
        str.append("<body>Redirecting</body></html>");

        out.write(str.toString().getBytes("UTF-8"));
        return;

    }

    private ServletResponse getResponse() {
        return (ServletResponse) parameterProviders.get("path").getParameter("httpresponse");
    }

    private ServletRequest getRequest() {
        return (ServletRequest) parameterProviders.get("path").getParameter("httprequest");
    }
}
