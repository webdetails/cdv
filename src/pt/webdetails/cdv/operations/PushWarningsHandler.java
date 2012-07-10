/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cdv.operations;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.orientechnologies.orient.core.record.impl.ODocument;

import pt.webdetails.cdv.notifications.Alert;
import pt.webdetails.cdv.notifications.EventManager;
import pt.webdetails.cpf.JsonRequestHandler;
import pt.webdetails.cpf.JsonSerializable;
import pt.webdetails.cpf.Result;
import pt.webdetails.cpf.messaging.PluginEvent;
import pt.webdetails.cpf.persistence.PersistenceEngine;

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
      
      //store on the side
      ODocument doc = getDocument(CLASS, request);
      PersistenceEngine.getInstance().store(null, CLASS, request, doc);
      
      logEvent(event, request);
      
      //store event on the side?
//      String eventId = storeEvent(event);
//      logger.info("event stored on the side, id=" + eventId);
      return Result.getOK("warning received").toJSON();
      
    } catch (Exception e){
      logger.error(e);
      return Result.getFromException(e).toJSON();
    }
  }
  
  public static JsonSerializable listClass(String tableClass) throws JSONException {
    Map<String,Object> params = new HashMap<String, Object>();
//  params.put("classTable", classTable);
    JSONArray array = new JSONArray();
    for(ODocument doc : PersistenceEngine.getInstance().executeQuery("select * from " + tableClass, params)){
//       logger.debug(getJson(doc).toString(2));
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
//      try {
        array.put(getJson(doc));
//        logger.debug("q::" + getJson("cdaTesting",doc).toString(2));
//      } catch (JSONException e) {
//      }
    }
    return Result.getOK(array);
  }

  /**
   * @param event
   * @throws JSONException
   */
  private void logEvent(PluginEvent event, JSONObject jsonEvent) throws JSONException {
    logger.info("[" + event.getPlugin() + ":" + event.getEventType() + "] " +
                "[" + dateFormat.format(new Date(event.getTimeStamp())) + "] \n" + 
                jsonEvent.toString(2));
  }
  
//  private static String storeEvent(PluginEvent event) throws JSONException {
//    
////    ODocument eventDoc = new ODocument(event.getPersistenceClass())
////      .field("eventType", event.getEventType())
////      .field("timeStamp", event.getTimeStamp())
////      .field("eventType", event.getEventType())
////      ; 
//    
//    
//    JSONObject result = PersistenceEngine.getInstance().store(null, event.getPlugin() + event.getEventType(), event.toJSON());
//    boolean ok = result.getBoolean("result");//TODO: Should be Result obj
//    if(!ok){
//      logger.error("Error storing event: " + result.toString(2));
//      return null;
//    }
//    else return result.getString("id");
//  }
  
  //TODO: move to PEngine
  private static ODocument getDocument(String baseClass, JSONObject event){
    ODocument doc = new ODocument(baseClass);
    
    for(String field : new JsonKeyIterable(event)){
      
      if(field.equals("key")) continue;
      
      try {
        Object value = event.get(field);
        if(value instanceof JSONObject){
          
          doc.field(field, getDocument(baseClass + "_" + field, (JSONObject) value ));
          
          JSONObject obj = event.getJSONObject(field);
//          logger.debug("obj:" + obj.toString(2));
        }
        else if(value instanceof StackTraceElement[]){
            continue;
        }
        else {
          doc.field(field, value);
        }
        
      } catch (JSONException e) {
        logger.error(e);
      }
    }
    
    return doc;
  }
  
  private static JSONObject getJson(ODocument doc) {
    JSONObject json = new JSONObject();
    
    for(String field : doc.fieldNames()){
      try{
      Object value = doc.field(field); //doc.<Object>field(field)
      if(value instanceof ODocument){
        ODocument docVal = (ODocument) value;
//        logger.debug("obj odoc:" + docVal.toJSON());
        json.put(field, getJson(docVal));
      }
      else if(value != null) {
//        logger.debug(value.getClass());
        json.put(field, value);
      }
      } catch(JSONException e){
        logger.error(e);
      }
    }
    
    return json;
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
        
        StackTraceElement[] sTrace = ((StackTraceElement[]) request.get("stackTrace"));
        for (int i=0; i < sTrace.length; i++)
            msg += "\n" + sTrace[i].toString();
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
