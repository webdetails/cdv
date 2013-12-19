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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import pt.webdetails.cdv.CdvConstants;
import pt.webdetails.cdv.notifications.Alert.Level;
import pt.webdetails.cdv.util.CdvEnvironment;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationEngine {

  private static NotificationEngine instance;
  private Map<NotificationKey, List<NotificationOutlet>> alerts;
  private Map<String, Class> outlets;
  private static final Log logger = LogFactory.getLog( NotificationEngine.class );
  private static final String ALL_GROUPS = "*";

  private NotificationEngine() {
    try {
      Document doc;
      SAXReader saxReader = new SAXReader();

      if ( CdvEnvironment.getPluginRepositoryReader().fileExists( CdvConstants.SolutionFiles.NOTIFCATIONS ) ) {
        doc = saxReader.read(
          CdvEnvironment.getPluginRepositoryReader().getFileInputStream( CdvConstants.SolutionFiles.NOTIFCATIONS ) );
      } else {
        doc = saxReader
          .read( CdvEnvironment.getPluginSystemReader().getFileInputStream( CdvConstants.SolutionFiles.NOTIFCATIONS ) );
      }

      listOutlets( doc );
      listAlerts( doc );
    } catch ( IOException e ) {
      logger.error( "Failed to read alert settings", e );
    } catch ( DocumentException e ) {
      logger.error( "Failed to read alert settings", e );
    }
  }

  public synchronized static NotificationEngine getInstance() {
    if ( instance == null ) {
      instance = new NotificationEngine();
    }
    return instance;
  }

  public synchronized static NotificationEngine refresh() {
    instance = null;
    return getInstance();
  }

  private void listOutlets( Document doc ) {
    outlets = new HashMap<String, Class>();
    List<Node> outletNodes = (List<Node>) doc.selectNodes( "//outlets/outlet" );
    for ( Node outletNode : outletNodes ) {
      String outletClassName = "", outletName;
      try {
        outletClassName = outletNode.selectSingleNode( "./@class" ).getStringValue();
        outletName = outletNode.selectSingleNode( "./@name" ).getStringValue();
        Class outletClass = Class.forName( outletClassName );
        outlets.put( outletName, outletClass );
        Method setDefaults = outletClass.getDeclaredMethod( "setDefaults", Node.class );
        setDefaults.invoke( null, outletNode.selectSingleNode( ".//conf" ) );
      } catch ( ClassNotFoundException ex ) {
        logger.error( "Class for outlet " + outletClassName + " not found.", ex );
      } catch ( NoSuchMethodException ex ) {
        logger.error( "Class " + outletClassName + " doesn't provide the necessary interface" );
        logger.error( ex );
      } catch ( InvocationTargetException ex ) {
        logger.error( "Failed to set defaults on " + outletClassName );
        logger.error( ex );
      } catch ( IllegalAccessException ex ) {
        logger.error( "Failed to set defaults on " + outletClassName );
        logger.error( ex );
      }
    }
  }

  private void listAlerts( Document doc ) {
    alerts = new HashMap<NotificationKey, List<NotificationOutlet>>();
    List<Node> alertNodes = doc.selectNodes( "//alerts/alert" );
    for ( Node alertNode : alertNodes ) {
      String outletName = "";
      try {
        outletName = alertNode.selectSingleNode( "./@outlet" ).getStringValue();
        Class outletClass = outlets.get( outletName );
        if ( outletClass == null ) {
          throw new ClassNotFoundException();
        }
        List<Node> groups = alertNode.selectNodes( "./groups/group" );
        for ( Node group : groups ) {
          Constructor cons = outletClass.getConstructor( Node.class );
          NotificationOutlet outlet = (NotificationOutlet) cons.newInstance( alertNode );
          String minLevel = group.selectSingleNode( "./@threshold" ).getStringValue();
          if ( minLevel.equals( "*" ) ) {
            minLevel = "ALL";
          }
          String groupName = group.selectSingleNode( "./@name" ).getStringValue();
          Level level;
          try {
            level = Level.valueOf( minLevel.toUpperCase() );
          } catch ( Exception ex ) {
            level = Level.WARN;
          }
          NotificationKey key = new NotificationKey( level, groupName );
          if ( !alerts.containsKey( key ) ) {
            alerts.put( key, new ArrayList<NotificationOutlet>() );
          }
          alerts.get( key ).add( outlet );
        }
      } catch ( InstantiationException e ) {
        logger.error( "Failed to instantiate " + outletName, e );
      } catch ( ClassNotFoundException ex ) {
        logger.error( "Failed to get class for outlet " + outletName, ex );
      } catch ( NoSuchMethodException ex ) {
        logger.error( "Class " + outletName + " doesn't provide the necessary interface" );
        logger.error( ex );
      } catch ( InvocationTargetException ex ) {
        logger.error( "Failed to set defaults on " + outletName );
        logger.error( ex );
      } catch ( IllegalAccessException ex ) {
        logger.error( "Failed to set defaults on " + outletName );
        logger.error( ex );
      }
    }
  }

  public void publish( Alert not ) {
    Level level = not.getLevel();
    String group = not.getGroup();
    NotificationKey key = new NotificationKey( level, group );
    List<NotificationOutlet> targets = alerts.get( key );
    if ( targets != null ) {
      for ( NotificationOutlet outlet : targets ) {
        outlet.publish( not );
      }
    }
    NotificationKey allKey = new NotificationKey( level, ALL_GROUPS );
    targets = alerts.get( allKey );
    if ( targets != null ) {
      for ( NotificationOutlet outlet : targets ) {
        outlet.publish( not );
      }
    }
  }
}

class NotificationKey {

  private Level level;
  private String group;

  public NotificationKey( Level l, String g ) {
    level = l;
    group = g;
  }

  @Override
  public boolean equals( Object obj ) {
    if ( obj == null ) {
      return false;
    }
    if ( getClass() != obj.getClass() ) {
      return false;
    }
    final NotificationKey other = (NotificationKey) obj;

    return this.level == other.level && !( ( this.group == null ) ? ( other.group != null ) :
      !this.group.equals( other.group ) );
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 17 * hash + ( this.level != null ? this.level.hashCode() : 0 );
    hash = 17 * hash + ( this.group != null ? this.group.hashCode() : 0 );
    return hash;
  }
}