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
import org.dom4j.Node;

/**
 * A dummy outlet
 */
public class LoggingOutlet implements NotificationOutlet {

  private static Log log = LogFactory.getLog( LoggingOutlet.class );

  public LoggingOutlet() {
  }

  public LoggingOutlet( Node node ) {

  }

  @Override
  public void publish( Alert alert ) {
    String msg = alert.getTimestamp() + ":" + alert.getGroup() + ":" + alert.getMessage();
    switch( alert.getLevel() ) {
      case OK:
      default:
        log.info( msg );
        break;
      case WARN:
        log.warn( msg );
        break;
      case ERROR:
        log.error( msg );
        break;
      case CRITICAL:
        log.fatal( msg );
        break;
    }
  }

  public static void setDefaults(Node node) {

  }

}
