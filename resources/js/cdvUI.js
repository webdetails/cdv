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
            if(typeof this.saveCallback === "function" ){
                this.saveCallback();
            }
            
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
        "<div class='textEditorIframeContainer'><div class='textEditorIframe'><iframe seamless='true' marginheight='0'></iframe></div>"+
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
        var headerHeight = $('.textEditorControls', this.$ph).height() + $('.textEditorNotification', this.$ph).height();
        var editorHeight = this.$ph.height() - headerHeight - 5;
        this.externalEditor.height(editorHeight);

        this.externalEditor.load(function() 
        {

            var editorEnv = myself.getEditorWindow();
            editorEnv.listeners.onStatusUpdate = myself.setDirty;
            editorEnv.listeners.notify = function(msg, type){
                myself.notify(msg);
            }
      
            $('#notifications').hide();
        });

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
        label: "Preview Test", 
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
            extraButtons: this.getButtons(),
            saveCallback: this.saveCallback
                
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
        if(env){
            var test = this.getTestToOperate(env,this.runTestCallback, $("button.previewQuery",this.$ph));            
        }
        
        
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
        if(env){
            var test = this.getTestToOperate(env,this.toggleQueryResultsCallback, $("button.run",this.$ph));
        }
        


      
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
            return null;
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
    /*{
        name: "Home",
        link: "home",
        sublinks: []
    },*/
    {
        name: "Validations",
        link: "validations",
        sublinks: []
    },
    {
        name: "Alerts",
        link: "alerts",
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


    wd.cdvUI.newValidation = function () {


        // Also generate a textEditorComponent
     
        var popupTextEditor = Dashboards.getComponent("popupTextEditorComponent");
        if (!popupTextEditor) {
            popupTextEditor = {
                name: "popupTextEditorComponent", 
                type: "popupTextEditor", 
                file: undefined, // will be set later
                saveCallback: function(){
                    // We want to refresh CDV
                    $.ajax({
                        url: "refreshTests",
                        type: "GET",
                        async: true,
                        success: function(){
                            Dashboards.fireChange("editorFileSaved","xxx");
                        }
                    })
                }
            };
            // Call cdf comp
            Dashboards.addComponents([popupTextEditor]);
            popupTextEditor.update();
        }


        var txt = 'New Test name: <input id="testName" type="text" placeholder="newTest"></input>';                                                            
        var callBackFunction = function (v,m,f) {
            if(v !== undefined){
                if (v === "ok"){                                
                    var newFileName = m.children()[0].value;     
                                
                    if (newFileName.length == 0) {
                        alert("Please enter a file name");
                    } else {
                        $.ajax({
                            url: "newTest",
                            data: {
                                newName: newFileName
                            },
                            type: "GET",
                            async: true,
                            success: function(response){
                                if (response && response.success == "true") {

                                    popupTextEditor.setFile(response.path);
                                    popupTextEditor.update();
                                    popupTextEditor.show();                            
                                } else {
                                    alert("Error while creating new test");
                                }  
                            }
                        });                                                        
                    }
                }	
            }
        };

        var promptConfig = {
            callback: callBackFunction,
            buttons: {
                Cancel: 'cancel'
            },
            loaded: function(){}
        };
        
        promptConfig.buttons['New'] = "ok";    
        $.prompt(txt,promptConfig);                                                            
    };



    /* Copy samples from cdv to sample tests */

    wd.cdvUI.copyCDVTests = function(){
        
        $.ajax({
            url: "copyCDVTests",
            type: "GET",
            async: true,
            success: function(response){
                if (response && response.success == "true") {
                    Dashboards.fireChange("tableChanged","foo");
                } else {
                    alert("Error while copying tests");
                }  
            }
        });  
        
    };


    wd.cdvUI.alertDescriptionAddin = {
        name: "alertDescription",
        label: "alertDescription",
        defaults: {
        
        },
        init: function(){
        
            // Register this for datatables sort
            $.fn.dataTableExt.oSort[this.name+'-asc'] = $.fn.dataTableExt.oSort['string-asc'];
            $.fn.dataTableExt.oSort[this.name+'-desc'] = $.fn.dataTableExt.oSort['string-desc'];
        }, 
        sort: function(a,b){
            return this.sumStrArray(a) - this.sumStrArray(b);
        }, 

        implementation: function (tgt, st, opt) {
        
            // encapsulate this
        
            var $t = $(tgt);
            var text = st.value;
            var popup =  st.tableData[st.rowIdx][8];
        
            if($t.find("div.alertPopup").length>0){
                return; // Done already
            }
            
            var template = "<div class='alertPopup'>" + 
            "<div class='cda'><div class='cdaFile'>" +
            "<span class='cdaPath' title='{{popup}}'>{{val}}</span>" + 
            "</div></div></div>";

            $t.html(Mustache.render(template, {
                val: text,
                popup: popup
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

Dashboards.registerAddIn("Table", "colType", new AddIn(wd.cdvUI.alertDescriptionAddin));





    wd.cdvUI.validationFileAddin = {
        name: "validationFile",
        label: "ValidationFile",
        defaults: {
        
        },
        init: function(){
        
            // Register this for datatables sort
            $.fn.dataTableExt.oSort[this.name+'-asc'] = $.fn.dataTableExt.oSort['string-asc'];
            $.fn.dataTableExt.oSort[this.name+'-desc'] = $.fn.dataTableExt.oSort['string-desc'];
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
    
                var arr = e.match(/(.*?)\[(.*?)](?: (.*))?/);
                obj.cda = arr[1];
                obj.dataAccessId = arr[2];
    
                var pathArray = obj.cda.split("/")
                var a = pathArray.slice(0,pathArray.length-1);
                obj.file = pathArray.slice(pathArray.length - 1)[0];
                obj.path = pathArray.slice(0,pathArray.length - 1).join("/")+"/";
            
            
                obj.pathDesc = (a.length>3?a.slice(0,2).concat([".."]).concat(a.slice(a.length-1,a.length)):a).join("/")+"/"
    
                var params = arr[3];
                if(params){
                    obj.params = _.map(params.substr(1,params.length-2).split(", "), function(param){
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
                        return p.paramName+": "+p.paramValue.replace(/'/g,"&quot;");
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
            }/*,
            {
                name: "Delete", 
                callback: function(testName){
                    Dashboards.log("Clicked on Delete for " + testName);
                }
            }*/
            ]  
        },
        init: function(){
        
            // Register this for datatables sort
            $.fn.dataTableExt.oSort[this.name+'-asc'] = $.fn.dataTableExt.oSort['string-asc'];
            $.fn.dataTableExt.oSort[this.name+'-desc'] = $.fn.dataTableExt.oSort['string-desc'];
        
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

            idColIndex: 5,
            nameColIndex: 1,
            groupColIndex: 0,
            pathColIndex: 6,
            popup: [
            {
                name: "Run Test", 
                callback: function(test){
                    Dashboards.log("Clicked on Run for " + test.id);
                    var myself = this;
                    Dashboards.incrementRunningCalls();
                    setTimeout(function(){
                        $.getJSON('runTest',
                        {
                            name: test.name,
                            group: test.group
                        },
                        function(result){
                            alert(JSON.stringify(result,null,2));
                            Dashboards.fireChange("tableChanged",true);
                            Dashboards.decrementRunningCalls();
                            myself.popup.hide();
                        });
                    }, 1);
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
                    Dashboards.log("View Previous Executions " + test.group + "test.name");
                    document.location.href="alerts?cdvGroup=" + encodeURIComponent(test.group) + "&cdvName=" + encodeURIComponent(test.name);
                }
            },/*
            {
                name: "Delete Test", 
                callback: function(test){
                    Dashboards.log("Clicked on Delete for " + test.id);
                    if(confirm("You sure you want to delete this test?")){
                        alert("Todo: Delete the test");
                    }
                }
            },*/
            {
                name: "Duplicate Test", 
                callback: function(test){
                    Dashboards.log("Clicked on Duplicate for " + test.id);                    

                    var myself= this;
                    var txt = 'New file name: <input id="testName" type="text" placeholder="New Test Name"></input>';                                                            
                    var callBackFunction = function (v,m,f) {
                        if(v !== undefined){
                            if (v === "ok"){                                
                                var newFileName = m.children()[0].value;     
                                
                                if (newFileName.length == 0) {
                                    alert("Please enter a file name");
                                } else {
                                    $.ajax({
                                        url: "duplicateTest",
                                        data: {
                                            path: test.path, 
                                            newName: newFileName
                                        },
                                        type: "GET",
                                        async: true,
                                        success: function(response){
                                            if (response && response.success == "true") {
                                                myself.editFile(response.path);
                                                myself.popup.hide();                                
                                            } else {
                                                alert("Error while duplicating");
                                            }  
                                        }
                                    });                                                        
                                }
                            }	
                        }
                    };

                    var promptConfig = {
                        callback: callBackFunction,
                        buttons: {
                            Cancel: 'cancel'
                        },
                        loaded: function(){}
                    };
        
                    promptConfig.buttons['Duplicate'] = "ok";
                    this.popup.hide();
                    $.prompt(txt,promptConfig);                                                            
                }
            }            
            ]  
        },
        init: function(){

            // Register this for datatables sort
            var myself = this;
            $.fn.dataTableExt.oSort[this.name+'-asc'] = $.fn.dataTableExt.oSort['string-asc'];
            $.fn.dataTableExt.oSort[this.name+'-desc'] = $.fn.dataTableExt.oSort['string-desc'];


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
                    file: undefined, // will be set later
                    saveCallback: function(){
                        // We want to refresh CDV
                        $.ajax({
                            url: "refreshTests",
                            type: "GET",
                            async: true,
                            success: function(){
                                Dashboards.fireChange("editorFileSaved","xxx");
                            }
                        })
                    }
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
                var testName = st.rawData.resultset[st.rowIdx][opt.nameColIndex];
                var testGroup = st.rawData.resultset[st.rowIdx][opt.groupColIndex];
                var testPath = st.rawData.resultset[st.rowIdx][opt.pathColIndex];


                $popupDiv.html(Mustache.render(template, {
                    popup: opt.popup
                }));


                $popupDiv.find("> div").off("click").on("click","button",function(evt){
                    var $target = $(this);
                    var idx = $target.prevAll("button").length;

                    opt.popup[idx].callback.call(myself, {
                        id: testId, 
                        name: testName, 
                        group: testGroup, 
                        path: testPath
                    }, opt);
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


    // CDA Popup Addin, which is the same as the one before except with new properties

    wd.cdvUI.cdaPopupAddIn = $.extend({}, wd.cdvUI.validationPopupAddIn,{
        name: "cdaPopup",
        label: "cdaPopup",
        defaults: {
            idColIndex: 4,
            pathColIndex: 1,
            eventType: "QueryError",
            popup: [
            /*{
                name: "Rerun query", 
                callback: function(test){
                    Dashboards.log("Clicked on ReRun for " + test.id);
                    $.getJSON('runTest',
                    {
                        name: test.name,
                        group: test.group
                    },
                    function(result){
                        alert(JSON.stringify(result,null,2));
                    });

                }
            },*/
            {
                name: "Delete Entry", 
                callback: function(test){
                    this.deleteCDAReport(test.id);
                    
                }
            },
            {
                name: "Delete all", 
                callback: function(test, opt){
                    this.deleteAllCDAReportsOfType(opt.eventType);
                    
                }
            }
            ]          
        },
       
        deleteCDAReport: function(id){
            Dashboards.log("Clicked on Delete for " + id);
            if(confirm("You sure you want to delete this test?")){
            
                var myself = this;
            
                $.ajax({
                    url: "deleteCdaEntry",
                    type: "GET",
                    data:{
                        cdaEntryId: id
                    },
                    async: true,
                    success: function(){
                        Dashboards.fireChange("entriesChanged","xxx");
                        myself.popup.hide();
                    },
                    fail: function(jqXHR, textStatus) {
                        alert( "Request failed: " + textStatus );
                        myself.popup.hide();
                    }
                })
                
                
            }
            
        },
        
        deleteAllCDAReportsOfType: function(eventType){
            Dashboards.log("Clicked on Delete all cda report of type for " + eventType);
            var myself = this;
            if(confirm("You sure you want to delete all entries?")){
            
                $.ajax({
                    url: "deleteCdaEntriesOfEventType",
                    type: "GET",
                    data:{
                        eventType: eventType
                    },
                    async: true,
                    success: function(){
                        Dashboards.fireChange("entriesChanged","xxx");
                        myself.popup.hide();
                    },
                    fail: function(jqXHR, textStatus) {
                        alert( "Request failed: " + textStatus );
                        myself.popup.hide();
                    }
                })
            

            }
            this.popup.hide();
        }
    });
    
    
Dashboards.registerAddIn("Table", "colType", new AddIn(wd.cdvUI.cdaPopupAddIn));


    // CDA Popup Addin, which is the same as the one before except with new properties

    wd.cdvUI.alertsAddIn = $.extend({}, wd.cdvUI.validationPopupAddIn,{
        name: "alertsPopup",
        label: "alertsPopup",
        defaults: {
            idColIndex: 7,
            nameColIndex: 3,
            groupColIndex: 2,
            popup: [
            {
                name: "Focus on this test", 
                callback: function(test){
                    this.focusOnTest(test);
                    
                }
            },
            {
                name: "Delete Entry", 
                callback: function(test){
                    this.deleteAlert(test.id);
                    
                }
            }
            ]          
        },
                
        focusOnTest: function(test){
          
            this.popup.hide();
            Dashboards.setParameter('cdvGroup',test.group);
            Dashboards.fireChange('cdvName',test.name);
            return;
        },
        
        deleteAlert: function(id){
            Dashboards.log("Clicked on Delete for " + id);
            if(confirm("You sure you want to delete this alert?")){
            
                var myself = this;
            
                $.ajax({
                    url: "deleteAlert",
                    type: "GET",
                    data:{
                        alertId: id
                    },
                    async: true,
                    success: function(){
                        Dashboards.fireChange("entriesChanged","xxx");
                        myself.popup.hide();
                    },
                    fail: function(jqXHR, textStatus) {
                        alert( "Request failed: " + textStatus );
                        myself.popup.hide();
                    }
                })
                
                
            }
            
        },
        
        deleteAllAlerts: function(){
            Dashboards.log("Clicked on Delete all alerts");
            var myself = this;
            if(confirm("You sure you want to delete all alerts?")){
            
                $.ajax({
                    url: "deleteAllAlerts",
                    type: "GET",
                    data:{},
                    async: true,
                    success: function(){
                        Dashboards.fireChange("entriesChanged","xxx");
                        myself.popup.hide();
                    },
                    fail: function(jqXHR, textStatus) {
                        alert( "Request failed: " + textStatus );
                        myself.popup.hide();
                    }
                })
            

            }
            this.popup.hide();
        }
    });
    
    
Dashboards.registerAddIn("Table", "colType", new AddIn(wd.cdvUI.alertsAddIn));



    // Group date
    wd.cdvUI.elapsedTimeAddIn = {
        name: "elapsedTime",
        label: "elapsedTime",
        defaults: {
            longFormat: true
        },
        init: function(){
        
            // Register this for datatables sort
            var myself = this;
            $.fn.dataTableExt.oSort[this.name+'-asc'] = $.fn.dataTableExt.oSort['numeric-asc'];
            $.fn.dataTableExt.oSort[this.name+'-desc'] = $.fn.dataTableExt.oSort['numeric-desc'];
        }, 

        implementation: function (tgt, st, opt) {
        
            // encapsulate this
            var $t = $(tgt);
            var text = st.value;
            $t.text(wd.cdv.utils.groupTimestamp(text,1,opt));
    
        }
    };

Dashboards.registerAddIn("Table", "colType", new AddIn(wd.cdvUI.elapsedTimeAddIn));

    // Group date
    wd.cdvUI.alertTypeAddIn = {
        name: "alertType",
        label: "alertType",
        defaults: {
    
        },
        init: function(){
    
            // Register this for datatables sort
            var myself = this;
            $.fn.dataTableExt.oSort[this.name+'-asc'] = $.fn.dataTableExt.oSort['numeric-asc'];
            $.fn.dataTableExt.oSort[this.name+'-desc'] = $.fn.dataTableExt.oSort['numeric-desc'];
        }, 

        implementation: function (tgt, st, opt) {
    
            // encapsulate this
            var $t = $(tgt);
            var text = st.value;
        
            $t.parent("tr").addClass('alert' + text.toLowerCase());

        }
    };

Dashboards.registerAddIn("Table", "colType", new AddIn(wd.cdvUI.alertTypeAddIn));

    wd.cdvUI.testResultAddIn = {
        name: "testResult",
        label: "TestResult",
        defaults: {
    
        },
        init: function(){
    
            // Register this for datatables sort
            $.fn.dataTableExt.oSort[this.name+'-asc'] = $.fn.dataTableExt.oSort['string-asc'];
            $.fn.dataTableExt.oSort[this.name+'-desc'] = $.fn.dataTableExt.oSort['string-desc'];
        }, 
        sort: function(a,b){
            return this.sumStrArray(a) - this.sumStrArray(b);
        }, 

        implementation: function (tgt, st, opt) {
    
            // encapsulate this
    
            var $t = $(tgt).empty();
            var result = st.value.split('|');
            var $elem = $("<div></div>").appendTo($t).text(result[0]).attr('title',result[1]);
            $elem.tipsy({
                gravity: 's', 
                html:true
            });

        }
    };

Dashboards.registerAddIn("Table", "colType", new AddIn(wd.cdvUI.testResultAddIn));

    wd.cdvUI.testResultAddIn = {
        name: "testResult",
        label: "TestResult",
        defaults: {
    
        },
        init: function(){
            var levels = ["OK", "WARN", "ERROR", "CRITICAL"],
            compare = function(a,b) {
                return levels.indexOf(a) - levels.indexOf(b);
            };
        
            // Register this for datatables sort
            $.fn.dataTableExt.oSort[this.name+'-asc'] = function(a,b){
                return compare(a,b);
            };
            $.fn.dataTableExt.oSort[this.name+'-desc'] = function(a,b){
                return compare(b,a);
            };
        }, 
        sort: function(a,b){
            return this.sumStrArray(a) - this.sumStrArray(b);
        }, 

        implementation: function (tgt, st, opt) {
    
            // encapsulate this
    
            var $t = $(tgt).empty();
            var result = st.value.split('|');
            $t.parent("tr").addClass('alert' + result[0].toLowerCase());
            var $elem = $("<div></div>").appendTo($t).text(result[0]).attr('title',result[1]);
            if(result.length > 1) {
                $elem.tipsy({
                    gravity: 's', 
                    html:true
                });
            }
        }
    };

Dashboards.registerAddIn("Table", "colType", new AddIn(wd.cdvUI.testResultAddIn));

    $.fn.dataTableExt.oPagination.splitWithDescription = {
        /*
       * Function: oPagination.full_numbers.fnInit
       * Purpose:  Initalise dom elements required for pagination with a list of the pages
       * Returns:  -
       * Inputs:   object:oSettings - dataTables settings object
       *           node:nPaging - the DIV which contains this pagination control
       *           function:fnCallbackDraw - draw function which must be called on update
       */
        "fnInit": function ( oSettings, nPaging, fnCallbackDraw )
        {
            var nList = document.createElement( 'span' );
            var nNext = document.createElement( 'span' );
            var nPrevious = document.createElement( 'span' );
        
            nPrevious.innerHTML = "&nbsp;";
            nNext.innerHTML = "&nbsp;";
        
            var oClasses = oSettings.oClasses;
            nPrevious.className = oClasses.sPageButton+" "+oClasses.sPagePrevious;
            nNext.className= oClasses.sPageButton+" "+oClasses.sPageNext;
        
            nPaging.appendChild( nPrevious );
            nPaging.appendChild( nList );
            nPaging.appendChild( nNext );
        
            $(nPrevious).click( function() {
                if ( oSettings.oApi._fnPageChange( oSettings, "previous" ) )
                {
                    fnCallbackDraw( oSettings );
                }
            } );
        
            $(nNext).click( function() {
                if ( oSettings.oApi._fnPageChange( oSettings, "next" ) )
                {
                    fnCallbackDraw( oSettings );
                }
            } );
        
            /* Take the brutal approach to cancelling text selection */
            $('span', nPaging)
            .bind( 'mousedown', function () {
                return false;
            } )
            .bind( 'selectstart', function () {
                return false;
            } );
        
            /* ID the first elements only */
            if ( oSettings.sTableId !== '' && typeof oSettings.aanFeatures.p == "undefined" )
            {
                nPaging.setAttribute( 'id', oSettings.sTableId+'_paginate' );
                nPrevious.setAttribute( 'id', oSettings.sTableId+'_previous' );
                nList.setAttribute( 'id', oSettings.sTableId+'_list' );
                nNext.setAttribute( 'id', oSettings.sTableId+'_next' );
            }
        },
      
        /*
       * Function: oPagination.full_numbers.fnUpdate
       * Purpose:  Update the list of page buttons shows
       * Returns:  -
       * Inputs:   object:oSettings - dataTables settings object
       *           function:fnCallbackDraw - draw function to call on page change
       */
        "fnUpdate": function ( oSettings, fnCallbackDraw )
        {
            if ( !oSettings.aanFeatures.p )
            {
                return;
            }
        
            var iPageCount = 5;
            var iPageCountHalf = Math.floor(iPageCount / 2);
            var iPages = Math.ceil((oSettings.fnRecordsDisplay()) / oSettings._iDisplayLength);
            var iCurrentPage = Math.ceil(oSettings._iDisplayStart / oSettings._iDisplayLength) + 1;
            var sList = "";
            var iStartButton, iEndButton, i, iLen;
            var oClasses = oSettings.oClasses;
        

            var templ = "Showing entries {{start}}-{{end}} of {{total}}",
            opts = {
                total: oSettings.fnRecordsDisplay(),
                start: oSettings._iDisplayStart + 1,
                end: oSettings._iDisplayStart + oSettings._iDisplayLength
            }
            $('#'+oSettings.sTableId+'_list').text(Mustache.render(templ, opts));
        
        
            /* Loop over each instance of the pager */
            var an = oSettings.aanFeatures.p;
            var anButtons, anStatic, nPaginateList;
            var fnClick = function() {
                /* Use the information in the element to jump to the required page */
                var iTarget = (this.innerHTML * 1) - 1;
                oSettings._iDisplayStart = iTarget * oSettings._iDisplayLength;
                fnCallbackDraw( oSettings );
                return false;
            };
            var fnFalse = function () {
                return false;
            };
        
            for ( i=0, iLen=an.length ; i<iLen ; i++ )
            {
                if ( an[i].childNodes.length === 0 )
                {
                    continue;
                }
          
                /* Build up the dynamic list forst - html and listeners */
                var qjPaginateList = $('span:eq(2)', an[i]);
                qjPaginateList.html( sList );
                $('span', qjPaginateList).click( fnClick ).bind( 'mousedown', fnFalse )
                .bind( 'selectstart', fnFalse );
          
                /* Update the 'premanent botton's classes */
                anButtons = an[i].getElementsByTagName('span');
                $(anButtons[1]).addClass('description');
                anStatic = [
                anButtons[0], anButtons[anButtons.length-1]
                ];
                $(anStatic).removeClass( oClasses.sPageButton+" "+oClasses.sPageButtonActive+" "+oClasses.sPageButtonStaticDisabled );
                if ( iCurrentPage == 1 )
                {
                    anStatic[0].className += " "+oClasses.sPageButtonStaticDisabled;
                }
                else
                {
                    anStatic[0].className += " "+oClasses.sPageButton;
                }
          
                if ( iPages === 0 || iCurrentPage == iPages || oSettings._iDisplayLength == -1 )
                {
                    anStatic[1].className += " "+oClasses.sPageButtonStaticDisabled;
                }
                else
                {
                    anStatic[1].className += " "+oClasses.sPageButton;
                }
            }
        }

    }
