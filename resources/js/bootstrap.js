lib("lib/json2.js");
lib("lib/underscore.js");
lib("lib/timers.js");
lib("lib/later.js");
lib("lib/wd.js");
lib("scheduler.js");
lib("cdv.js");
lib("cda.js");

// Wrap up console obj so that logging works
var console = {
    log: function(m){
        print(m)
        }
    
}


cdv = wd.cdv.cdv();
loadTests();

var timerUp = true;

//scheduler.scheduleTask(function(){print("Every 7!")},"0/7 * * * * *");
//scheduler.scheduleTask(function(){print("Every 5!")},"1-59/5 * * * * *");

registerHandler("GET", "/restartTimer", function(out){
    this.setOutputType(this.MIME_TEXT);
    timerUp = true;
    scheduler.restart();
    out.write(new java.lang.String("Timer turned on").getBytes("utf-8"));
});

registerHandler("GET", "/stopTimer", function(out){
    try { 
        this.setOutputType(this.MIME_TEXT);
        timerUp = false;
        print("Turning timer off");
        scheduler.pause();
        out.write(new java.lang.String("Timer turned off").getBytes("utf-8"));
        print("Timer turned off");

    } catch(e) {
        print(e);
    }
});

registerHandler("GET", "/listTests", function(out){
    try { 
        this.setOutputType(this.MIME_JSON);
        out.write(new java.lang.String(JSON.stringify(cdv.listTests())).getBytes("utf-8"));
    } catch(e) {
        print(e);
    }
});

registerHandler("GET", "/listTestsFlatten", function(out){
    try { 
        this.setOutputType(this.MIME_JSON);
        var str = JSON.stringify(cdv.listTestsFlatten());
        out.write(new java.lang.String(str).getBytes("utf-8"));
    } catch(e) {
        print(e);
    }
});


registerHandler("GET", "/runTests", function(out){
    
    try { 
        this.setOutputType(this.MIME_JSON);
        var groups = cdv.listTests();
        var g, t, group, test, results = [];
    
        var callback = function(r){
            results.push(r.toJSON());
        }
    
        for (g in groups) if (groups.hasOwnProperty(g)) {
            var group = groups[g];
            for (t in group) if (group.hasOwnProperty(t)) {
                var test = group[t];
                cdv.runTest(test,{
                    callback: callback
                });
            }
        }
        out.write(new java.lang.String(JSON.stringify(results)).getBytes("utf-8"));
    } catch (e) {
        print(e);
    }
});

registerHandler("GET", "/testPersistence", function(out){
    
  try {
    persistenceEngine.initializeClass("test");
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



