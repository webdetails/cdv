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
    
        spec = _.extend(_spec,spec);


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
            return {
                type: spec.type, 
                description: spec.description
            };
        }
    
        myself.clone = function() {
            return wd.cdv.alert(spec);
        };

        myself.setDescription = function(description) {
            spec.description = description;
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
            validationResults:[],
            duration: -1, 
            durationAlert: undefined, 
            expectedDuration: -1,
            timestamp: new Date()
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
    
        myself.getResult = function(){
            return spec.result;
        }
    
        myself.setResult = function(result){
            spec.result = result;
        }

        myself.getTimestamp = function() {
            return spec.timestamp;
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
        
            return translationLogMap[myself.getTestResult().getType()] || "debug";
        
        };
        
        myself.getTestResult = function(){
            // Test result will be the bigger alert level of the ones we have
            var result =  myself.getValidationResults().slice(0);
            result = result.sort(function(a,b){
                return b.getAlert().getLevel()-a.getAlert().getLevel()
            })[0];
            var res = result.getAlert();
            return res;
        };
    
    
        myself.getTestResultDescription = function(){
            
            var testName = myself.getTest().group + ": " + myself.getTest().name + " - ";
            
            var resultMap = {},keys=[], alertMap=[];
            var count = myself.getValidationResults().length;
        
            _.each(myself.getValidationResults().sort(function(a,b){
                return a.getAlert().getLevel() - b.getAlert().getLevel()
            }),function(r){
             
                var alert = r.getAlert();
                if(!resultMap[alert.getType()]){
                    
                    resultMap[alert.getType()] = 1;
                    alertMap[alert.getType()] = alert;
                
                    // Manually building the keys array to guarantee correct order
                    keys.push(alert.getType());
                }
                else{
                    resultMap[alert.getType()]++;
                }
           
            });
        

            if(keys.length == 1 && keys[0]==="OK"){
                return testName + "All " + resultMap.OK + " validation(s) passed successfully";
 
            }       
        
            var lowercaseFirstLetter = function(str){
                return str.charAt(0).toUpperCase() + str.slice(1);
            }
            
            var isFirst = true;
            var r = _.map(keys, function(type){
                
                var alertCount = resultMap[type],
                str = "";
                if(isFirst && type !== "OK" && alertCount == 1){
                    str = " ["+alertMap[type].getDescription() + "] ";
                }
                isFirst = false;
                
                
                return alertCount + " " + type + (alertCount>1?"s":"") + str;
            });
        
            return testName +  r.join(", ");
            
        
        };
   
   
        myself.toJSON = function(){
            var result = myself.getTestResult().toJSON();
            var output = {
                timestamp: myself.getTimestamp().getTime(),
                test: myself.getTest(),
                validationResults: myself.getValidationResults(),
                testResult: {
                    type: result.type,
                    cause: result,
                    description: myself.getTestResultDescription()
                }
            };
            output.validationResults = _.map(output.validationResults,function(e){
                return e.toJSON();
            });
            if (myself.getExpectedDuration() > 0) {
                output.duration = {
                    type: myself.getDurationAlert().getType(),
                    duration: myself.getDuration(),
                    expected: myself.getExpectedDuration()
                };
            }
            return output;
        }; 
    
    
        myself.toString = function(){
        
            var result = myself.toJSON();

            var str = "[" + myself.getTestResult().getType() +"] " + result.test.name + ", Result: " + myself.getTestResultDescription();
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
    
        myself.toJSON = function() {
            return eval("(" + JSON.stringify(spec) + ")");
        }

        myself.toString = function(){
        
            var str = " Validation '" + myself.getName() + "' : [" + myself.getAlert().getType() +"] " + myself.getAlert().getDescription();
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
            wd.info("parsing " + alertType);
            if (!alerts[alertType]){
                wd.error("couldn't find an alert for " + alertType);
                throw new myself.exceptions.UnknowAlertTypeError(alertType);
            }
        
            return alerts[alertType].clone();
        
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
        _tests = {},
    
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
    

            return myself.executeQuery(test, opts || {} , myself.runTestCallback);
        
        
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
            // Make the actual validation. 
            // this can either be a custom validation or a call to a preset validation
            //
       
            _.each(test.validations,function(validation){
                try{
                    var res = myself.performValidation(validation,rs);
                    testResult.addValidationResult(res);

                }
                catch(e){
                    myself.log("Found error while doing validation" ,"error")
                }

            })

            myself.log( testResult.toString(), testResult.getLogType());
        
            if (spec.isServerSide) {
              var resJSON = testResult.toJSON();
              var fullMessage = "";
              
              _.map(testResult.getValidationResults(), function (validationResult, i) {
                  fullMessage += " * " + validationResult.toString() + "         ";
              });
              
              /* Emit an alert for the test result*/ 
              var alrt = eventManager.createAlert(resJSON.testResult.type, resJSON.test.group, resJSON.test.name, resJSON.testResult.description, fullMessage);
              eventManager.publish(alrt);
              /* Emit an alert for the test's duration */
              var description = "Query execution time: {{duration}}ms (Expected {{expected}}ms)",
                  descriptionVals = {
                    duration: resJSON.duration.duration,
                    expected: resJSON.duration.expected
                  },
                  title = "Unexpected Query Duration - {{group}}: {{name}}",
                  titleVals = {
                    level: resJSON.duration.type,
                    name: resJSON.test.name,
                    group: resJSON.test.group,
                  };
              var durationAlert = eventManager.createAlert(resJSON.duration.type, resJSON.test.group, resJSON.test.name, Mustache.render(title,titleVals), Mustache.render(description,descriptionVals));
              eventManager.publish(durationAlert);

              var tr =  eventManager.createTestResult(JSON.stringify(resJSON)), doc;
              if(tr) {
                  doc = persistenceEngine.createDocument(tr.getPersistenceClass(), tr.toJSON().toString());
                  /* Set the */
                  var params = new Packages.java.util.HashMap();
                  var group = resJSON.test.group.replace(/"/,"\\\"");
                  var name = resJSON.test.name.replace(/"/,"\\\"");
                  params.put("name", resJSON.test.name);
                  params.put("group", resJSON.test.group);
                  var stmt = "update TestResult set latest = false where test[group] = \"" + group + "\" and test[name] = \"" + name + "\" and latest = true";
                  print(stmt);
                  persistenceEngine.command(stmt,params);
                  doc.field("latest", true);
                  persistenceEngine.store(null, tr.getPersistenceClass(), null, doc);
              }
            } 
            // Do we have a user callback?
            if( opts && typeof opts.callback === 'function'){
                opts.callback(testResult);
            } 
        }
    

        // Given a test, execute the query and returns the result and the length
    
        myself.executeQuery = function(test, opts, callback){
        
            var startTime = new Date().getTime();
        
        
            // Make the queries asynchronosly
            var count = 0,
            total= test.queries.length,
            rs = [];
        
            var wrapUpCalls = function(){
            
                var duration = (new Date().getTime()) - startTime;
            
                myself.log("Finished execution. Duration: "+ duration + "ms Result: " + rs);
                callback(test, opts, {
                    duration: duration, 
                    resultset: rs
                });
            }
        
            _.map(test.queries, function(cdaInfo, idx){
            
            
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
            var validationFunction = wd.cdv.validators.getValidator(validation.validationType);
            return validationFunction.call(myself,validation,rs);
        };

        myself.registerTest = function(test) {
            if (!_tests[test.group]) _tests[test.group] = {};
            _tests[test.group][test.name] = test;
            
            if (spec.isServerSide){
              if(getPluginSetting("scheduler/active") == "true") {
                scheduler.scheduleTask(function(){
                  var result = myself.runTestById({
                      group: test.group, 
                      name: test.name
                  }).toJSON();
                }, test.cron, test.group, test.name);
              } else {
                wd.log("Test scheduling skipped");
              }
            }
        };

        myself.resetTests = function() {
            _tests = {};
        };

        myself.listTests = function(group){
            if(group) {
                return _.pick(_tests,[group]);
            } else {
                return _tests;
            }
        };


        myself.listTestsFlatten = function(group){
        
            wd.log("Entering listTestFlatten")
        
            function flatten(json){
                var nj = {},
                iter = 0,
                walk = function(j){
                    var jp;
                    for(var prop in j){
                        if (!j.hasOwnProperty(prop)){
                            continue;
                        }
                        jp = j[prop];
                        if(_.isObject(jp) && iter <= 20){
                            iter++;
                            walk(jp);
                        }else{
                            nj[prop] = jp;
                        }
                    }
                };
            
                walk(json);
                return nj;
            }

            var preprocessObj = function(o){
            
                // validations will be put here
                var a = o.queries;
                o.queries = _.map(a,function(i){
                    
                    var output = i.cdaFile + "[" + i.dataAccessId+ "]";
                    if(i.parameters){
                        output += " (" +_.map(i.parameters,function(v,k){
                            return k+": "+v
                        }).join(", ")+")";
                    }
                    
                    return output;
                }).join("; ").replace(/ \(\)/g,"");
                
                
                // Validations will also become a csv of the validations
                var b = o.validations;
                o.validations = {
                    validationName: _.pluck(b, "validationName").join(", "),
                    validationType: _.pluck(b, "validationType").join(", ")
                };
            
                return flatten(o);
            }
        
        
            var t = myself.listTests(group);
        
            // Get the keys, deep copying initial obj
            var arr = JSON.parse(JSON.stringify(_.flatten(_.map(t,_.values)))); 
            wd.log("Debug: " + arr);
            arr = arr.sort(function(a,b){return a.group > b.group? 1 : a.group < b.group ? -1 : 0;});
            return _.map(arr,preprocessObj);
        
        
        };


        myself.getTest = function(group, name) {
            try {
                return _tests[group][name];
            } catch(e) {
                return undefined;
            }
        }
    
    
        myself.getTestById = function(id){
            var callback;
            /* We need to adjust the filter callback
             * to the type of key that we were given
             */
            if (typeof id == "object") {
                callback = function(o) {
                    return o.name == id.name && o.group == id.group;
                }
            } else {
                callback = function(t){
                    return t.id == id
                }
            }
        
            return _.chain(_tests)
            .map(_.values)
            .flatten()
            .find(callback).value();
        
        }
    
    
        myself.runTestById = function(id){
            
            var result;
            var callback = function(o){
                result = o
            }
            
            myself.runTest(myself.getTestById(id), {
                callback: callback
            })
            
            
            return result;
            
        }
    
    
        return myself;


    }


    wd.cdv.utils = wd.cdv.utils || {
    
        groupTimestamp: function (d, _level, opt) {

            var defaults = {
              longFormat: true
            }
            opt = _.extend({},defaults,opt);
            var level = _level || 1;

            // Level: 1 - group granularity
            // Level: 2 - smaller granularity

            var now = new Date();
    
            var str;
            var how_many = 0;
            var what = '';

            if(level == 1){
    
                var diff = (now.getTime() - d)/1000;
                if (diff >= 86400) {
                    how_many = Math.floor(diff / 86400);
                    what = opt.longFormat ? ' day' : 'd';
                } else if (diff >= 3600) {
                    how_many = Math.floor(diff / 3600);
                    what = opt.longFormat ? ' hour' : 'h';
                } else if (diff >= 60) {
                    how_many = Math.floor(diff / 60);
                    what = opt.longFormat ? ' minute' : 'm';
                } else {
                    return "Just now";
                }

                if (opt.longFormat && how_many > 1) what = what + 's'; // Plural
                if (how_many != '') var str = how_many + what + ' ago';

            }
            else{
    
                var midnightUnixTime = new Date(now.getYear(),now.getMonth(),now.getDate()).getTime();
                var yesterdayUnixTime = midnightUnixTime - 1000*3600*24;
                var thisWeekUnixTime = midnightUnixTime - 1000*3600*24 * (now.getDay());
                var thisMonthUnixTime = midnightUnixTime - 1000*3600*24 * (now.getDate()-1);
    
                if (d > midnightUnixTime ){
                    str = 'Today';
                }
                else if (d > yesterdayUnixTime){
                    str = 'Yesterday';
                }
                else if (d > thisWeekUnixTime){
                    str = 'This week';
                }
                else if (d > thisMonthUnixTime){
                    str = 'This month';
                }
                else{
                    str = 'Old';
                }
               
                return str;
    
            }
    
            return str;


        }
    
    };


wd.cdv.validators = (function (){
  var _validators = {},
      myself = {};

  myself.registerValidator = function(type,fn) {
      _validators[type] = fn;
  };

  myself.getValidator = function(type) {
      return _validators[type];
  }
  return myself;
}());

