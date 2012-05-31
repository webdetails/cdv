package pt.webdetails.cdv;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import pt.webdetails.cdv.Router.HttpMethod;
import pt.webdetails.cdv.scripts.GlobalScope;

/**
 * This class inits Cdv plugin within the bi-platform
 * @author pdpi
 *
 */
public class CdvLifecycleListener implements IPluginLifecycleListener {

    static Log logger = LogFactory.getLog(CdvLifecycleListener.class);

    public void init() throws PluginLifecycleException {
        reInit();
    }

    public static void reInit() {
        GlobalScope scope = GlobalScope.reset();
        Router.resetBaseRouter().registerHandler(HttpMethod.GET, "/hello", new DummyHandler());
        scope.executeScript("system/cdv/js/cdv.js");
    }

    public void loaded() throws PluginLifecycleException {
    }

    public void unLoaded() throws PluginLifecycleException {
    }
}
