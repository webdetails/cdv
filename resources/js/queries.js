registerHandler("GET", "/lastExecution", function(out){
    
    try {
        var result = persistenceEngine.query("select max(timestamp) as lastExecution from Alert",null);
        out.write(new java.lang.String(result.get("object")).getBytes("utf-8"));
    } catch (e) {
        print(e);
    }
});

registerHandler("GET", "/getAlertsByGroup", function(out,pathParams,requestParams){
    
    try {
        var params = new Packages.java.util.HashMap();
        params.put("group", requestParams.getStringParameter("group",""));
        var results = persistenceEngine.query("select * from Alert where group = :group",params);
        out.write(new java.lang.String(results).getBytes("utf-8"));
    } catch (e) {
        print(e);
    }
});



registerHandler("GET", "/getAlerts", function(out,pathParams,requestParams){
    
    try {
        var params = new Packages.java.util.HashMap();
        var alertType = requestParams.getStringArrayParameter("alertType",null);
        
        var viewHistory = false;
        var cdvGroup = requestParams.getStringParameter("cdvGroup",null);
        var cdvName = requestParams.getStringParameter("cdvName",null);
        var showCda = requestParams.getStringParameter("showCda","true");
        showCda = (showCda == null || showCda == "true");
        
        var isEmpty = function(s){
            return (s == null || s == "");
            
        }
        
        if( !isEmpty(cdvGroup) && !isEmpty(cdvName) ){
            viewHistory = true;
        }

        console.log("cdvGroup: " + (cdvName==null) + "; ViewHistory: " + viewHistory);
        
        // params.put("alertType", alertType);
        params.put("cdvGroup", cdvGroup);
        params.put("cdvName", cdvName);
        params.put("showCda", showCda);
        
        console.log("Starting getAlerts: " +  new Date() + "; " + alertType + "; >" + (viewHistory?("View History: " + cdvGroup + ':' + cdvName):"<"));
        
        var results;
        
        console.log("Alert filters: DISABLED FOR NOW UNTIL WE MAKE THIS WORK ");
        
        var where = "";
        if(viewHistory){
            // where = " and group = \""+cdvGroup+"\" and name = \""+cdvName+"\" ";
            console.log("Setting where");
            where = " and group = :cdvGroup and name = :cdvName ";
            
        }

        if(!showCda) {
          where += " and group <> 'cda' ";
        }
        
        // TODO - Get this into prepared statements. Currently, seems orient doesn't properly support them
        if (alertType != null){
            var alertTypes = [], i = 0;
            for( i = 0; i < alertType.length; i++ ){
                var s = '"'+alertType[i] + '"';
                alertTypes.push(s);
                console.log("AlertType " + i + ": " + s);
            }
            
            if(alertTypes.length > 0){
                where += " and level in [ " + alertTypes.join(" , ") + "]";
            }
                
        }
        var query;
        if(alertType && false){
            query = "select timestamp, group, name, summary, message, userid, level, @rid as rid" +
                " from Alert where level in [ :alertType ] " + where + " order by timestamp desc limit 100";
            
        }
        else{
            query  = "select timestamp, group, name, summary, message, userid, level, @rid as rid" +
                " from Alert where 1 = 1 " + where + " order by timestamp desc limit 100";
            
        }
        results = persistenceEngine.query(query, params );
        
        
        wd.log("Starting parse of results"); // TODO - Change this code. It's taking ages!
        
        var object = JSON.parse(results.getJSONArray("object").toString());
                
        _.map(object,function(l){
            
            // Add timestamp group
            l.timestampGroup = wd.cdv.utils.groupTimestamp(l.timestamp,2);
            return l;
  
        });

        console.log("Finishing getAlerts: " +  new Date());
        out.write(new java.lang.String(JSON.stringify(object,null,2)).getBytes("utf-8"));
        
        
    } catch (e) {
        print(e);
    }
});

registerHandler("GET", "/getLatestResults", function(out){
    
    try {
        var results = persistenceEngine.query("select test[name] as name, test[group] as group, testResult[type] as level, testResult.description as message" +
            " from TestResult where latest = true",null);
        
        var object = JSON.parse(results.getJSONArray("object").toString());

        out.write(new java.lang.String(JSON.stringify(object,null,2)).getBytes("utf-8"));
       
    } catch (e) {
        print(e);
    }
});


registerHandler("GET", "/getCdaErrors", function(out){
    
    try {        
        var results = persistenceEngine.query("select timestamp, "+
            "queryInfo[cdaSettingsId].append(\"[\").append(queryInfo[dataAccessId]).append(\"]\") as file, queryInfo[parameters] as parameters , "+
            "message, @rid from cdaEvent where eventType = 'QueryError'  order by timestamp desc limit 100",null);
        
        //console.log("Results: " + results.toJSON());
        
        var object = JSON.parse(results.getJSONArray("object").toString());
        _.map(object,function(l){
            
            // Add timestamp group
            l.timestampGroup = wd.cdv.utils.groupTimestamp(l.timestamp,2);
            
            
            // Put the files as we need them

            var params = _.flatten(l.parameters);
            var paramsArray = [];
            for(i = 0; i< params.length/2; i++){
                paramsArray.push([params[i*2],params[i*2+1]]); 
            }
    
            if(paramsArray.length > 0){
                var paramStr = _.map(paramsArray,function(m){
                    return m.join(": ")
                }).join(", ");
                l.file = l.file + " (" + paramStr + ")";
            }
            delete l.parameters;
            return l;
  
        });

        out.write(new java.lang.String(JSON.stringify(object,null,2)).getBytes("utf-8"));
        
        
    } catch (e) {
        print(e);
    }
});



registerHandler("GET", "/getCdaSlowQueries", function(out){
    
    try {
        var results = persistenceEngine.query("select timestamp, "+
            "queryInfo[cdaSettingsId].append(\"[\").append(queryInfo[dataAccessId]).append(\"]\") as file, queryInfo[parameters] as parameters , "+
            "duration, @rid from cdaEvent where eventType = 'QueryTooLong'  order by timestamp desc limit 100",null);
        
        //console.log("Results: " + results.toJSON());
        
        var object = JSON.parse(results.getJSONArray("object").toString());
        _.map(object,function(l){
            
            // Add timestamp group
            l.timestampGroup = wd.cdv.utils.groupTimestamp(l.timestamp,2);
            
            
            // Put the files as we need them

            var params = _.flatten(l.parameters);
            var paramsArray = [];
            for(i = 0; i< params.length/2; i++){
                paramsArray.push([params[i*2],params[i*2+1]]); 
            }
    
            if(paramsArray.length > 0){
                var paramStr = _.map(paramsArray,function(m){
                    return m.join(": ")
                }).join(", ");
                l.file = l.file + " (" + paramStr + ")";
            }
            delete l.parameters;
            return l;
  
        });

        out.write(new java.lang.String(JSON.stringify(object,null,2)).getBytes("utf-8"));
        
        
    } catch (e) {
        print(e);
    }
});


registerHandler("GET", "/deleteCdaEntry", function(out,pathParams,requestParams){
    

    try {        
        console.log("deleteCdaEntry method ");
        var params = new Packages.java.util.HashMap();
        var cdaEntryId = requestParams.getStringParameter("cdaEntryId","")
        params.put("cdaEntryId", cdaEntryId);
        
        // 1. Find queryInfo.@rid
        var queryInfoRid = JSON.parse(persistenceEngine.query("select queryInfo from cdaEvent where  @rid = :cdaEntryId ", params).getJSONArray("object").toString())[0].queryInfo;
        console.log("Deleting queryInfoRid: " + queryInfoRid);
        persistenceEngine.deleteRecord(queryInfoRid);
        persistenceEngine.deleteRecord(cdaEntryId);
        
        out.write(new java.lang.String("{result: true}").getBytes("utf-8"));

    /*
        var results = persistenceEngine.query("delete from ( traverse * from (select from cdaEvent where  @rid = \"" + cdaEntryId + "\" ) where  $depth <= 1 )", params);
        out.write(new java.lang.String(results).getBytes("utf-8"));
         */
       
    } catch (e) {
        print(e);
    }
});


registerHandler("GET", "/deleteCdaEntriesOfEventType", function(out,pathParams,requestParams){
    

    try {
        var params = new Packages.java.util.HashMap();
        var eventType = requestParams.getStringParameter("eventType","")
        params.put("eventType", eventType);
        
        console.log("Deleting eventTypes: " + eventType);

        // 1. Find queryInfo.@rid
        var result = persistenceEngine.command("delete from cdaEvent where eventType = :eventType ", params)
        out.write(new java.lang.String(result).getBytes("utf-8"));
        
    } catch (e) {
        print(e);
    }
});

registerHandler("GET", "/deleteAlert", function(out,pathParams,requestParams){
    

    try {        
        console.log("deleteAlert method ");
        var params = new Packages.java.util.HashMap();
        var cdaEntryId = requestParams.getStringParameter("alertId","")
        params.put("alertId", cdaEntryId);
        
        // 1. Delete it
        var result = persistenceEngine.command("delete from Alert where  @rid = :alertId ", params);
        out.write(new java.lang.String("{result: true}").getBytes("utf-8"));


    } catch (e) {
        print(e);
    }
});

registerHandler("GET", "/listSchedules", function(out,pathParams,requestParams){
    

    try {        
      out.write(new java.lang.String(JSON.stringify(scheduler.listSchedules())).getBytes("utf-8"));


    } catch (e) {
        print(e);
    }
});

registerHandler("GET", "/deleteAllAlerts", function(out,pathParams,requestParams){
    

    try {
        var alertType = requestParams.getStringArrayParameter("alertType",null) || [''],
            alertStr = _(alertType).map(function(e){
              return '"' + (e + "").replace(/"/g, '\\"') + '"';
            }).join(", "),
            whereAlert = 'where level in [' + alertStr +']';
            whereTestResult = 'where testResult[level] in [' + alertStr +']';

        // 1. Truncate it
        print(whereAlert);
        print(whereTestResult);
        persistenceEngine.command("delete from Alert " + whereAlert, null);
        var truncateResults = requestParams.getStringParameter("truncateResults","false");
        if(truncateResults == "true") {
          persistenceEngine.command("delete from TestResult " + whereTestResult, null);
        }
        out.write(new java.lang.String("{result: true}").getBytes("utf-8"));
   } catch (e) {
        print(e);
    }
});
