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

package pt.webdetails.cdv.notifications;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Alert {

  public enum Level {

    ALL, OK, WARN, ERROR, CRITICAL
  }

  private Level level;
  private String msg;
  private String group;
  private String name;
  private String summary;
  private Date timestamp;

  public Alert( Level level, String group, String name, String msg, String summary ) {
    init( level, group, name, msg, summary, new Date() );
  }

  private Alert( JSONObject json ) {
    try {
      Level lvl = Level.valueOf( json.getString( "level" ).toUpperCase() );
      String g = json.getString( "group" ),
        n = json.getString( "group" ),
        m = json.getString( "message" ),
        s = json.getString( "summary" );
      init( lvl, g, n, m, s, new Date() );
    } catch ( JSONException e ) {
    }
  }

  public static Alert fromJSON( JSONObject json ) {
    Alert alert = new Alert( json );
    try {
      alert.timestamp = new Date( json.getLong( "timestamp" ) );
    } catch ( JSONException jse ) {
      // Do nothing
    }
    return alert;
  }

  private void init( Level level, String group, String name, String msg, String summary, Date date ) {
    this.level = level;
    this.msg = msg;
    this.name = name;
    this.group = group;
    this.timestamp = date;
    this.summary = summary;
  }

  public Level getLevel() {
    return level;
  }

  public String getGroup() {
    return group;
  }

  public String getMessage() {
    return msg;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public String getName() {
    return name;
  }

  public String getSummary() {
    return summary;
  }

  public String getPersistenceClass() {
    return "Alert";
  }

  public JSONObject getKey() {
    JSONObject json = new JSONObject();
    try {
      json.put( "level", level.toString() );
      json.put( "group", group );
      json.put( "name", name );
      json.put( "timestamp", timestamp.getTime() );
    } catch ( JSONException jse ) {
    }
    return json;
  }

  public JSONObject toJSON() {
    JSONObject json = getKey();
    try {
      json.put( "summary", summary );
      json.put( "message", msg );
    } catch ( JSONException jse ) {
    }
    return json;
  }
}
