/*
 * include base libraries
 */
lib("lib/json2.js");
lib("lib/underscore.js");
lib("lib/timers.js");
lib("lib/later.js");
lib("lib/wd.js");

/*
 * load core code
 */
lib("scheduler.js");
lib("cdv.js");
lib("cda.js");



// Wrap up console obj so that logging works
var console = {
    log: function(m){
        print(m)
    }
    
}

/*
 * boot up CDV
 */
cdv = wd.cdv.cdv();
/*
 * load core test types
 */
lib("tests/customTest.js");

/*
 * Load tests
 */
loadTests();


/*
 * set up request handlers
 */
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
        var result = r.toJSON();
        results.push(result);
        var tr =  eventManager.createTestResult(JSON.stringify(result)), doc;
        if(tr) {
          doc = persistenceEngine.createDocument(tr.getPersistenceClass(), tr.toJSON().toString());
          persistenceEngine.store(null, tr.getPersistenceClass(), null, doc);
        }
      }
  
      for (g in groups) if (groups.hasOwnProperty(g)) {
          var group = groups[g];
          for (t in group) if (group.hasOwnProperty(t)) {
              var test = group[t];
              cdv.runTest(test, {
                  callback: callback
              });
          }
      }
      out.write(new java.lang.String(JSON.stringify(results)).getBytes("utf-8"));
    } catch (e) {
        print(e);
    }
});


registerHandler("GET", "/runTest", function(out,pathParams,requestParams){
    var myself = this;
    try {
      callWithDefaultSession(function(){ 
        myself.setOutputType(myself.MIME_JSON);
        var str = cdv.runTestById({group: requestParams.getStringParameter("group",""),name: requestParams.getStringParameter("name","")});
        str = JSON.stringify(str.toJSON());
        out.write(new java.lang.String(str).getBytes("utf-8"));
      });
    } catch(e) {
        print(e);
    }
});

registerHandler("GET", "/refreshNotifications", function(out){
  eventManager = eventManager.refresh();
  out.write(new java.lang.String('{"success": true}').getBytes("utf-8"));
});

registerHandler("GET", "/refreshTests", function(out){
  scheduler.reset();
  loadTests();
});

registerHandler("GET",'/foo',function(out){
  var klass = Packages.pt.webdetails.cpf.persistence.PersistenceEngine;

  var methods = persistenceEngine.getClass().getMethods()
  for(var m = 0; m < methods.length; m++) {
    print(methods[m].toString());
  }
});
lib("selftest.js");
lib("queries.js");

