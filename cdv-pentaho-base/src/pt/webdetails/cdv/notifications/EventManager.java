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


package pt.webdetails.cdv.notifications;

import pt.webdetails.cdv.results.TestResult;
import pt.webdetails.cpf.persistence.PersistenceEngine;

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

    public Alert createAlert(String lvl, String group, String name, String subject, String msg) {
      return new Alert(Alert.Level.valueOf(lvl.toUpperCase()), group, name, msg, subject);
    }

    public void publish(Alert alert) {
        pe.store(null, "Alert", alert.toJSON());
        ne.publish(alert);
    }
    
    public TestResult createTestResult(String json) {
        return new TestResult(json);
    }
}
