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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdv.bean.factory.CoreBeanFactory;
import pt.webdetails.cdv.bean.factory.ICdvBeanFactory;
import pt.webdetails.cdv.util.CdvEnvironment;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.exceptions.InitializationException;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;

import java.io.IOException;
import java.io.InputStream;

public class CdvEngine {

  private static CdvEngine instance;
  protected static Log logger = LogFactory.getLog( CdvEngine.class );
  private ICdvEnvironment cdvEnv;

  private CdvEngine() {
    logger.debug( "Starting ElementEngine" );
  }

  public static CdvEngine getInstance() {
    if ( instance == null ) {
      instance = new CdvEngine();
      try {
        initialize();
      } catch ( Exception ex ) {
        logger.fatal( "Error initializing CdvEngine: " + Util.getExceptionDescription( ex ) );
      }
    }

    return instance;
  }

  public ICdvEnvironment getEnvironment() {
    return getInstance().cdvEnv;
  }

  private static void initialize() throws InitializationException {
    if ( instance.cdvEnv == null ) {

      ICdvBeanFactory factory = new CoreBeanFactory();

      // try to get the environment from the configuration
      // will return the DefaultCdvEnvironment by default
      ICdvEnvironment env = instance.getConfiguredEnvironment( factory );

      if ( env != null ) {
        env.init( factory );
      }

      instance.cdvEnv = env;
    }
  }

  public void ensureBasicDirsAndFiles( String baseSolution )
  {
    instance.ensureBasicDirs(baseSolution);
    instance.ensureBasicFiles(baseSolution);
  }

  private void ensureBasicDirs( String baseSolution ) {
    if ( !ensureDirExists( Util.joinPath( baseSolution , CdvConstants.SolutionFolders.HOME ))) {
      logger.error( "Couldn't find or create cdv plugin repository folder." );
    }
    if ( !ensureDirExists( Util.joinPath( baseSolution, CdvConstants.SolutionFolders.HOME,
      CdvConstants.SolutionFolders.TESTS ))) {
      logger.error( "Couldn't find or create cdv/TESTS plugin repository folder." );
    }
  }

  private boolean ensureDirExists( String relPath ) {
    IUserContentAccess repoBase = CdvEnvironment.getUserContentAccess();
    return repoBase.fileExists( relPath ) || repoBase.createFolder( relPath );
  }

  private void ensureBasicFiles( String baseSolution ) {
    if ( !ensureFileExists( Util.joinPath( baseSolution, CdvConstants.SolutionFolders.HOME,
      CdvConstants.SolutionFiles.NOTIFCATIONS ), CdvConstants.SolutionFiles.NOTIFCATIONS) ) {
      logger.error( "Couldn't find or create cdv/notifications.xml plugin repository file." );
    }
  }

  private boolean ensureFileExists( String fileName, String systemFileName ) {
    IUserContentAccess repoBase = CdvEnvironment.getUserContentAccess();

    if ( !repoBase.fileExists( fileName ) )
    {
      IReadAccess readAccess = CdvEnvironment.getPluginSystemWriter();

      try {
        InputStream stream = readAccess.getFileInputStream( systemFileName );
        return repoBase.fileExists( fileName ) || repoBase.saveFile( fileName, stream );
      } catch ( IOException e ) {
        return false;
      }
    }
    return true;
  }

  public static ICdvEnvironment getEnv() {
    return getInstance().getEnvironment();
  }

  protected synchronized ICdvEnvironment getConfiguredEnvironment( ICdvBeanFactory factory )
    throws InitializationException {

    Object obj = factory.getBean( ICdvEnvironment.class.getSimpleName() );

    if ( obj != null && obj instanceof ICdvEnvironment ) {
      return (ICdvEnvironment) obj;
    } else {
      String msg = "No bean found for ICdvEnvironment!!";
      logger.fatal( msg );
      throw new InitializationException( msg, null );
    }
  }
}