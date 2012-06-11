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
    
    myself.toJSON = function(){
        return spec;
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
        date: new Date(),
        result: undefined,        
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
    
    myself.getTest = function(){
        return spec.test;
    };
    
    myself.getResult = function(){
        return spec.result;
    }
    
    myself.setResult = function(result){
        spec.result = result;
    }


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
        
        return translationLogMap[myself.getResult().getType()] || "debug";
        
    };
   
   
    myself.toJSON = function(){
      
        var result = {
            
            test: myself.getTest(),
            result: myself.getResult()
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
        var str = "[" + myself.getResult().getType() +"] " + result.test.name + ", Result: " + myself.getResult().getDescription();
        if(result.duration){
            str += "; Duration: [" + result.duration.type +"] " + result.duration.duration + "ms (expected: " + result.duration.expected + "ms)";
        }
    
        return str;
        
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
        shortName: 'CDV',
        isServerSide: true
    },
    _tests = {};
    
    spec = _.extend({},_spec,spec);

    var myself = {};
    
    // Apply mixins
    wd.cdv.exceptionsMixin(myself);
    wd.cdv.alertMixin(myself);
    
    
    // Get CDA
    myself.cda = wd.cda.cda({
        isServerSide:spec.isServerSide
    });


    myself.log = function(msg,level){
        wd.log("["+spec.shortName+"] " + msg,level || "debug");
    }
    
    
    
    // Main function to make the tests
    
    myself.runTest = function(test, opts){
        
        // Opts is an object with specific options to this test. 
        // Supported options:
        // * callback(result): Function called when the test is done with the result
        
       
        myself.log("Making test [" + test.group + "].["+test.name+"] ","debug");
    

        return myself.executeQuery(test, opts, myself.runTestCallback);
        
        
    };


    // Process the test restults
    
    myself.runTestCallback = function(test, opts, result){
         
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
       
        try{
            var res = myself.performValidation(test.test,rs);
            testResult.setResult(res);
        }
        catch(e){
            myself.log("Found error while doing validation" ,"error")
        }
       
        myself.log( testResult.toString(), testResult.getLogType());
        
        // Do we have a user callback?
        if(typeof opts.callback === 'function'){
            opts.callback(testResult);
        } 
    }
    

    // Given a test, execute the query and returns the result and the length
    
    myself.executeQuery = function(test, opts, callback){
        
        var startTime = new Date().getTime();
        
        
        // Make the queries asynchronosly
        var count = 0,
        total= test.validation.length,
        rs = [];
        
        var wrapUpCalls = function(){
            
            var duration = (new Date().getTime()) - startTime;
            
            myself.log("Finished execution. Duration: "+ duration + "ms Result: " + rs);
            callback(test, opts, {
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
        
        // Call test!
        wd.warn("TODO: Call preexisting validation here");
        wd.warn("TODO: Pass validation arguments");
        
        wd.log("Validation function: " + validation.validationFunction);


        var result = validation.validationFunction.call(myself,rs,[]);
        return myself.parseAlert(result);
        
        
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



