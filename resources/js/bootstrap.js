lib("lib/json2.js");
lib("lib/underscore.js");
lib("lib/timers.js");
lib("lib/later.js");
lib("lib/wd.js");
lib("scheduler.js");
lib("cdv.js");
lib("cda.js");

cdv = wd.cdv.cdv();
loadTests();

var timerUp = true;

//scheduler.scheduleTask(function(){print("Every 7!")},"0/7 * * * * *");
//scheduler.scheduleTask(function(){print("Every 5!")},"1-59/5 * * * * *");

registerHandler("GET", "/restartTimer", function(out){
  this.setOutputType(this.MIME_TEXT);
  timerUp = true;
  scheduler.restart();
  out.write(new java.lang.String("Timer turned on").getBytes("utf-8"));
});

registerHandler("GET", "/stopTimer", function(out){
  try { 
  this.setOutputType(this.MIME_TEXT);
  timerUp = false;
  print("Turning timer off");
  scheduler.pause();
  out.write(new java.lang.String("Timer turned off").getBytes("utf-8"));
  print("Timer turned off");

 } catch(e) {
  print(e);
 }
});

registerHandler("GET", "/listTests", function(out){
  try { 
    this.setOutputType(this.MIME_JSON);
    out.write(new java.lang.String(JSON.stringify(cdv.listTests())).getBytes("utf-8"));
  } catch(e) {
    print(e);
  }
});
