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
package pt.webdetails.cdv;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import pt.webdetails.cdv.notifications.NotificationEngine;
import pt.webdetails.cdv.operations.PushWarningsHandler;
import pt.webdetails.cdv.scripts.GlobalScope;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.RestRequestHandler.HttpMethod;
import pt.webdetails.cpf.SimpleLifeCycleListener;
import pt.webdetails.cpf.persistence.PersistenceEngine;

public class CdvLifecycleListener extends SimpleLifeCycleListener {

  static Log logger = LogFactory.getLog(CdvLifecycleListener.class);

  public void init() throws PluginLifecycleException {
    logger.debug( "Init for CDV" );
    getEnvironment();
    reInit();
  }

  public static void reInit() {
    PersistenceEngine pe  = PersistenceEngine.getInstance();


    //Make sure the CDV document classes exist
    pe.initializeClass( "TestResult" );
    pe.initializeClass( "Alert" );
    pe.initializeClass( "cdaEvent" );
    pe.initializeClass( "test" );


    NotificationEngine.getInstance();
    GlobalScope scope = GlobalScope.reset();
    Router.resetBaseRouter().registerHandler( HttpMethod.GET, "/hello", new DummyHandler() );
    Router.getBaseRouter().registerHandler( HttpMethod.GET, "/warnings", new PushWarningsHandler() );
//        Router.getBaseRouter().registerHandler(HttpMethod.GET, "/listCda", new ListPluginNotifications());
//        Router.getBaseRouter().registerHandler(HttpMethod.POST, "/warnings", new PushWarningsHandler());
    scope.executeScript("system/cdv/js/bootstrap.js"); //system/cdv/


  }

  @Override
  public void loaded() throws PluginLifecycleException {
    logger.debug( "Load for CDV" );
  }

  public void unLoaded() throws PluginLifecycleException {
    logger.debug( "Unload for CDV" );
  }

  @Override
  public PluginEnvironment getEnvironment() {
    return (PluginEnvironment) CdvEngine.getInstance().getEnvironment();
  }
}
