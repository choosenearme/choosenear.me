var CNM = CNM || {};

CNM.Channel = function() {
	this.observers = [];
};

CNM.Channel.prototype.send = function(msg) {
	for (var i = 0; i < this.observers.length; i++) {
		this.observers[i](msg);
	}
};

CNM.Channel.prototype.sendFunction = function() {
	var channel = this;
	return function(msg) {
		channel.send(msg);
	};
};

CNM.Channel.prototype.respond = function(f) {
	this.observers.push(f);
};

CNM.Channel.prototype.foreach = CNM.Channel.prototype.respond

CNM.Channel.prototype.map = function(f) {
	var channel = new CNM.Channel();
	this.respond(function(msg) {
		channel.send(f(msg));
	});
	return channel;
};

CNM.Channel.prototype.flatMap = function(f) {
	var channel = new CNM.Channel();
	this.respond(function (msg) {
		var next = f(msg);
		next.respond(function(nextMsg) {
			channel.send(nextMsg);
		});
	})
	return channel;
};

CNM.Channel.prototype.pipeTo = function(that) {
	this.respond(function (msg) {
		that.send(msg);
	});
};
