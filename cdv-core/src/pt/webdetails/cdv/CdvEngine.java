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


package pt.webdetails.cdv;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdv.util.CdvEnvironment;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.bean.IBeanFactory;
import pt.webdetails.cpf.bean.AbstractBeanFactory;
import pt.webdetails.cpf.exceptions.InitializationException;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;

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

      IBeanFactory factory = new AbstractBeanFactory(){
        @Override
        public String getSpringXMLFilename(){ return "cdv.spring.xml"; }
      };

      // try to get the environment from the configuration
      // will return the DefaultCdvEnvironment by default
      ICdvEnvironment env = instance.getConfiguredEnvironment( factory );

      if ( env != null ) {
        env.init( factory );
      }

      instance.cdvEnv = env;
    }
  }

  public void ensureBasicDirsAndFiles() {
    instance.ensureBasicDirs();
    instance.ensureBasicFiles();
  }

  private void ensureBasicDirs() {
    if ( !ensureDirExists( "" ) ) {
      logger.error( "Couldn't find or create cdv plugin repository folder." );
    }
    if ( !ensureDirExists( Util.joinPath( CdvConstants.SolutionFolders.TESTS ) ) ) {
      logger.error( "Couldn't find or create cdv/TESTS plugin repository folder." );
    }
  }


  private boolean ensureDirExists( String relPath ) {
    IRWAccess repoBase = CdvEnvironment.getPluginRepositoryWriter();
    return repoBase.fileExists( relPath ) || repoBase.createFolder( relPath );
  }

  private void ensureBasicFiles() {
    if ( !ensureFileExists( Util.joinPath( CdvConstants.SolutionFiles.NOTIFCATIONS ),
        CdvConstants.SolutionFiles.NOTIFCATIONS ) ) {
      logger.error( "Couldn't find or create cdv/notifications.xml plugin repository file." );
    }
  }

  private boolean ensureFileExists( String fileName, String systemFileName ) {
    IRWAccess repoBase = CdvEnvironment.getPluginRepositoryWriter();

    if ( !repoBase.fileExists( fileName ) ) {
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

  protected synchronized ICdvEnvironment getConfiguredEnvironment( IBeanFactory factory )
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
