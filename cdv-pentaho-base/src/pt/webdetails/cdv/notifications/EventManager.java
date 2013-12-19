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
