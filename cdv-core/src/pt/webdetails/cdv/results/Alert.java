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

package pt.webdetails.cdv.results;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import pt.webdetails.cdv.notifications.Alert.Level;

public class Alert {

  private static Log logger = LogFactory.getLog( Alert.class );
  private String description;
  private Level type;

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public Level getType() {
    return type;
  }

  public void setType( Level level ) {
    this.type = level;
  }

  public Alert( String description, Level level ) {
    this.description = description;
    this.type = level;
  }

  public JSONObject toJSON() {
    JSONObject json = new JSONObject();
    try {
      json.put( "description", description );
      json.put( "type", type.toString() );
    } catch ( JSONException jse ) {
      logger.error( "failed to create JSON for alert: " + jse.toString() );
    }
    return json;
  }

  public static Alert fromJSON( JSONObject json ) {
    try {
      String description = json.getString( "description" );
      Level type = Level.valueOf( json.getString( "type" ) );
      return new Alert( description, type );
    } catch ( JSONException jse ) {
      throw new IllegalArgumentException( "Couldn't create Alert from JSON", jse );
    }
  }
}