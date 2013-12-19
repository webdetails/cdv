/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. 
 *  
 */

var wd = wd || {};


wd.cda = wd.cda || {};

wd.cda.exceptionsMixin = wd.cda.exceptionsMixin || function(myself,spec){
    
    var impl = myself.exceptions = {};
          
    impl.QueryError = function(type){
        this.type = type;
    }
    impl.QueryError.prototype.toString = function(){
        return "Error found during query: " + this.type;
    };  
    
};



wd.cda.cda = wd.cda.cda || function(spec){



    /**
     * Specific specs
     */
    var _spec = {
        name: 'Community Data Access',
        shortName: 'CDA',
        webAppPath: "/pentaho",
        isServerSide: true
    };
    
    spec = _.extend({},_spec,spec);

    var myself = {};
    
    // Apply mixins
    wd.cda.exceptionsMixin(myself);
    
    myself.log = function(msg,level){
        wd.log("["+spec.shortName+"] " + msg,level);
    }

    
    myself.doQuery = function(cdaFile, dataAccessId, params, callback){
        
        if (spec.isServerSide){


            var p, result;
            var datasource = datasourceFactory.createDatasource('cda');
            datasource.setDefinitionFile(cdaFile);
            datasource.setDataAccessId(dataAccessId);
            for (p in params) if (params.hasOwnProperty(p)) {
                datasource.setParameter(p, params[p]);
            }
            var res = String(datasource.execute());
            console.log("Query Result: " + res);
            result = JSON.parse(res);
            console.log(JSON.stringify(result));
            callback(result);

        }
        else{
            
            myself.log("Making client side query on " + cdaFile + "["+dataAccessId+"]");
        
            var json = {
                resultset:[],
                metadata:[]
            };
        
            // Make CDA call
        
            var cd = {
                path: cdaFile,
                dataAccessId: dataAccessId,
                bypassCache: true
            };
        
            for (param in params) {
                cd['param' + param] = params[param];
            }
        
            $.post(spec.webAppPath + "/content/cda/doQuery?", cd,
                function(json) {
                    callback(json);
                },'json');
           
                        
        }
        
                
    }
    
    return myself;
}

