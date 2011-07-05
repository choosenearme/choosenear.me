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
        f.apply(this, this.result)
    else
        this.waiters.push(f)
};

/**
 * Set the value of the future.
 */
CNM.Future.prototype.update = function() {
    if (this.done)
        throw "Future already updated"
    else {
        this.done = true;
        this.result = arguments;
        for (var i = 0; i < this.waiters.length; i++) {
            this.waiters[i].apply(this, this.result);
        }
    }
};

/**
 * Get a function that sets the value of the future.
 * (Damn 'this' pointer.)
 */
CNM.Future.prototype.updateFunction = function() {
    var future = this;
    return function() { future.update.apply(future, arguments) };
};

CNM.Future.prototype.foreach = CNM.Future.prototype.respond

/**
 * Return a new Future transformed with the given function.
 */
CNM.Future.prototype.map = function(f) {
    var future = new CNM.Future();
    this.respond(function() {
        var result = f.apply(this, arguments);
        var args = (result instanceof CNM.Tuple) ? result.args : [result];
        future.update.apply(future, args);
    });
    return future;
};

/**
 * Return a new Future transformed with the given function
 * and then flattened.
 */
CNM.Future.prototype.flatMap = function(f) {
    var future = new CNM.Future();
    this.respond(function() {
        var next = f.apply(this, arguments);
        next.respond(function() {
            future.update.apply(future, arguments);
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
