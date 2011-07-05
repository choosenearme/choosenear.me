var CNM = CNM || {};

CNM.Channel = function() {
	this.observers = [];
};

CNM.Channel.prototype.send = function() {
	for (var i = 0; i < this.observers.length; i++) {
		this.observers[i].apply(this, arguments);
	}
};

CNM.Channel.prototype.sendFunction = function() {
	var channel = this;
	return function() {
		channel.send.apply(channel, arguments);
	};
};

CNM.Channel.prototype.respond = function(f) {
	this.observers.push(f);
};

CNM.Channel.prototype.foreach = CNM.Channel.prototype.respond

CNM.Channel.prototype.map = function(f) {
	var channel = new CNM.Channel();
	this.respond(function() {
		var result = f.apply(this, arguments);
		var args = (result instanceof CNM.Tuple) ? result.args : [result];
		channel.send.apply(channel, args);
	});
	return channel;
};

CNM.Channel.prototype.flatMap = function(f) {
	var channel = new CNM.Channel();
	this.respond(function () {
		var next = f.apply(this, arguments);
		next.respond(function() {
			channel.send.apply(channel, arguments);
		});
	})
	return channel;
};

CNM.Channel.prototype.pipeTo = function(that) {
	this.respond(function () {
		that.send.apply(that, arguments);
	});
};
