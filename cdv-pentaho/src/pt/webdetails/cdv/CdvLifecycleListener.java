/*!
* Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
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

package pt.webdetails.cdv;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import pt.webdetails.cdv.notifications.NotificationEngine;
import pt.webdetails.cdv.operations.PushWarningsHandler;
import pt.webdetails.cdv.scripts.GlobalScope;
import pt.webdetails.cdv.util.CdvEnvironment;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.RestRequestHandler;
import pt.webdetails.cpf.SimpleLifeCycleListener;
import pt.webdetails.cpf.persistence.PersistenceEngine;

public class CdvLifecycleListener extends SimpleLifeCycleListener {

  static Log logger = LogFactory.getLog( CdvLifecycleListener.class );

  public void init() throws PluginLifecycleException {
    logger.debug( "Init for CDV" );
    reInit();
  }

  public static void reInit() {
    CdvEnvironment.ensureDefaultDirAndFilesExists();
    PersistenceEngine pe = PersistenceEngine.getInstance();

    if ( !pe.classExists( "TestResult" ) ) {
      pe.initializeClass( "TestResult" );
    }
    if ( !pe.classExists( "Alert" ) ) {
      pe.initializeClass( "Alert" );
    }
    if ( !pe.classExists( "cdaEvent" ) ) {
      pe.initializeClass( "cdaEvent" );
    }
    if ( !pe.classExists( "test" ) ) {
      pe.initializeClass( "test" );
    }

    NotificationEngine.getInstance();
    GlobalScope scope = GlobalScope.reset();
    Router.resetBaseRouter().registerHandler( RestRequestHandler.HttpMethod.GET, "/hello", new DummyHandler() );
    Router.getBaseRouter().registerHandler( RestRequestHandler.HttpMethod.GET, "/warnings", new PushWarningsHandler() );
    scope.executeScript( "js/bootstrap.js" );
  }

  @Override
  public void loaded() throws PluginLifecycleException {
    logger.debug( "Load for CDV" );
  }


  @Override
  public PluginEnvironment getEnvironment() {
    return (PluginEnvironment) CdvEngine.getInstance().getEnvironment();
  }
}
