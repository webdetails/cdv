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

package pt.webdetails.cdv.operations;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.orientechnologies.orient.core.record.impl.ODocument;

import pt.webdetails.cdv.notifications.Alert;
import pt.webdetails.cdv.notifications.EventManager;
import pt.webdetails.cpf.JsonRequestHandler;
import pt.webdetails.cpf.http.ICommonParameterProvider;
import pt.webdetails.cpf.messaging.JsonSerializable;
import pt.webdetails.cpf.Result;
import pt.webdetails.cpf.messaging.PluginEvent;
import pt.webdetails.cpf.persistence.PersistenceEngine;
import pt.webdetails.cpf.utils.CharsetHelper;

public class PushWarningsHandler extends JsonRequestHandler {
  
  private final static String CLASS = "cdaEvent";

  private static Map<String, Alert.Level> cdaEventLevels =  new HashMap<String, Alert.Level>();
  static{
    cdaEventLevels.put("QueryTooLong", Alert.Level.WARN);
    cdaEventLevels.put("QueryError", Alert.Level.ERROR);
    cdaEventLevels.put("ParseError", Alert.Level.WARN);
  }

  private static Log logger = LogFactory.getLog(PushWarningsHandler.class); 
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

  @Override
  public JSONObject call(JSONObject request) throws Exception {

    try{
      PluginEvent event = new PluginEvent(request);

      //publish and store alert;
      Alert alert = getAlertFromEvent(event, request);
      EventManager.getInstance().publish(alert);
      
      //store on the side //TODO: still needed or keep alerts only?
      ODocument doc = getDocument(CLASS, request);
      PersistenceEngine.getInstance().store(null, CLASS, request, doc);
      
      logEvent(event, request);
      return Result.getOK("warning received").toJSON();
      
    } catch (Exception e){
      logger.error(e);
      return Result.getFromException(e).toJSON();
    }
  }
  
  public static JsonSerializable listClass(String tableClass) throws JSONException {
    Map<String,Object> params = new HashMap<String, Object>();
    JSONArray array = new JSONArray();
    for(ODocument doc : PersistenceEngine.getInstance().executeQuery("select * from " + tableClass, params)){
      array.put(getJson(doc));
    }
    return Result.getOK(array);
  }
  
  public static JsonSerializable listClass(String settings, String dataAccessId){
    Map<String,Object> params = new HashMap<String, Object>();
    params.put("settingsId", settings);
    params.put("dataAccessId", dataAccessId);
    String query = "select * from " + CLASS + " where queryInfo.dataAccessId = :dataAccessId AND queryInfo.cdaSettingsId = :settingsId";
    JSONArray array = new JSONArray();
    for(ODocument doc : PersistenceEngine.getInstance().executeQuery(query, params)){
        array.put(getJson(doc));
    }
    return Result.getOK(array);
  }

  /**
   * @param event
   * @throws org.json.JSONException
   */
  private void logEvent(PluginEvent event, JSONObject jsonEvent) throws JSONException {
    logger.info("[" + event.getPlugin() + ":" + event.getEventType() + "] " +
                "[" + dateFormat.format(new Date(event.getTimeStamp())) + "] \n" + 
                jsonEvent.toString(2));
  }
  
  //TODO: move to PEngine
  private static ODocument getDocument(String baseClass, JSONObject event){
    ODocument doc = new ODocument(baseClass);
    
    for(String field : new JsonKeyIterable(event)){
      
      if(field.equals("key")) continue;
      
      try {
        Object value = event.get(field);
        if(value instanceof JSONObject){
          
          doc.field(field, getDocument(baseClass + "_" + field, (JSONObject) value ));
        }
        else {
          doc.field(field, escapeStringLiterals(value));
        }
        
      } catch (JSONException e) {
        logger.error(e);
      }
    }
    
    return doc;
  }
  
  //TODO: will not work for strings containing jsonObjects
  private static Object escapeStringLiterals(Object value){
    if(value instanceof String){
      return StringUtils.replace((String)value, "\"", "\\\"");
    }
    else if(value instanceof JSONArray){
      JSONArray jArray = (JSONArray) value;
      for(int i=0; i< jArray.length();i++){
        try{
          jArray.put(i, escapeStringLiterals(jArray.get(i)));
        } catch (JSONException e){
          logger.error("Error escaping array contents for array " + jArray.toString(), e);
        }
      }
      return jArray;
    }
    else return value;
  }
  
  //TODO: move to PEngine
  private static JSONObject getJson(ODocument doc) {
    JSONObject json = new JSONObject();
    
    for(String field : doc.fieldNames()){
      try{
      Object value = doc.field(field); //doc.<Object>field(field)
      if(value instanceof ODocument){
        ODocument docVal = (ODocument) value;
        json.put(field, getJson(docVal));
      }
      else if(value != null) {
        json.put(field, value);
      }
      } catch(JSONException e){
        logger.error(e);
      }
    }
    
    return json;
  }

  @Override
  public void call( OutputStream out, ICommonParameterProvider pathParams, ICommonParameterProvider requestParams ) {
    Object request = requestParams.getParameter(JSON_REQUEST_PARAM);
    JSONObject jsonRequest = null;
    if(request instanceof JSONObject){
      jsonRequest = (JSONObject) request;
    }
    else if(request instanceof String){
      try {
        jsonRequest = new JSONObject((String) request);
      } catch (JSONException e) {
        String msg = "Error deserializing JSON request '" + request + "'";
        try {
          IOUtils.write( msg, out, CharsetHelper.getEncoding() );
        } catch (IOException e1) {
          LogFactory.getLog(this.getClass()).error("Error writing output ", e1);
        }
        LogFactory.getLog(this.getClass()).error(msg, e);
      }
    }

    try {
      JSONObject result = call(jsonRequest);
      IOUtils.write(result.toString(), out, CharsetHelper.getEncoding());
    } catch (Exception e) {
      LogFactory.getLog(this.getClass()).error("", e);
    }
  }

  static class JsonKeyIterable implements Iterable<String> {

    JSONObject object;
    
    public JsonKeyIterable(JSONObject obj){
      object = obj;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Iterator<String> iterator() {
      return object.keys();
    }
    
  }
  
  //TODO: pass Objects to cpf to get less stringy version?
  private static Alert getAlertFromEvent(PluginEvent event, JSONObject request){
    
    String msg = event.toString();
    String subject = "[" + getLevel(event) + "] " + event.getPlugin();
    if ("QueryTooLong".equals(event.getEventType())) {
      
      try {
        JSONObject queryInfo = new JSONObject(request.getString("queryInfo"));
        msg = "Query " +   queryInfo.getString("dataAccessId") + " in " + request.getString("name") + 
                " took " +  request.getString("duration") + " seconds. \n\n Actual Query: " + 
                queryInfo.getString("query")+ " \n\nParameters: " + queryInfo.getString("parameters"); 
        subject = "CDA Slow Query Alert: " + queryInfo.getString("dataAccessId") + 
                " took " + request.getString("duration") + " seconds";         
      } catch (JSONException jsoe) {
        logger.error("Error while getting alert message from original event", jsoe);
      }     
    } else if ("QueryError".equals(event.getEventType())) {
      try {
        JSONObject queryInfo = new JSONObject(request.getString("queryInfo"));
        msg = "Query " + queryInfo.getString("dataAccessId") + " in " + request.getString("name") + 
                " has failed with exception " + request.getString("message") + " \n\n" +
                "Stack Trace: ";
        
        
        Object[] sTrace = (Object[]) request.get("stackTrace");
        for (int i=0; i < sTrace.length; i++){
            msg += "\n" + sTrace[i].toString();
        }
        msg += "\n\n Actual Query: " + queryInfo.getString("query")+ " \n\nParameters: " + 
                queryInfo.getString("parameters"); 
        subject = "CDA ERROR: Query " + queryInfo.getString("dataAccessId") + " in " + request.getString("name") + 
                " has failed with exception " + request.getString("message");
      } catch (JSONException jsoe) {
        logger.error("Error while getting alert message from original event", jsoe);
      }     
    }
    
    return new Alert(getLevel(event), event.getPlugin(), event.getName() , msg, subject);
  }
  
  private static Alert.Level getLevel(PluginEvent event) {
    if(event.getPlugin().equals("cda")){
      Alert.Level level = cdaEventLevels.get(event.getEventType());
      if(level != null) return level;
    }
    return Alert.Level.WARN;//default
  }

}
