/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cdv;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import javax.servlet.ServletRequest;

import org.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.pentaho.platform.api.engine.IParameterProvider;

import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import pt.webdetails.cdv.operations.PushWarningsHandler;
import pt.webdetails.cpf.InterPluginCall;
import pt.webdetails.cpf.RestContentGenerator;
import pt.webdetails.cpf.RestRequestHandler;
import pt.webdetails.cpf.Result;
import pt.webdetails.cpf.annotations.AccessLevel;
import pt.webdetails.cpf.annotations.Exposed;
import pt.webdetails.cpf.persistence.PersistenceEngine;
import pt.webdetails.cpf.repository.RepositoryAccess;

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
    public void alerts(OutputStream out) throws UnsupportedEncodingException, IOException {
        callCDE("alerts.wcdf", out);
    }

    @Exposed(accessLevel = AccessLevel.PUBLIC)
    public void cdaErrors(OutputStream out) throws UnsupportedEncodingException, IOException {
        callCDE("cdaErrors.wcdf", out);
    }

    @Exposed(accessLevel = AccessLevel.PUBLIC)
    public void slowQueries(OutputStream out) throws UnsupportedEncodingException, IOException {
        callCDE("slowQueries.wcdf", out);
    }

    @Exposed(accessLevel = AccessLevel.PUBLIC)
    public void notificationSettings(OutputStream out) throws UnsupportedEncodingException, IOException {
        callCDE("notificationSettings.wcdf", out);
    }


    
    private JSONObject createTest(String origin, String newPath) throws JSONException {
        
        JSONObject result = new JSONObject();
        if (!newPath.endsWith(".cdv"))
          newPath = newPath + ".cdv";
        String newFileName = "cdv/tests/" + newPath;
            
        RepositoryAccess repAccess = RepositoryAccess.getRepository();
        try {
          if (repAccess.resourceExists(newFileName)) {
            logger.error("New File already exists, aborting creatio of new test");
            result.put("success", "false");
            return result;
          }
              
          String originalTest = repAccess.getResourceAsString(origin);
          String segment = originalTest.substring(originalTest.indexOf("path:"), originalTest.indexOf("createdBy:"));
          originalTest = originalTest.replaceAll(Matcher.quoteReplacement(segment), "path: '" + newFileName + "',");
          segment = originalTest.substring(originalTest.indexOf("name:"), originalTest.indexOf("group:"));
          originalTest = originalTest.replace(Matcher.quoteReplacement(segment),"name: '" + newFileName.substring(newFileName.lastIndexOf("/")+1).replaceAll(Matcher.quoteReplacement(".cdv"), "")  + "',");
          segment = originalTest.substring(originalTest.indexOf("createdBy:"), originalTest.indexOf("createdAt:"));              
          originalTest = originalTest.replace(Matcher.quoteReplacement(segment),"createdBy: '" + PentahoSessionHolder.getSession().getName()  + "',");              
          repAccess.publishFile(newFileName, originalTest, false);
          result.put("success", "true");
          result.put("path", newFileName);
        } catch (IOException ioe) {
          logger.error("Error while creating test file", ioe);
          result.put("success", "false");              
        }
      
        return result;
    }
    
    
    @Exposed(accessLevel = AccessLevel.PUBLIC, outputType = MimeType.JSON)
    public void newTest(OutputStream out) throws IOException, JSONException {
      String newName = getRequestParameters().getStringParameter("newName", null);
      JSONObject result = createTest("system/cdv/validationTemplate.cdv", newName);      
       writeOut(out, result.toString(2));      
    }
    
    
    
    @Exposed(accessLevel = AccessLevel.PUBLIC, outputType = MimeType.JSON)
    public void duplicateTest(OutputStream out) throws IOException, JSONException {
        String path = getRequestParameters().getStringParameter("path", null);
        String newName = getRequestParameters().getStringParameter("newName", null);
        JSONObject result;
        
        //Need to validate this a bit more - ensure that path is in cdv/tests at least
        if (path != null) {          
            result = createTest(path, newName);
        } else {
          result = new JSONObject();
          result.put("success", "false");
        }
        writeOut(out, result.toString(2));
    }
    
    
    
    //TODO: TEMP!
    @Exposed(accessLevel = AccessLevel.ADMIN, outputType = MimeType.PLAIN_TEXT)
    public void deleteTable(OutputStream out) throws IOException {
        String classTable = getRequestParameters().getStringParameter("class", null);
        if (classTable != null) {
            int deleted = PersistenceEngine.getInstance().deleteAll(classTable);
            writeOut(out, "deleted " + deleted + " instances");
        } else {
            writeOut(out, "No class");
        }
    }

    //TODO:TEMP!
    @Exposed(accessLevel = AccessLevel.ADMIN, outputType = MimeType.JSON)
    public void listTable(OutputStream out) throws IOException, JSONException {
        String classTable = getRequestParameters().getStringParameter("class", null);

        if (classTable != null) {
            writeOut(out, PushWarningsHandler.listClass(classTable));
        } else {
            writeOut(out, Result.getError("No class"));
        }
    }

    //TODO:TEMP!
    @Exposed(accessLevel = AccessLevel.ADMIN, outputType = MimeType.JSON)
    public void listCda(OutputStream out) throws IOException, JSONException {
        String dataAccessId = getRequestParameters().getStringParameter("dataAccessId", null);
        String settingsId = getRequestParameters().getStringParameter("cdaSettingsId", null);

        if (!StringUtils.isEmpty(dataAccessId) && !StringUtils.isEmpty(settingsId)) {
            writeOut(out, PushWarningsHandler.listClass(settingsId, dataAccessId));
        } else {
            writeOut(out, Result.getError("Something missing"));
        }
    }

//    //TODO:TEMP!
//    @Exposed(outputType = MimeType.JSON)
//    public void testCdaStuff(OutputStream out) throws Exception {
//      JSONObject event = new JSONObject(
//"{\"plugin\":\"cda\",\"eventType\":\"QueryTooLong\",\"timestamp\":\"1340291929309\",\"event\":{\"duration\":42,\"queryInfo\":{\"dataAccessId\":\"olapQuery\",\"query\":\"some query\",\"cdaSettingsId\":\"testing/testeMdxCat.cda\",\"parameters\":{}}}}");
//      PushWarningsHandler h = new PushWarningsHandler();
//      writeOut(out, h.call(event).toString(2));
//    }
    private void callCDE(String file, OutputStream out) throws UnsupportedEncodingException, IOException {

        ServletRequest wrapper = getRequest();
        String root = wrapper.getServerName() + ":" + wrapper.getServerPort();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("solution", "system");
        params.put("path", "cdv/presentation/");
        params.put("file", file);
        params.put("absolute", "true");
        params.put("root", root);
        IParameterProvider requestParams = getRequestParameters();
        copyParametersFromProvider(params, requestParams);

        if (requestParams.hasParameter("mode") && requestParams.getStringParameter("mode", "Render").equals("edit")) {
            redirectToCdeEditor(out, params);
            return;
        }

        InterPluginCall pluginCall = new InterPluginCall(InterPluginCall.CDE, "Render", params);
        pluginCall.setResponse(getResponse());
        pluginCall.setOutputStream(out);
        pluginCall.run();
    }

    private void redirectToCdeEditor(OutputStream out, Map<String, Object> params) throws IOException {

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("../pentaho-cdf-dd/edit");
        if (params.size() > 0) {
            urlBuilder.append("?");
        }

        List<String> paramArray = new ArrayList<String>();
        for (String key : params.keySet()) {
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
