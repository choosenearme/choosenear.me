var CNM = CNM || {};

CNM.MarkerChannels = function(marker) {
    this.marker = marker;
};

CNM.MarkerChannels.prototype.channelForEvent = function(eventName) {
    var channel = new CNM.Channel();
    var marker = this.marker;
    google.maps.event.addListener(marker, eventName, function() {
        channel.send(marker);
    });
    return channel;
};

CNM.MarkerChannels.prototype.clicks = function() { return this.channelForEvent('click'); };

CNM.MarkerChannels.prototype.dragEnds = function() { return this.channelForEvent('dragend'); };
