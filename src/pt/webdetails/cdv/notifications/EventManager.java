/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdv.notifications;

import pt.webdetails.cpf.persistence.PersistenceEngine;

/**
 *
 * @author pdpi
 */
public class EventManager {

    private static EventManager instance;
    NotificationEngine ne;
    PersistenceEngine pe;

    private EventManager() {
        ne = NotificationEngine.getInstance();
        pe = PersistenceEngine.getInstance();
    }

    public static EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    public synchronized static EventManager refresh() {
        NotificationEngine.refresh();
        instance = null;
        return getInstance();
    }

    public Alert createAlert(String lvl, String group, String name, String msg) {
        return new Alert(Alert.Level.valueOf(lvl.toUpperCase()), group, name, msg);
    }

    public void publish(Alert alert) {
        pe.store(null, "Alert", alert.toJSON());
        ne.publish(alert);
    }
}
