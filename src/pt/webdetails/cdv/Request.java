/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdv;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author pdpi
 */
public class Request {

    private HttpServletResponse response;
    public static final String MIME_HTML = "text/html";
    public static final String MIME_JSON = "application/json";
    public static final String MIME_XML = "application/xml";

    public Request(HttpServletResponse response) {
        this.response = response;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public void setResponseHeader(final String header, String value) {
        if (response != null) {
            response.setHeader(header, value);
        }
    }

    public void setOutputType(String type) {
        setResponseHeader("Content-Type", type);
    }
}
