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

package pt.webdetails.cdv.scripts;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.repository.hibernate.HibernateUtil;
import pt.webdetails.cdv.Router;
import pt.webdetails.cdv.api.CdvApi;
import pt.webdetails.cdv.notifications.EventManager;
import pt.webdetails.cdv.util.CdvEnvironment;
import pt.webdetails.cpf.datasources.DatasourceFactory;
import pt.webdetails.cpf.persistence.PersistenceEngine;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IBasicFileFilter;
import pt.webdetails.cpf.repository.api.IUserContentAccess;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class GlobalScope extends ImporterTopLevel {

  private static final long serialVersionUID = -3528272077278611074L;
  private static final int START_LINE = 1;
  protected static final Log logger = LogFactory.getLog( GlobalScope.class );
  private static GlobalScope _instance;
  private static ContextFactory contextFactory;
  private static IPentahoSession session;

  public static synchronized GlobalScope getInstance() {
    if ( _instance == null ) {
      _instance = new GlobalScope();
    }
    return _instance;
  }

  public static synchronized GlobalScope reset() {
    _instance = new GlobalScope();
    return _instance;
  }

  public GlobalScope() {
    super();
    init();
  }

  private void init() {
    Context cx = getContextFactory().enterContext();
    try {
      cx.initStandardObjects( this );
      String[] names = {
        "registerHandler", "callWithDefaultSession", "print", "lib", "load", "loadTests", "getPluginSetting" };
      defineFunctionProperties( names, GlobalScope.class,
        ScriptableObject.DONTENUM );
      Object wrappedEventManager = Context.javaToJS( EventManager.getInstance(), this );
      ScriptableObject.putProperty( this, "eventManager", wrappedEventManager );
      Object wrappedPersistence = Context.javaToJS( PersistenceEngine.getInstance(), this );
      ScriptableObject.putProperty( this, "persistenceEngine", wrappedPersistence );
      Object wrappedFactory = Context.javaToJS( new DatasourceFactory(), this );
      ScriptableObject.putProperty( this, "datasourceFactory", wrappedFactory );
    } finally {
      Context.exit();
    }

  }

  public static ContextFactory getContextFactory() {
    if ( contextFactory == null ) {
      contextFactory = new ContextFactory();
    }
    return contextFactory;
  }

  public static Object registerHandler( Context cx, Scriptable thisObj,
                                        Object[] args, Function funObj ) {

    String method = args[ 0 ].toString();
    String path = args[ 1 ].toString();
    Function handler = (Function) args[ 2 ];
    try {
      Router.getBaseRouter().registerHandler( Router.HttpMethod.valueOf( method ), path, handler );
    } catch ( Exception e ) {
      return Context.toBoolean( false );
    }
    return Context.toBoolean( true );
  }

  public static Object loadTests( Context cx, Scriptable thisObj,
                                  Object[] args, Function funObj ) {
    IBasicFileFilter cdvFilter = new IBasicFileFilter() {
      public boolean accept( IBasicFile file ) {
        return file.getExtension().equals( "cdv" );
      }
    };
    IUserContentAccess repository = CdvEnvironment.getUserContentAccess();

    if ( repository.fileExists( CdvEnvironment.getTestDir() ) ) {
      List<IBasicFile> files = repository.listFiles( CdvEnvironment.getTestDir(), cdvFilter );
      for ( IBasicFile file : files ) {
        if ( file.isDirectory() ) {
          continue;
        }

        String path = file.getFullPath();
        // workaround for http://jira.pentaho.com/browse/BISERVER-3538
        path = StringUtils.removeStart( path, "/solution" );
        InputStream stream = null;

        try {
          stream = repository.getFileInputStream( path );
          //stream = repository.getResourceInputStream( path, FileAccess.EXECUTE, false );
          cx.evaluateReader( thisObj, new InputStreamReader( stream ), path, 1, null );

        } catch ( Exception e ) {
          logger.error( e );
        } finally {
          IOUtils.closeQuietly( stream );
        }
      }
    }
    // Get the paths ot the necessary files: dependencies and the main script.
    return Context.toBoolean( true );
  }

  public void executeScript( String path ) {
    Context cx = getContextFactory().enterContext();

    executeScript( cx, path, this );
  }

  public static void executeScript( Context cx, String path, Scriptable scope ) {
    //TODO: Verify what path reachs here
    cx.setLanguageVersion( Context.VERSION_1_7 );
    InputStream stream = null;
    try {
      stream = CdvEnvironment.getPluginSystemReader().getFileInputStream( path );
      cx.evaluateReader( scope, new InputStreamReader( stream ), path, START_LINE, null );
    } catch ( Exception e ) {
      logger.error( e );
    } finally {
      IOUtils.closeQuietly( stream );
    }
  }

  public static Object print( Context cx, Scriptable thisObj,
                              Object[] args, Function funObj ) {

    for ( Object arg : args ) {
      String s = Context.toString( arg );
      logger.info( s );
    }
    return Context.getUndefinedValue();
  }

  public static Object load( Context cx, Scriptable thisObj,
                             Object[] args, Function funObj ) {
    String file = args[ 0 ] instanceof NativeJavaObject ? ( (NativeJavaObject) args[ 0 ] ).unwrap().toString() :
      args[ 0 ].toString();
    executeScript( cx, file, thisObj );
    return Context.toBoolean( true );
  }

  public static Object lib( Context cx, Scriptable thisObj,
                            Object[] args, Function funObj ) {
    if ( args == null || args.length < 1 ) {
      throw new IllegalArgumentException( "lib called with insufficient arguments" );
    }
    String file = args[ 0 ].toString();
    executeScript( cx, CdvEnvironment.getJsDir() + file, thisObj );
    return Context.toBoolean( true );
  }

  public static Object callWithDefaultSession( final Context cx, final Scriptable thisObj,
                                               Object[] args, Function funObj ) {
    final Callable callback = (Callable) args[ 0 ];
    try {
      SecurityHelper.getInstance().runAsSystem( new java.util.concurrent.Callable<Object>() {
        @Override public Object call() throws Exception {
          callback.call( cx, GlobalScope.getInstance(), thisObj, null );
          HibernateUtil.closeSession();
          return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
      } );
    } catch ( Exception e ) {
      logger.error( e );
    }
    return Context.toBoolean( true );
  }

  public static Object getPluginSetting( Context cx, Scriptable thisObj,
                                         Object[] args, Function funObj ) {
    String path = args[ 0 ].toString();
    final IPluginResourceLoader resLoader = PentahoSystem.get( IPluginResourceLoader.class, null );
    String settingValue = resLoader.getPluginSetting( CdvApi.class, path );
    return Context.toString( settingValue );
  }

  private static IPentahoSession getSession() {
    if ( session == null ) {
      session = new StandaloneSession( "CDV" );
    }
    return session;
  }
}