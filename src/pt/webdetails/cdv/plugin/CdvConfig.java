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

  //@Override
  public String getPluginName() {
    return "cdv";
  }
}
