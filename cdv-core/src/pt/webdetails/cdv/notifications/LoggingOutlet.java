/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2002 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


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
