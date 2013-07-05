/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdv.plugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cpf.PluginSettings;
import pt.webdetails.cpf.repository.PentahoRepositoryAccess;

public class CdvConfig extends PluginSettings
{
  private static Log logger = LogFactory.getLog(CdvConfig.class);
  
  public static final String PLUGIN_ID = "cdv";
  public static final String PLUGIN_TITLE = "cdv";
  public static final String PLUGIN_SYSTEM_PATH = PLUGIN_ID + "/" ;
  public static final String PLUGIN_SOLUTION_PATH = "system/" + PLUGIN_SYSTEM_PATH;
  
  private static CdvConfig instance;
  private CdvConfig(){
   super();
   setRepository(PentahoRepositoryAccess.getRepository());
  }
  public static CdvConfig getConfig(){
    if(instance == null){
      instance = new CdvConfig();
    }
    return instance;
  }

  @Override
  public String getPluginName() {
    return "cdv";
  }
}
