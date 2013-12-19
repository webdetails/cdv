(function(global){

  var myself = {},
      _timer = null,
      _queue = [];

  function handleTimeout(){
    var now = Date.now(),
        task;
    while (_queue[0].next < now) {
      task = _queue.shift();
      try {
      task.handler();
      } catch (e) {
      }
      addTask(task);
    }
    reschedule();
  }
  
  myself.scheduleTask = function(task, schedule, group, name) {
    var taskObj;
    if (arguments.length == 1) {
      taskObj = task;
    } else {
      schedule = typeof schedule == "string" ? cronParser().parse(schedule, true) : schedule;
      taskObj = {
        handler: function(){callWithDefaultSession(task);},
        schedule: schedule,
        group: group,
        name: name
      };
    }
    addTask(taskObj);
    reschedule();
  };

  function addTask(task) {
    task.next = later(1,true).getNext(task.schedule, new Date(Date.now() + 1000));
    if (_queue.length === 0) {
      _queue.unshift(task);
    } else {
      var idx = 0;
      var next = task.next;
      while(_queue[idx] && (_queue[idx].next < next)) {
        idx += 1;
      }
      _queue.splice(idx, 0, task);
    }
  };

  function reschedule(){
    var now = Date.now(),
        head = myself.peek(),
        timeout = head.next - now;
    timeout = timeout < 100 ? 100 : timeout;
    clearTimeout(_timer);
    _timer = setTimeout(handleTimeout, timeout);
  }

  myself.peek = function() {
    return _queue[0];
  };

  myself.getCron = function(cronExpr) {
    return cron().parse(cronExpr,true);
  };

  myself.pause = function() {
    clearTimeout(_timer);
    _timer = null;
  };

  myself.restart = function() {
    if (_timer === null) {
      reschedule();
    }
  };

  myself.reset = function() {
    myself.pause();
    _queue = [];
  };

  myself.listSchedules = function() {
    return _(_queue).map(function(q){return {group: q.group, name: q.name, next: q.next};});
  }
  global.scheduler = myself;
}(this));
