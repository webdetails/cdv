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

public class ValidationResult {


  private static Log logger = LogFactory.getLog( ValidationResult.class );
  private Alert alert;
  private String description;
  private String name;
  private String type;

  public ValidationResult( Alert alert, String description, String name, String type ) {
    this.alert = alert;
    this.description = description;
    this.name = name;
    this.type = type;
  }

  public Alert getAlert() {
    return alert;
  }

  public void setAlert( Alert alert ) {
    this.alert = alert;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType( String type ) {
    this.type = type;
  }

  public JSONObject toJSON() {
    JSONObject json = new JSONObject();
    try {
      json.put( "type", type );
      json.put( "name", name );
      json.put( "description", description );
      json.put( "alert", alert.toJSON() );
    } catch ( JSONException jse ) {
      logger.error( "Could't get JSON for Validation Result: " + jse.toString() );
    }
    return json;
  }

  public static ValidationResult fromJSON( JSONObject json ) {
    try {
      String name = json.getString( "name" );
      String description = json.getString( "description" );
      Alert alert = Alert.fromJSON( json.getJSONObject( "alert" ) );
      String type = json.getString( "type" );
      return new ValidationResult( alert, description, name, type );
    } catch ( JSONException jse ) {
      throw new IllegalArgumentException( "Error reading TestResult from JSON", jse );
    }
  }
}