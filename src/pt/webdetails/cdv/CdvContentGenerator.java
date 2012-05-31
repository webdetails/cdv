/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdv;

import java.io.OutputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.engine.services.solution.BaseContentGenerator;
import org.springframework.security.wrapper.SavedRequestAwareWrapper;
import pt.webdetails.cdv.Router.HttpMethod;

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
        } else {
            Router.getBaseRouter().route(method, path, out, pathParams, requestParams);
        }
    }

    private void setResponseHeaders(final String mimeType, final String attachmentName) {
        // Make sure we have the correct mime type
        final HttpServletResponse response = (HttpServletResponse) parameterProviders.get("path").getParameter("httpresponse");
        response.setHeader("Content-Type", mimeType);
        if (attachmentName != null) {
            response.setHeader("content-disposition", "attachment; filename=" + attachmentName);
        } // We can't cache this requests
        response.setHeader("Cache-Control", "max-age=0, no-store");
    }

    private void refresh(OutputStream out, IParameterProvider pathParams, IParameterProvider requestParams) {
        CdvLifecycleListener.reInit();
    }
}
