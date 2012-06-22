/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. 
 *  
 */

var wd = wd || {};
wd.cdvUI = wd.cdvUI ||{
    

    siteMap: [
    {
        name: "Home",
        link: "home",
        sublinks: []
    },
    {
        name: "Validations",
        link: "validations",
        sublinks: []
    },
    {
        name: "CDA Errors",
        link: "cdaErrors",
        sublinks: []
    },
    {
        name: "Slow Queries",
        link: "slowQueries",
        sublinks: []
    },
    {
        name: "About",
        link: "about",
        sublinks: []
    },

    ]
    
    
    
};



wd.cdvUI.validationFileAddin = {
    name: "validationFile",
    label: "ValidationFile",
    defaults: {
        
    },
    init: function(){
        
        // Register this for datatables sort
        var myself = this;
        $.fn.dataTableExt.oSort[this.name+'-asc'] = function(a,b){
            return myself.sort(a,b)
        };
        $.fn.dataTableExt.oSort[this.name+'-desc'] = function(a,b){
            return myself.sort(b,a)
        };     
    }, 
    sort: function(a,b){
        return this.sumStrArray(a) - this.sumStrArray(b);
    }, 

    implementation: function (tgt, st, opt) {
        
        // encapsulate this
        
        var $t = $(tgt);
        var text = st.value;
        
        if($t.find("div.validationFileWrapper").length>0){
            return; // Done already
        }
        
        var val = _.map(text.split(" ;"),function(e){
    
            var obj = {};
    
            var arr = e.match(/(.*)\[(.*)] (.*)/);
            obj.cda = arr[1];
            obj.dataAccessId = arr[2];
    
            var pathArray = obj.cda.split("/")
            var a = pathArray.slice(0,pathArray.length-1);
            obj.file = pathArray.slice(pathArray.length - 1)[0];
            obj.path = pathArray.slice(0,pathArray.length - 1).join("/")+"/";
            
            
            obj.pathDesc = (a.length>3?a.slice(0,2).concat([".."]).concat(a.slice(a.length-1,a.length)):a).join("/")+"/"
    
            var params = arr[3];
            if(params){
                obj.params = _.map(params.substr(2,params.length-3).split(", "), function(param){
                    var a = param.split(": ");
                    return {
                        paramName: a[0], 
                        paramValue: a[1]
                    } ;
                });
        
            }
            
            obj.paramLink = function(){
                if(!obj.params){
                    return "";
                }
                
                var n = obj.params.length;
                var template = " <a title='" + _.map(obj.params,function(p){
                    return p.paramName+": "+p.paramValue;
                }).join("<br />") + "' class='params'>(" + n + " param" + (n>1?"s":"") +")</a>";
                
                return template;
            };
            
            return obj;
    
        });

        var template = "<div class='validationFileWrapper'>{{#val}}<div class='cda'><div class='cdaFile'>" +
        "<span class='cdaPath' title='{{cda}}'>{{pathDesc}}</span><span>{{file}}</span><span class='dataAccessId'>:{{dataAccessId}}</span>"+
        "<span class='paramLink'>{{{paramLink}}}</span></div>" +
        // "</div>{{#params}}<div class='params'><div class='paramKey'>{{paramName}}" +
        // "</div><div class='paramValue'>{{paramValue}}</div></div>{{/params}}" +
        "</div>{{/val}}</div>";

        $t.html(Mustache.render(template, {
            val: val
        }));
        
        $t.find("a.params").tipsy({
            gravity: 's', 
            html:true
        });
        $t.find("span.cdaPath").tipsy({
            gravity: 's', 
            html:true
        });
    
    }
};

Dashboards.registerAddIn("Table", "colType", new AddIn(wd.cdvUI.validationFileAddin));




wd.cdvUI.validationButtonsAddIn = {
    name: "validationButtons",
    label: "ValidationButtons",
    defaults: {
        
        idColIndex: 1,
        buttons: [
        {
            name: "Run", 
            callback: function(testName){
                Dashboards.log("Clicked on Run for " + testName);
            }
        },
        {
            name: "Edit", 
            callback: function(testName){
                Dashboards.log("Clicked on Edit for " + testName);
            }
        },
        {
            name: "Delete", 
            callback: function(testName){
                Dashboards.log("Clicked on Delete for " + testName);
            }
        }
        ]  
    },
    init: function(){
        
        // Register this for datatables sort
        var myself = this;
        $.fn.dataTableExt.oSort[this.name+'-asc'] = function(a,b){
            return myself.sort(a,b)
        };
        $.fn.dataTableExt.oSort[this.name+'-desc'] = function(a,b){
            return myself.sort(b,a)
        };
        
        
        
    },

    implementation: function (tgt, st, opt) {
        
        // encapsulate this
        
        var $t = $(tgt);
        
        if($t.find("button").length>0){
            return; // Done already
        }
      
        var template = "<div class='validationButtons'>{{#buttons}}<button class='validationButton'>{{name}}</button>{{/buttons}}</div>";
        
        $t.html(Mustache.render(template, {
            buttons: opt.buttons
        }));
        
        var testName = $t.parent("tr").find("td:nth-child("+(opt.idColIndex + 1)+")").text();
        
        var myself = this;
        $t.find("> div").on("click","button",function(evt){
            var $target = $(this);
            var idx = $target.prevAll("button").length;
            
            opt.buttons[idx].callback.call(myself, testName);
        })
        
        
    }
};

Dashboards.registerAddIn("Table", "colType", new AddIn(wd.cdvUI.validationButtonsAddIn));



wd.cdvUI.validationPopupAddIn = {
    name: "validationPopup",
    label: "ValidationPopup",
    popup: undefined,
    popupObj: undefined,
    defaults: {
        
        idColIndex: 6,
        popup: [
        {
            name: "Run Test", 
            callback: function(testId){
                Dashboards.log("Clicked on Run for " + testId);
                
                var result = cdv.runTestById(testId);
                Dashboards.log("Test output: " + result);
                
            }
        },
        {
            name: "View Test", 
            callback: function(testId){
                Dashboards.log("Clicked on View for " + testId);
            }
        },
        {
            name: "Edit Test", 
            callback: function(testId){
                Dashboards.log("Clicked on Edit for " + testId);
            }
        },
        {
            name: "View Query Result", 
            callback: function(testId){
                Dashboards.log("Clicked on View Query Result for " + testId);
            }
        },
        {
            name: "Delete Test", 
            callback: function(testId){
                Dashboards.log("Clicked on Delete for " + testId);
            }
        }
        ]  
    },
    init: function(){
        
        // Register this for datatables sort
        var myself = this;
        $.fn.dataTableExt.oSort[this.name+'-asc'] = function(a,b){
            return myself.sort(a,b)
        };
        $.fn.dataTableExt.oSort[this.name+'-desc'] = function(a,b){
            return myself.sort(b,a)
        };
        
        // Generate a popup component for us
        this.popup = new PopupComponent({
            name: "validationAddinPopupComponent", 
            type:"popup", 
            htmlObject: 'validationAddinPopupObj',
            gravity: "W",
            draggable: false,
            closeOnClickOutside: true
        })
        
        this.popupObj = $("<div id='validationAddinPopupObj' class='validationButtonPopup'></div>").appendTo("body");
        
        // Call cdf comp
        Dashboards.addComponents([this.popup]);
        this.popup.update();
        
    },

    implementation: function (tgt, st, opt) {
        
        // encapsulate this
        
        var $t = $(tgt),
        isFirst = false;
        
        var $popupDiv = $("#validationAddinPopupObj");
        
        if($t.find("button").length>0){
            return; // Done already
        }
        
        $t.html("<button>Options</button>")


        // Bind this to a clickable element, setting the appropriate functions
        var myself = this;
        
        var popup = this.popupObj.parent("div.popupComponent");
        if(!popup.hasClass("optionsPopup")){
            popup.addClass("optionsPopup");
        }
            

        $t.click(function(){
            
            var template = "<div class='validationPopup'>{{#popup}}<button class='validationButton'>{{name}}</button>{{/popup}}</div>";
            
            var testId = st.rawData.resultset[st.rowIdx][opt.idColIndex];
            
        
            $popupDiv.html(Mustache.render(template, {
                popup: opt.popup
            }));


            $popupDiv.find("> div").off("click").on("click","button",function(evt){
                var $target = $(this);
                var idx = $target.prevAll("button").length;
            
                opt.popup[idx].callback.call(myself, testId);
            })
            
            //myself.popupObj

            // Add a class to the popupComponent so that we can style it
            
            myself.popup.popup($t);
            return false;
            
            // style: move this a bit to the right
            
        })
        
        
        
        
    }
};

Dashboards.registerAddIn("Table", "colType", new AddIn(wd.cdvUI.validationPopupAddIn));