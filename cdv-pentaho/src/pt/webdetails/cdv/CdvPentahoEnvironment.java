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
import pt.webdetails.cpf.bean.IBeanFactory;
import pt.webdetails.cpf.PentahoPluginEnvironment;
import pt.webdetails.cpf.exceptions.InitializationException;

public class CdvPentahoEnvironment extends PentahoPluginEnvironment implements ICdvEnvironment {

  protected static Log logger = LogFactory.getLog( CdvPentahoEnvironment.class );

  private static final String PLUGIN_REPOSITORY_DIR = "cdv";
  private static final String SYSTEM_DIR = "system";
  private static final String PLUGIN_ID = "cdv";
  private static final String JS_DIR = "js/";
  private static final String TESTS_DIR = "cdv/tests/";

  private IBeanFactory factory;

  public void init( IBeanFactory factory ) throws InitializationException {
    this.factory = factory;
    init( this );
  }

  public void refresh() {
    try {
      init( this.factory );
    } catch ( InitializationException e ) {
      logger.error( "CdvPentahoEnvironment.refresh()", e );
    }
  }

  @Override
  public String getPluginRepositoryDir() {
    return PLUGIN_REPOSITORY_DIR;
  }

  @Override
  public String getSystemDir() {
    return SYSTEM_DIR;
  }

  @Override
  public String getPluginId() {
    return PLUGIN_ID;
  }

  @Override
  public String getJsDir() {
    return JS_DIR;
  }

  @Override
  public String getTestsDir() {
    return TESTS_DIR;
  }

  @Override
  public void ensureDefaultDirAndFilesExists() {
    CdvEngine.getInstance().ensureBasicDirsAndFiles();
  }
}
