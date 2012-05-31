/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. 
 *  
 */


var cdvFile = {
    type: "query",
    name: "Test 1",
    group: "CDV Sample Tests",
    validation: [ 
    {
        cdaFile: "/plugin-samples/cda/cdafiles/sql-jndi.cda&dataAccessId=1", 
        dataAccessId: "1" , 
        parameters: {}
    },
    ],
    tests:[ 
    {
        validationType: "custom",
        validationFunction:  function(rs, conf) {
            var exists = rs.map(function(r){
                return r.length > 0
            }).reduce(function(prev, curr){
                return conf.testAll ? (curr && prev) : (curr || prev);
            });
            return exists ? "ERROR" : "OK";
        }
    }],
    executionTimeValidation: {
        expected: 500,
        warnPercentage: 0.30,
        errorPercentage: 0.70,
        errorOnLow: true
    },

    cron: "0 2 * * ? *" 
};




var cdv;

$(function(){
    

    setTimeout(function(){

        cdv = wd.cdv.cdv();
        
        cdv.makeTest(cdvFile);
        
    
    },100)
    

})

