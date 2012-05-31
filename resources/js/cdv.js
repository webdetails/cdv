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
        description: "Default alert description"
    };
    
    spec = _.extend({},_spec,spec);


    var myself = {};

    myself.getType = function(){
        return spec.type;
    }
    
    myself.getDescription = function(){
        return spec.description;
    }
    
    myself.toString = function(){
        return "["+ spec.type + "] " + spec.description;
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
        OK:  wd.cdv.alert({
            type: "OK", 
            description: "Test passed successfully"
        }),
        WARN:  wd.cdv.alert({
            type: "WARN", 
            description: "Test failed with WARN"
        }),
        ERROR:  wd.cdv.alert({
            type: "ERROR", 
            description: "Test failed with ERROR"
        }),
        CRITICAL:  wd.cdv.alert({
            type: "CRITICAL", 
            description: "Test failed with CRITICAL"
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
        name: 'Community Data Validation'
    };
    
    spec = _.extend({},_spec,spec);

    var myself = {};
    
    // Apply mixins
    wd.cdv.exceptionsMixin(myself);
    wd.cdv.alertMixin(myself);
    

    
    myself.makeTest = function(test){
        
        debugger;
        wd.log("Making test");
        
        
    }
    
    
    return myself;


}



