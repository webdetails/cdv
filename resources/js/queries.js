registerHandler("GET", "/lastExecution", function(out){
    
    try {
        persistenceEngine.initializeClass("test");
        var results = persistenceEngine.query("select max(timestamp) as lastExecution from Alert",null);
        out.write(new java.lang.String(results).getBytes("utf-8"));
    } catch (e) {
        print(e);
    }
});

registerHandler("GET", "/getAlertsByGroup", function(out,pathParams,requestParams){
    
    try {
        persistenceEngine.initializeClass("Alert");
        var params = new Packages.java.util.HashMap();
        params.put("group", requestParams.getStringParameter("group",""));
        var results = persistenceEngine.query("select * from Alert where group = :group",params);
        out.write(new java.lang.String(results).getBytes("utf-8"));
    } catch (e) {
        print(e);
    }
});
