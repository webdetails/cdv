/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdv.notifications;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Node;
import pt.webdetails.cdv.notifications.Alert.Level;
import pt.webdetails.cpf.repository.RepositoryAccess;

/**
 *
 * @author pdpi
 */
public class NotificationEngine {

    private static NotificationEngine instance;
    private Map<NotificationKey, List<NotificationOutlet>> alerts;
    private Map<String, Class> outlets;
    private static final Log logger = LogFactory.getLog(NotificationEngine.class);

    private NotificationEngine() {
        try {
            Document doc = RepositoryAccess.getRepository().getResourceAsDocument("/solution/cdv/alerts.xml");
            listOutlets(doc);
            listAlerts(doc);
        } catch (IOException e) {
            logger.error("Failed to read alert settings");
        }
    }

    public synchronized static NotificationEngine getInstance() {
        if (instance == null) {
            instance = new NotificationEngine();
        }
        return instance;
    }

    private void listOutlets(Document doc) {
        List<Node> outletNodes = doc.selectNodes("//outlets/outlet");
        for (Node outletNode : outletNodes) {
            String outletClassName = "", outletName;
            try {
                outletClassName = outletNode.selectSingleNode("./@class").getStringValue();
                outletName = outletNode.selectSingleNode("./@name").getStringValue();
                Class outletClass = Class.forName(outletClassName);
                outlets.put(outletName, outletClass);
                Method setDefaults = outletClass.getDeclaredMethod("setDefaults", Node.class);
                setDefaults.invoke(null, outletNode.selectSingleNode(".//conf"));
            } catch (ClassNotFoundException ex) {
                logger.error("Failed to read alert settings");
            } catch (NoSuchMethodException ex) {
                logger.error("Class " + outletClassName + " doesn't provide the necessary interface");
                logger.error(ex);
            } catch (InvocationTargetException ex) {
                logger.error("Failed to set defaults on " + outletClassName);
                logger.error(ex);
            } catch (IllegalAccessException ex) {
                logger.error("Failed to set defaults on " + outletClassName);
                logger.error(ex);
            }
        }
    }

    private void listAlerts(Document doc) {
        List<Node> alertNodes = doc.selectNodes("//alerts/alert");
        for (Node alertNode : alertNodes) {
            String outletName = "";
            try {
                outletName = alertNode.selectSingleNode("./@outlet").getStringValue();
                Class outletClass = outlets.get(outletName);
                if (outletClass == null) {
                    throw new ClassNotFoundException();
                }
                Constructor cons = outletClass.getConstructor(Node.class);
                cons.newInstance(alertNode);
            } catch (InstantiationException e) {
                logger.error("Failed to instantiate " + outletName);
            }catch (ClassNotFoundException ex) {
                logger.error("Failed to read alert settings");
            } catch (NoSuchMethodException ex) {
                logger.error("Class " + outletName + " doesn't provide the necessary interface");
                logger.error(ex);
            } catch (InvocationTargetException ex) {
                logger.error("Failed to set defaults on " + outletName);
                logger.error(ex);
            } catch (IllegalAccessException ex) {
                logger.error("Failed to set defaults on " + outletName);
                logger.error(ex);
            }
        }
    }

    public void publish(Alert not) {
        Level level = not.getLevel();
        String group = not.getGroup();
        NotificationKey key = new NotificationKey(level, group);
        List<NotificationOutlet> targets = alerts.get(key);
        for (NotificationOutlet outlet : targets) {
            outlet.publish(not);
        }
    }
}

class NotificationKey {

    private Level level;
    private String group;

    public NotificationKey(Level l, String g) {
        level = l;
        group = g;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NotificationKey other = (NotificationKey) obj;
        if (this.level != other.level) {
            return false;
        }
        if ((this.group == null) ? (other.group != null) : !this.group.equals(other.group)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + (this.level != null ? this.level.hashCode() : 0);
        hash = 17 * hash + (this.group != null ? this.group.hashCode() : 0);
        return hash;
    }
}