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


package pt.webdetails.cdv.plugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdv.util.CdvEnvironment;
import pt.webdetails.cpf.PluginSettings;
import pt.webdetails.cpf.repository.api.IRWAccess;

public class CdvConfig extends PluginSettings
{
  private static Log logger = LogFactory.getLog(CdvConfig.class);
  
  public static final String PLUGIN_ID = "cdv";
  public static final String PLUGIN_TITLE = "cdv";
  public static final String PLUGIN_SYSTEM_PATH = PLUGIN_ID + "/" ;
  public static final String PLUGIN_SOLUTION_PATH = "system/" + PLUGIN_SYSTEM_PATH;

  private static CdvConfig instance;

  private CdvConfig( IRWAccess writeAccess ) {
    super( writeAccess );
  }

  public static CdvConfig getConfig() {
    if ( instance == null ) {
      instance = new CdvConfig( CdvEnvironment.getPluginSystemWriter());
    }
    return instance;
  }

  public String getPluginName() {
    return "cdv";
  }
}
