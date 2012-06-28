/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. 
 *  
 */


/*
 *  Text editor component 
 */

var TextEditorComponent = BaseComponent.extend({

    $ph: undefined,
    $rightPanel: undefined,
    isRightPanelShown: false,
    isInitialazed: false,
    externalEditor: undefined,
    defaultButtons: [
    {
        clazz: "save",
        label: "Save", 
        callback: function(){
            this.save();
        }
    }
    ],
    
    
    /* // Default settings
     * file: the file to edit
     */ 

    initialize: function(){
        
        Dashboards.log("Initializing TextEditorComponent")  
        this.isInitialazed = true;  
       
        // Do we have an htmlObject? if no, create one. If yes, setup placeholder
        if(this.htmlObject){
            this.$ph = $("#" + this.htmlObject);
        }
        else{
            this.$ph = $("<div id='textEditorDefautlId'></div>").appendTo("body");
        }
        
    
     
    },

    update: function(){
        
        
        var myself = this;
        
        if(!this.isInitialazed){
            myself.initialize();
        }

        this.isRightPanelShown = false;
        
        
        // Render the correct structure
        var buttons = this.getButtons();
        var template = "<div class='textEditorComponent'><div class='textEditorControls'>"+
        "<div class='textEditorFile'><span class='fileLabel'>File: </span>{{file}}</div>"+
        "<div class='textEditorButtons'>{{#buttons}}<button class='{{clazz}}'>{{label}}</button>{{/buttons}}</div>" +
        "</div><div class='textEditorNotification'><span class='textEditorNotificationMsg'>Test</span></div>"+
        "<div class='textEditorRightPanel'></div>"+
        "<div class='textEditorIframeContainer'><div class='textEditorIframe'><iframe></iframe></div>"+
        "</div>";
        
        this.$ph.html(Mustache.render(template, {
            file: this.file || "Unknown file", 
            buttons:buttons
        }));
    
        // bind
        this.$ph.find(".textEditorControls").on("click","button",function(){
            var $this = $(this);
            var idx = $this.prevAll("button").length;

            buttons[idx].callback(arguments);
        })
        
        if(this.file){
            this.loadFile();
        }
        
    //alert("Ok!");
       
        
    },
    
    getButtons: function(){
        
        var myself = this;
        var _extraButtons = this.extraButtons || [];
        _.chain(this.defaultButtons).each(function(b){
            b.callback = _.bind(b.callback, myself);
        })
        return this.defaultButtons.concat(_extraButtons);
        
    },
    
    setFile: function(_file){
        this.file = _file;
    },
    
    getFile: function(){
        return this.file;
    },
    
    loadFile: function() {

        var myself=this;
        

        // Disable button
        $('button.save',this.$ph).attr('disabled', true);

        this.externalEditor = $('iframe',this.$ph);
        //this.externalEditor.height(window.innerHeight - 200 -5);

        this.externalEditor.load(function() 
        {

            var editorEnv = myself.getEditorWindow();
            editorEnv.listeners.onStatusUpdate = myself.setDirty;
            editorEnv.listeners.notify = function(msg, type){
                myself.notify(msg);
            }
      
            $('#notifications').hide();
        });
        //var width = this.externalEditor.width() -10;
        this.externalEditor.attr('src','../pentaho-cdf-dd/extEditor?path=' + this.file + '&theme=ace/theme/eclipse&editorOnly=true');// &width='+width );        
        
    },
    
    notify: function(msg, level /*todo*/){
        
        var $notifications = this.$ph.find(".textEditorNotificationMsg");
        $notifications.text(msg);
        $notifications.show().delay(4000).fadeOut('slow');
    },
        
        
    setDirty: function(isDirty){
        $('button.save',this.$ph).attr('disabled', !isDirty);
    },
  
    getEditorWindow: function(){
        return this.externalEditor[0].contentWindow;
    },


    save: function(){
        
        this.getEditorWindow().save();
        
        // Also - trigger a call to refresh the cache.
        $.ajax({
            url: "refresh",
            async: true,
            success: function(){
                Dashboards.fireChange("editorFileSaved","xxx");
            }
        })
    },
    
    
    getRightPanel: function(){
        
        return this.$ph.find(".textEditorRightPanel");
        
    },
    
    toggleRightPanel: function(){
        
        this.getRightPanel().toggle();
        this.isRightPanelShown = !this.isRightPanelShown;
        
        // Force a resize on ace:
        this.getEditorWindow().editor.getEditor().resize();
        
        return this.isRightPanelShown;
    }
    


});



/*
 *  Popup text editor component 
 */

var PopupTextEditorComponent = BaseComponent.extend({

    $ph: undefined,
    isInitialazed: false,
    textEditor: undefined,
    textEditorPopupId: "popupTextEditorId",
    isQueryPreviewShown: false,
    testPromptPopup: undefined,
    $testPromptPopupObj: undefined,
    defaultButtons: [    
    {
        clazz: "run",
        label: "Run Test", 
        callback: function(){
            this.runTest();
        }
    },   
    {
        clazz: "previewQuery",
        label: "Query results", 
        callback: function(){
            this.toggleQueryResults();
        }
    },
    {
        clazz: "close",
        label: "Close", 
        callback: function(){
            this.hide();
        }
    }       
    ],
        
    /* // Default settings
     * file: the file to edit
     */ 

    initialize: function(){
        
        Dashboards.log("Initializing PopupTextEditorComponent")  
        this.isInitialazed = true;  
       
        // We need to create a placeholder for this
        this.$ph = $("#"+this.textEditorPopupId);
        if(this.$ph.length > 0){
            // we found one already?
            Dashboards.log("[PopupTextEditorComponent] Unexpected - Found an element with id " + this.popupTextEditorDefautlId)
        }
        else{
            this.$ph = $("<div id='"+this.textEditorPopupId+"'></div>").appendTo("body");
        }
        
        // Also generate a textEditorComponent
        this.textEditor = {
            name: "popupInnerTextEditorComponent", 
            type: "textEditor", 
            file: undefined,  // will be set later
            htmlObject: this.textEditorPopupId,
            extraButtons: this.getButtons()
                
        };
        
        
        Dashboards.addComponents([this.textEditor]);
     
    },

    update: function(){
        
        
        var myself = this;
        this.isQueryPreviewShown = false;
        
        
        if(!this.isInitialazed){
            myself.initialize();
        }

        // Update the text component
        
        this.textEditor.update();

        
    //alert("Ok!");
       
        
    },
    
    show: function(){

        this.$ph.find(">div.textEditorComponent").height($(window).height());
        this.$ph.slideDown();

    },
    
    
    hide: function(){
        
        this.$ph.slideUp();

    },

    runTest: function(){
        
        var env = this.setupEnvironment();
        var test = this.getTestToOperate(env,this.runTestCallback, $("button.previewQuery",this.$ph));
        
        
    },
    
    runTestCallback: function(env, test){

        var myself = this;
        this.textEditor.notify("Running test...");
        env.cdv.runTest(test, { 
            callback: function(result){
                myself.textEditor.notify(result.getTestResultDescription());
            }
        });
            
            
    },
    
    toggleQueryResults: function(){
      
      
        if(this.isQueryPreviewShown){
            // Hide it
            this.isQueryPreviewShown = this.textEditor.toggleRightPanel();
            return;
        }
        
        
        // Ok - Try to open it and run the test
        var env = this.setupEnvironment();
        var test = this.getTestToOperate(env,this.toggleQueryResultsCallback, $("button.run",this.$ph));
        


      
    },
    
    toggleQueryResultsCallback: function(env,test){
        
        var myself = this;
        this.textEditor.notify("Running query...");

        env.cdv.executeQuery(test,null, function(test,opts,queryResult){
            myself.textEditor.notify("Queries ran in " + queryResult.duration + "ms");
            
                    
            myself.textEditor.getRightPanel().html("<pre>"+JSON.stringify( queryResult.resultset ,undefined,2)+"</pre>");
            myself.isQueryPreviewShown = myself.textEditor.toggleRightPanel();
            wd.log("Toggling!");
            
        });
        
        
    },

    getButtons: function(){
        
        var myself = this;
        
        var _extraButtons = this.extraButtons || [];
        
        _.chain(this.defaultButtons).each(function(b){
            b.callback = _.bind(b.callback, myself);
        })
        return this.defaultButtons.concat(_extraButtons);
    },

    setFile: function(_file){
        this.file = _file;
        this.textEditor.setFile(_file);
    },
    
    
    setupEnvironment: function(){
        
        // Get source
        
        var src = this.textEditor.getEditorWindow().editor.getContents();
        
        var mask = {
            cdv: wd.cdv.cdv({
                isServerSide: false
            })
        };
        
        // mask global properties 
        for (p in this)
            mask[p] = undefined;

        // execute script in private context
        try{
            (new Function( "with(this) { " + src + "}")).call(mask);
        }
        catch(err){
            alert(err);
        }
        
        return mask;
        
    },
  
  
    getTestToOperate: function(env, operationCallback, target){
        
        var myself = this;
                
        // How many tests do we have? If only one, return
        
        var flattenedTests = env.cdv.listTestsFlatten().sort(function(a,b){
            return (a.group + a.name) >=  (b.group + b.name)
        });
        
        
        if(flattenedTests.length == 1){
            // return it
            operationCallback.call(myself,env,env.cdv.getTest(flattenedTests[0].group,flattenedTests[0].name));
            return;
        }
        
        
        // We need to prompt for the test
        if(!this.testPromptPopup){
            
                    
            // Generate a popup component for us
            this.testPromptPopup = {
                name: "testPromptPopup", 
                type:"popup", 
                htmlObject: 'testPromptPopupObj',
                gravity: "S",
                draggable: false,
                closeOnClickOutside: true
            }
            
            this.$testPromptPopupObj = $("<div id='testPromptPopupObj'></div>").appendTo("body");
            
            Dashboards.addComponents([this.testPromptPopup]);
            this.testPromptPopup.update();
            
            // Allow customization
            this.$testPromptPopupObj.parent("div.popupComponent").addClass("testPromptPopup");
        
            
        }
        
        var template = '<div class="testChooserWrapper"><div class="title">Multiple tests found. Choose the one you want:</div>'+
        '<div class="testChooserButtons">{{#tests}}<button> {{group}} - {{name}}</button>{{/tests}}</div></div>'
        this.$testPromptPopupObj.html(Mustache.render(template, {
            tests: flattenedTests 
        }));
        

        this.$testPromptPopupObj.off("click","button");
        this.$testPromptPopupObj.on("click","button",function(evt){
            var idx = $(this).prevAll("button").length;
            operationCallback.call(myself,env,env.cdv.getTest(flattenedTests[idx].group,flattenedTests[idx].name));
           
            myself.testPromptPopup.hide();
        })
        
        this.testPromptPopup.popup(target);
        
        return;
        
    }
    
    

});



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
        name: "Notification Settings",
        link: "notificationSettings",
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
        intialized: false,
        popup: undefined,
        popupObj: undefined,
        textEditor: undefined,
        textEditorObj: undefined,
        defaults: {

            idColIndex: 6,
            pathColIndex: 7,
            popup: [
            {
                name: "Run Test", 
                callback: function(test){
                    Dashboards.log("Clicked on Run for " + test.id);

                    var result = cdv.runTestById(test.id);
                    Dashboards.log("Test output: " + result);

                }
            },
            {
                name: "Edit Test", 
                callback: function(test){

                    // Editing

                    this.editFile(test.path);
                    this.popup.hide();
                }
            },
            {
                name: "View Previous Executions", 
                callback: function(test){
                    Dashboards.log("View Previous Executions " + test.id);
                }
            },
            {
                name: "Delete Test", 
                callback: function(test){
                    Dashboards.log("Clicked on Delete for " + test.id);
                    if(confirm("You sure you want to delete this test?")){
                        alert("Todo: Delete the test");
                    }
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
            
            if(!this.initialized){
                

                // Generate a popup component for us
                this.popup = {
                    name: "validationAddinPopupComponent", 
                    type:"popup", 
                    htmlObject: 'validationAddinPopupObj',
                    gravity: "W",
                    draggable: false,
                    closeOnClickOutside: true
                }

                this.popupObj = $("<div id='validationAddinPopupObj' class='validationButtonPopup'></div>").appendTo("body");


                // Also generate a textEditorComponent
                this.popupTextEditor = {
                    name: "popupTextEditorComponent", 
                    type: "popupTextEditor", 
                    file: undefined // will be set later
                };

                // Call cdf comp
                Dashboards.addComponents([this.popup, this.popupTextEditor]);
                this.popup.update();
                this.popupTextEditor.update();
               
                this.initialized = true;
            }


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
                var testPath = st.rawData.resultset[st.rowIdx][opt.pathColIndex];


                $popupDiv.html(Mustache.render(template, {
                    popup: opt.popup
                }));


                $popupDiv.find("> div").off("click").on("click","button",function(evt){
                    var $target = $(this);
                    var idx = $target.prevAll("button").length;

                    opt.popup[idx].callback.call(myself, {
                        id: testId, 
                        path: testPath
                    });
                })

                //myself.popupObj

                // Add a class to the popupComponent so that we can style it

                myself.popup.popup($t);
                return false;

            // style: move this a bit to the right

            })


        },

        editFile: function(path, readonly){

            // Todo - implement

            this.popupTextEditor.setFile(path);
            this.popupTextEditor.update();
            this.popupTextEditor.show();

        }
    };

Dashboards.registerAddIn("Table", "colType", new AddIn(wd.cdvUI.validationPopupAddIn));
