package pt.webdetails.cdv;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import pt.webdetails.cpf.JsonRequestHandler;
import pt.webdetails.cpf.Result;
import pt.webdetails.cpf.messaging.PluginEvent;

public class PushWarningsHandler extends JsonRequestHandler {

  private static Log logger = LogFactory.getLog(PushWarningsHandler.class); 
  
  
  @Override
  public JSONObject call(JSONObject request) throws Exception {

    try{
      PluginEvent event = new PluginEvent(request);
      
      logger.info("[" + event.getPlugin() + ":" + event.getEventType() + "] " +
                  "[" + new Date(event.getTimeStamp()) + "] \n" + 
                  event.getEvent().toString(4));
      return Result.getOK("warning received").toJSON();
    } catch (Exception e){
      return Result.getFromException(e).toJSON();
    }
  }

//  @Override
//  public void call(OutputStream out, IParameterProvider pathParams, IParameterProvider requestParams) {
//    try {
//      IOUtils.write("ok", out);
//    } catch (IOException e) {
//      LogFactory.getLog(this.getClass()).error(e);
//    }
//  }

}
