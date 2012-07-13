var setTimeout,
    clearTimeout,
    setInterval,
    clearInterval;

(function (global) {
    var timer = new java.util.Timer();
    var counter = 1; 
    var ids = {};

    setTimeout = function (fn,delay) {
        var id = counter++;
        ids[id] = new JavaAdapter(java.util.TimerTask,{run: function(){
          fn.apply(global, Array.prototype.slice.call(arguments,2)); 
        }});
        timer.schedule(ids[id],delay);
        return id;
    }

    clearTimeout = function (id) {
        if(ids[id]) {
          ids[id].cancel();
        }
        timer.purge();
        delete ids[id];
    }

    setInterval = function (fn,delay) {
        var id = counter++; 
        ids[id] = new JavaAdapter(java.util.TimerTask,{run: function(){
          fn.apply(global, Array.prototype.slice.call(arguments,2));
        }});
        timer.schedule(ids[id],delay,delay);
        return id;
    }

    clearInterval = clearTimeout;

})(this);


