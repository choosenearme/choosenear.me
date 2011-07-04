var CNM = CNM || {};

/**
 * A Future represents a computation that may or may not
 * have happened. It can have waiting functions that execute
 * when the computation happens.
 */
CNM.Future = function() {
    this.done = false;
    this.waiters = [];
};

/**
 * Register a callback that should execute when the Future completes.
 */
CNM.Future.prototype.respond = function(f) {
    if (this.done)
        f(this.result)
    else
        this.waiters.push(f)
};

/**
 * Set the value of the future.
 */
CNM.Future.prototype.update = function(result) {
    if (this.done)
        throw "Future already updated"
    else {
        this.done = true;
        this.result = result;
        for (var i = 0; i < this.waiters.length; i++) {
            this.waiters[i](result);
        }
    }
};

/**
 * Get a function that sets the value of the future.
 * (Damn 'this' pointer.)
 */
CNM.Future.prototype.updateFunction = function() {
    var future = this;
    return function(result) { future.update(result) };
};

CNM.Future.prototype.foreach = CNM.Future.prototype.respond

/**
 * Return a new Future transformed with the given function.
 */
CNM.Future.prototype.map = function(f) {
    var future = new CNM.Future();
    this.respond(function (result) { future.update(f(result)) });
    return future;
};

/**
 * Return a new Future transformed with the given function
 * and then flattened.
 */
CNM.Future.prototype.flatMap = function(f) {
    var future = new CNM.Future();
    this.respond(function (result) { 
        var next = f(result);
        next.respond(function (nextResult) {
            future.update(nextResult);
        });
    });
    return future;
};

/**
 * Pipe the result of this Future into a Channel.
 */
CNM.Future.prototype.pipeTo = function(channel) {
    this.respond(channel.sendFunction());
};
