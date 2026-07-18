/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/
registerHandler("GET", "/testPersistence", function(out){
    
    try {
        var results = persistenceEngine.query("select * from test",null);
        out.write(new java.lang.String(results).getBytes("utf-8"));
    } catch (e) {
        print(e);
    }
});

registerHandler("GET", "/testNotifications", function(out){
  var n = Packages.pt.webdetails.cdv.notifications;
  var alrt = eventManager.createAlert("OK", "group test", "test", "Email", "Este mail vem do CDV. Devo chegar um bocado tarde.");
  eventManager.publish(alrt);
  out.write(new java.lang.String("Done").getBytes("utf-8"));
});

