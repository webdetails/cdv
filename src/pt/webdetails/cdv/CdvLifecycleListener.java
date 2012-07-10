/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdv;

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.IUserDetailsRoleListService;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.repository.ISolutionRepositoryService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.UserSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.providers.anonymous.AnonymousAuthenticationToken;
import pt.webdetails.cdv.notifications.NotificationEngine;
import pt.webdetails.cdv.operations.PushWarningsHandler;
import pt.webdetails.cdv.scripts.GlobalScope;
import pt.webdetails.cpf.RestRequestHandler.HttpMethod;
import pt.webdetails.cpf.persistence.PersistenceEngine;
import pt.webdetails.cpf.repository.RepositoryAccess;

/**
 * This class inits Cdv plugin within the bi-platform
 * @author pdpi
 *
 */
public class CdvLifecycleListener implements IPluginLifecycleListener {

    static Log logger = LogFactory.getLog(CdvLifecycleListener.class);

    
   private static IPentahoSession getAdminSession() {
        IUserDetailsRoleListService userDetailsRoleListService = PentahoSystem.getUserDetailsRoleListService();
        UserSession session = new UserSession("admin", null, false, null);
        GrantedAuthority[] auths = userDetailsRoleListService.getUserRoleListService().getAllAuthorities();
        Authentication auth = new AnonymousAuthenticationToken("admin", SecurityHelper.SESSION_PRINCIPAL, auths);
        session.setAttribute(SecurityHelper.SESSION_PRINCIPAL, auth);
        session.doStartupActions(null);
        return session;
    }
     
    
    
    public void init() throws PluginLifecycleException {
        reInit();
    }

    public static void reInit() {
        PersistenceEngine pe  = PersistenceEngine.getInstance();
        
        
        //Make sure the CDV document classes exist
        pe.initializeClass("TestResult");
        pe.initializeClass("Alert");
        pe.initializeClass("cdaEvent");
        pe.initializeClass("test");
        
        
        NotificationEngine.getInstance();
        GlobalScope scope = GlobalScope.reset();
        Router.resetBaseRouter().registerHandler(HttpMethod.GET, "/hello", new DummyHandler());
        Router.getBaseRouter().registerHandler(HttpMethod.GET, "/warnings", new PushWarningsHandler());
//        Router.getBaseRouter().registerHandler(HttpMethod.GET, "/listCda", new ListPluginNotifications());
//        Router.getBaseRouter().registerHandler(HttpMethod.POST, "/warnings", new PushWarningsHandler());
        scope.executeScript("system/cdv/js/bootstrap.js");
    }

  @Override
  public void loaded() throws PluginLifecycleException {
    //Check if folder cdb and cdb/saiku and cdb/queries exist
    IPentahoSession adminSession = getAdminSession();
    ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, adminSession);
    ISolutionRepositoryService repService = PentahoSystem.get(ISolutionRepositoryService.class, adminSession);
    if (!solutionRepository.resourceExists("cdv")) {
      try {
        repService.createFolder(adminSession, "", "", "cdv", "CDV");                      
      } catch (IOException ioe) {
        logger.error("Error while creating folder cdv for cdv plugin. CDV will not work", ioe);
      }
    }
    
    if (!solutionRepository.resourceExists("cdv/tests")) {
      try {
        repService.createFolder(adminSession, "", "cdv", "tests", "tests");                
      } catch (IOException ioe) {
        logger.error("Error while creating folder cdv/tests for cdv plugin. CDV will not work", ioe);
      }      
    }

    if (!solutionRepository.resourceExists("cdv/notifications.xml")) {
      try {

        RepositoryAccess.getRepository().copySolutionFile("system/cdv/notifications.xml", "cdv/notifications.xml");
      } catch (IOException ioe) {
        logger.error("Error while creating default notifications.xml for CDV plugin. CDV will use the one at system/cdv", ioe);
      }      
    }
    
    
    
  }

    public void unLoaded() throws PluginLifecycleException {
    }
}
