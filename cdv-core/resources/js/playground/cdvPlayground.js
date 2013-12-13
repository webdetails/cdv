/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. 
 *  
 */


$.ajaxSetup({
    async: false
});

var cdv;

$(function(){
    

    setTimeout(function(){

        cdv = wd.cdv.cdv({
            isServerSide: false
        });
        
        
        // Register tests
        
        cdv.registerTest({
            id: 1,
            type: "query",
            name: "Test 1",
            group: "CDV Sample Tests",
            path: "/cdv/tests/test.cdv",
            createdBy: "Pedro",
            createdAt: 1339430893246,
            queries: [ 
            {
                cdaFile: "/plugin-samples/cda/cdafiles/sql-jndi.cda", 
                dataAccessId: "1" , 
                parameters: {}
            },
            {
                cdaFile: "/plugin-samples/cda/cdafiles/sql-jndi.cda", 
                dataAccessId: "1" , 
                parameters: {
                    status:"Cancelled"
                }
            }
            ],
            validations: 
            [
            {
                validationName: "Test Existence",
                validationType: "custom",
                validationFunction:  function(rs, conf) {
            
                
                    var _conf = {
                        testAll: true
                    };
                    conf = _.extend({},_conf,conf);
                    var exists = !!conf.testAll;

                    exists = _.reduce(_.map(rs,function(r){
                        return r.resultset.length > 0
                    }),function(prev, curr, exists){
                        return conf.testAll ? (curr && prev) : (curr || prev);
                    });

                    return exists ? "OK" : "ERROR";
        
                }
            },
            {
                validationName: "Test Existence 2",
                validationType: "custom",
                validationFunction:  function(rs, conf) {
            
                
                    var _conf = {
                        testAll: true
                    };
                    conf = _.extend({},_conf,conf);
                    var exists = !!conf.testAll;

                    exists = _.reduce(_.map(rs,function(r){
                        return r.resultset.length > 0
                    }),function(prev, curr, exists){
                        return conf.testAll ? (curr && prev) : (curr || prev);
                    });

                    return exists ? "ERROR" : "OK";
        
                }
            }
            ],
            executionTimeValidation: {
                expected: 100,
                warnPercentage: 0.30,
                errorPercentage: 0.70,
                errorOnLow: true
            },

            cron: "0 2 * * ? *" 
        });



        cdv.registerTest({
            id: 2,
            type: "query",
            name: "Test 2",
            group: "CDV Sample Tests",
            createdBy: "Pedro",
            path: "/cdv/tests/test2.cdv",
            createdAt: 1339430893246,
            validation: [ 
            {
                cdaFile: "/plugin-samples/cda/cdafiles/mondrian-jndi.cda", 
                dataAccessId: "1" , 
                parameters: {}
            },
            {
                cdaFile: "/plugin-samples/cda/cdafiles/mondrian-jndi.cda", 
                dataAccessId: "1" , 
                parameters: {
                    status:"Cancelled"
                }
            }
            ],
            validations: 
            [{
                validationName: "Test Existence",
                validationType: "custom",
                validationFunction:  function(rs, conf) {
            
                    var _conf = {
                        testAll: true
                    };
                    conf = _.extend({},_conf,conf);
                    var exists = !!conf.testAll;

                    exists = _.reduce(_.map(rs,function(r){
                        return r.resultset.length > 0
                    }),function(prev, curr, exists){
                        return conf.testAll ? (curr && prev) : (curr || prev);
                    });
                    return exists ? "ERROR" : "OK";
        
                }
            }],
            executionTimeValidation: {
                expected: 100,
                warnPercentage: 0.30,
                errorPercentage: 0.70,
                errorOnLow: true
            },

            cron: "0 2 * * ? *" 
        });


        
        // Test 1: List
        //$(".results").text(JSON.stringify( cdv.listTestsFlatten("CDV Sample Tests")  ));
        
        // Call again to make sure we're not overriding something
        //$(".results").text(JSON.stringify( cdv.listTestsFlatten("CDV Sample Tests")  ));
        
        
        
        // Test 2: Execute a test
        
       
        /*
        var callback = function(result){
            $(".results").text(JSON.stringify(result));
        }
        
        cdv.runTest(cdv.getTest("CDV Sample Tests","Test 1") , {
            callback: callback
        });
         */
        
        
        // Test 3: List tests
        
        
        var firstTest = _.map(cdv.listTestsFlatten().sort(function(a,b){
            return (a.group + a.name) >=  (b.group + b.name)
        }), function(t){
            
            console.log("Test Group " + t.group + ": " + t.name);
            return cdv.getTest( t.group, t.name);
            
        })[0];
        
        /*
        cdv.runTest(firstTest,{
            callback: function(){
                alert("OK")
            }
        });
        */
        
        cdv.executeQuery(firstTest,null, function(test,opts,queryResult){
            debugger;
            alert("OK");
            
        });
        
        
        
        
            
    },100)
    

})

