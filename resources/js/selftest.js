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
  var alrt = eventManager.getAlert("OK", "test", "Este mail vem do CDV. Devo chegar um bocado tarde.");
  eventManager.publish(alrt);
  out.write(new java.lang.String("Done").getBytes("utf-8"));
});

