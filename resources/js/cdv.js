/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. 
 *  
 */

var wd = wd || {};
wd.cdv = wd.cdv||{};

wd.cdv.alert = wd.cdv.alert || function(spec){


    /**
     * Specific specs
     */
    var _spec = {
        type: "overrideme",
        description: "Default alert description",
        level: 0
    };
    
    spec = _.extend({},_spec,spec);


    var myself = {};
    
    myself.getType = function(){
        return spec.type;
    }
    
    myself.getDescription = function(){
        return spec.description;
    }
    
    myself.getLevel = function(){
        return spec.level;
    }
    
    myself.toString = function(){
        return "["+ spec.type + "] " + spec.description;
    }
    
    return myself;
}


wd.cdv.testResult = wd.cdv.testResult || function(spec){


    /**
     * Specific specs
     */
    var _spec = {
        type: "testResult",
        description: "Test result",
        test: undefined,
        validationResults: [],
        date: new Date(),
        duration: -1, 
        durationAlert: undefined, 
        expectedDuration: -1
    };
    
    spec = _.extend({},_spec,spec);


    var translationLogMap = {
        OK: "debug", 
        WARN: "warn",
        ERROR: "error",
        CRITICAL: "error"
    };
    
    var myself = {};

    myself.getType = function(){
        return spec.type;
    }
    
    myself.getDescription = function(){
        return spec.description;
    }
    
    myself.getValidationResults = function(){
        return spec.validationResults;
    };
    
    myself.addValidationResult = function(validationResult){
        spec.validationResults.push(validationResult);
    };
    
    myself.getTest = function(){
        return spec.test;
    };
    
    
    myself.getTestResult = function(){
        // Test result will be the bigger alert level of the ones we have
        var result =  myself.getValidationResults().slice(0);
        result = result.sort(function(a,b){
            return b.getAlert().getLevel()-a.getAlert().getLevel()
        })[0];
        return result.getAlert();
    };
    
    
    myself.getTestResultDescription = function(){
    
    
        var resultMap = {},keys=[];
        var count = myself.getValidationResults().length;
        
        _.each(myself.getValidationResults().sort(function(a,b){
            return b.getAlert().getLevel() - a.getAlert().getLevel()
        }),function(r){
             
            var alert = r.getAlert();
            if(!resultMap[alert.getType()]){
                resultMap[alert.getType()] = 1;
                
                // Manually building the keys array to guarantee correct order
                keys.push(alert.getType());
            }
            else{
                resultMap[alert.getType()]++;
            }
           
        });
        

        if(keys.length == 1 && keys[0]==="OK"){
            return "All " + resultMap.OK + " test(s) passed successfully";
 
        }       
        
        var lowercaseFirstLetter = function(str){
            return str.charAt(0).toUpperCase() + str.slice(1);
        }
            
        var r = _.map(keys, function(type){
            var count = resultMap[type];
            return count + " tests " + lowercaseFirstLetter(myself.alerts[type].getDescription());
        });
        
        return r.join(", ");
            
        
        
    };


    myself.getDuration = function(){
        return spec.duration
    };
    
    myself.getExpectedDuration = function(){
        return spec.expectedDuration;
    };

    myself.getDurationAlert = function(){
        return spec.durationAlert
    };
    
    
    myself.getLogType = function(){
        
        return translationLogMap[myself.getTestResult().getType()] || "debug";
        
    };
   
    myself.toJSON = function(){
      
      var result = {
        tests: {
          name: myself.getTest().name,
          type: myself.getTestResult().getType(),
          description: myself.getDescription()
        }
      };
      if (myself.getExpectedDuration() > 0) {
        result.duration = {
          type: myself.getDurationAlert().getType(),
          duration: myself.getDuration(),
          expected: myself.getExpectedDuration()
        };
      }
      return result;
    }; 
    myself.toString = function(){
        
        var result = myself.toJSON();
        var str = "[" + result.tests.type +"] " + result.tests.name + ", Result: " + result.tests.description;
        if(result.duration){
            str += "; Duration: [" + result.duration.type +"] " + result.duration.duration + "ms (expected: " + result.duration.expected + "ms)";
        }
    
        return str;
        
    }
    

    return myself;
}


wd.cdv.validationResult = wd.cdv.validationResult || function(spec){


    /**
     * Specific specs
     */
    var _spec = {
        name: "overrideMe",
        type: "overrideMe",
        description: "",
        alert: undefined
    };
    
    spec = _.extend({},_spec,spec);

    var myself = {};

    myself.getType = function(){
        return spec.type;
    }
    
    myself.getName = function(){
        return spec.name;
    }
    
    myself.getDescription = function(){
        return spec.description;
    }
    
    myself.getAlert = function(){
        return spec.alert;
    }
    
    myself.setAlert = function(alert){
        spec.alert = alert;
    }
    
    return myself;

}


wd.cdv.exceptionsMixin = wd.cdv.exceptionsMixin || function(myself,spec){
    
    var impl = myself.exceptions = {};
          
    impl.UnknowAlertTypeError = function(type){
        this.type = type;
    }
    impl.UnknowAlertTypeError.prototype.toString = function(){
        return "Trying to parse an unexisting alert type: " + this.type;
    };  
    
};


wd.cdv.alertMixin = wd.cdv.alertMixin || function(myself,spec){

    var alerts = {
        NA:  wd.cdv.alert({
            type: "NA", 
            description: "Not Applicable",
            level: -1
        }),
        OK:  wd.cdv.alert({
            type: "OK", 
            description: "Passed successfully",
            level: 10
        }),
        WARN:  wd.cdv.alert({
            type: "WARN", 
            description: "Failed with WARN",
            level: 20
        }),
        ERROR:  wd.cdv.alert({
            type: "ERROR", 
            description: "Failed with ERROR",
            level: 30
        }),
        CRITICAL:  wd.cdv.alert({
            type: "CRITICAL", 
            description: "Failed with CRITICAL",
            level: 40
        })
    }

    
    // Bind this
    myself.alerts = alerts;
    

    
    
    // Parse function
    myself.parseAlert = function(alertType){
        
        if (!alerts[alertType]){
            throw new myself.exceptions.UnknowAlertTypeError(alertType);
        }
        
        return alerts[alertType];
        
    }
    
    wd.debug("Added alert mixin");
    
};


wd.cdv.cdv = wd.cdv.cdv || function(spec){


    /**
     * Specific specs
     */
    var _spec = {
        name: 'Community Data Validation',
        shortName: 'CDV'
    },
    _tests = {};
    
    spec = _.extend({},_spec,spec);

    var myself = {};
    
    // Apply mixins
    wd.cdv.exceptionsMixin(myself);
    wd.cdv.alertMixin(myself);
    
    
    // Get CDA
    myself.cda = wd.cda.cda();


    myself.log = function(msg,level){
        wd.log("["+spec.shortName+"] " + msg,level || "debug");
    }
    
    
    
    // Main function to make the tests
    
    myself.runTest = function(test){
        
        // 1. Make CDA calls
        // 2. Store time
        // 3. Compose the results into a single vector of arays
        // 4. Run tests
        // 5. Evaluate
       
       
        myself.log("Making test [" + test.group + "].["+test.name+"] ","debug");
    

        return myself.executeQuery(test, myself.runTestCallback);
        
        
    };


    // Process the test restults
    
    myself.runTestCallback = function(test,result){
         
        var duration = result.duration;
        var rs = result.resultset;

        var resultSpec = {
            test:test
        };
        
        // 1. Parse duration
        // 2. make test
        // 3. return
        
        var parseDuration = function(test,duration){

            if (!test.executionTimeValidation){
                console.log("No info");
                return "NA";
            }
    
            var t = test.executionTimeValidation;
            
            resultSpec.duration = duration;
            resultSpec.expectedDuration = t.expected;
    
            var isDurationAgainstPercentageValid = function(perc){
        
                var v = duration/t.expected - 1;
                return v < 0 && !t.errorOnLow ? true:Math.abs(v)<=perc;
            }
    
    
            // check if we're within the values
            if(!isDurationAgainstPercentageValid(t.errorPercentage)){
                return "ERROR";
            }
            if(!isDurationAgainstPercentageValid(t.warnPercentage)){
                return "WARN";
            }


            return "OK";
        }


        // Add duration information
        var durationResult =  parseDuration(test,duration);
        resultSpec.durationAlert = myself.parseAlert(durationResult);
        
        
        // Build result object and process validations
        var testResult = wd.cdv.testResult(resultSpec);

        
        //
        // Make the actual test. 
        // this can either be a custom validation or a call to a preset validation
        //
        _.each(test.tests,function(validation){
            try{
                var res = myself.performValidation(validation,rs);
                testResult.addValidationResult(res);
            }
            catch(e){
                myself.log("Found error while doing validation" ,"error")
            }

        })
        
        
        myself.log( testResult.toString(), testResult.getLogType());
        
        var fn;
        if(fn = spec.userCallback) fn(testResult);
    }
    

    // Given a test, execute the query and returns the result and the length
    
    myself.executeQuery = function(test, callback){
        
        var startTime = new Date().getTime();
        
        
        // Make the queries asynchronosly
        var count = 0,
        total= test.validation.length,
        rs = [];
        
        var wrapUpCalls = function(){
            
            var duration = (new Date().getTime()) - startTime;
            
            // myself.log("Finished execution. Duration: "+ duration + "ms Result: " + rs);
            callback(test, {
                duration: duration, 
                resultset: rs
            });
        }
        
        _.map(test.validation, function(cdaInfo, idx){
            
            
            // Define callback function
            myself.log("Making cda test " + idx, "debug");
            
            
            var validationCallback = function(json){

                count++;
                rs[idx] = json;
                
                if(count===total){
                    wrapUpCalls();
                }
                
            };
            
            
            // Make CDA call
            return myself.cda.doQuery(cdaInfo.cdaFile,cdaInfo.dataAccessId,cdaInfo.parameters,validationCallback);
            
            
        })
        
    }
    
    
    myself.performValidation = function(validation, rs){
        var validationResult = wd.cdv.validationResult({
            name: validation.validationName, 
            type: validation.validationType
        })
        
        
        // Call test!
        wd.warn("TODO: Call preexisting validation here");
        wd.warn("TODO: Pass validation arguments");
        
        var result = validation.validationFunction.call(myself,rs,[]);
        
        validationResult.setAlert(myself.parseAlert("OK"));
        return validationResult;
        
    }



    myself.setUserCallback = function(fn) {
      spec.userCallback = fn;
    };

    myself.registerTest = function(test) {
        if (!_tests[test.group]) _tests[test.group] = {};
        _tests[test.group][test.name] = test;
    };

    myself.listTests = function(group){
      if(group) {
        return _tests[group];
      } else {
        return _tests;
      }
    };

    myself.getTest = function(group, name) {
      try {
        return _tests[group][name];
      } catch(e) {
        return undefined;
      }
    }
    return myself;


}



