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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pt.webdetails.cdv.notifications.Alert.Level;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestResult {

  private static Log logger = LogFactory.getLog( TestResult.class );
  private List<ValidationResult> results;
  private Date timestamp;
  private Level overallLevel;
  private Level durationLevel;
  private String overallDescription;
  private String name;
  private String group;
  private Alert cause;
  private int expectedDuration;
  private int actualDuration;

  public TestResult( String json ) {
    try {
      init( new JSONObject( json ) );
    } catch ( JSONException jse ) {
      throw new IllegalArgumentException( "Couldn't restore TestResult from JSON", jse );
    }
  }

  public TestResult( JSONObject json ) {
    init( json );
  }

  private void init( JSONObject json ) {
    try {
      timestamp = new Date( json.getLong( "timestamp" ) );

      JSONObject duration = json.getJSONObject( "duration" );
      expectedDuration = duration.getInt( "expected" );
      actualDuration = duration.getInt( "duration" );
      durationLevel = Level.valueOf( duration.getString( "type" ) );

      JSONObject test = json.getJSONObject( "test" );
      name = test.getString( "name" );
      group = test.getString( "group" );

      JSONObject testResult = json.getJSONObject( "testResult" );
      cause = Alert.fromJSON( testResult.getJSONObject( "cause" ) );
      overallDescription = testResult.getString( "description" );
      overallLevel = Level.valueOf( testResult.getString( "type" ) );

      results = new ArrayList<ValidationResult>();
      JSONArray validationResults = json.getJSONArray( "validationResults" );
      for ( int i = 0; i < validationResults.length(); i++ ) {
        JSONObject validation = validationResults.getJSONObject( i );
        results.add( ValidationResult.fromJSON( validation ) );
      }

    } catch ( JSONException jse ) {
      throw new IllegalArgumentException( "Error reading TestResult from JSON", jse );
    }
  }

  public JSONObject toJSON() throws JSONException {
    JSONObject json = new JSONObject();

    try {
      JSONObject testResult = new JSONObject();
      testResult.put( "cause", cause.toJSON() );
      testResult.put( "type", overallLevel.toString() );
      testResult.put( "description", overallDescription );
      json.put( "testResult", testResult );

      JSONArray validationResults = new JSONArray();
      for ( ValidationResult res : results ) {
        validationResults.put( res.toJSON() );
      }
      json.put( "validationResults", validationResults );

      JSONObject durationResult = new JSONObject();
      durationResult.put( "type", durationLevel.toString() );
      durationResult.put( "expected", expectedDuration );
      durationResult.put( "duration", actualDuration );
      json.put( "duration", durationResult );

      json.put( "timestamp", timestamp.getTime() );

      JSONObject test = new JSONObject();
      test.put( "name", name );
      test.put( "group", group );
      json.put( "test", test );
    } catch ( JSONException jse ) {
      logger.error( "Could't get JSON for Validation Result: " + jse.toString() );
    }
    return json;
  }

  public static TestResult fromJSON( JSONObject json ) {
    TestResult result = new TestResult( json );
    return result;
  }

  public String getPersistenceClass() {
    return "TestResult";
  }
}