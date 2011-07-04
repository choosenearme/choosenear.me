var CNM = CNM || {};

CNM.Map = function(elem, defaultPosition) {
    this.currentPosition = defaultPosition;
    this.markers = [];
    var options = {
        zoom: 12,
        center: this.currentPosition,
        mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    this.map = new google.maps.Map(elem, options);
};

CNM.Map.prototype.clearMarkers = function() {
    this.markers.map(function (marker) {
        marker.setMap(null);
    });
    this.markers = [];
};

CNM.Map.prototype.addMarker = function(properties) {
    var markerSettings = $.extend({
        map: this.map
    }, properties);
    var marker = new google.maps.Marker(markerSettings);
    this.markers.push(marker);
    return new CNM.MarkerChannels(marker);
};

CNM.Map.prototype.addCurrentPositionMarker = function(latlng) {
    // We don't reuse addMarker because we don't want
    // this marker to be cleared when we clear the proposals.
    var marker = new google.maps.Marker({
        position: latlng,
        title: "This is where you are",
        draggable: true,
        map: this.map
    });
    return new CNM.MarkerChannels(marker);
};

CNM.Map.prototype.addDCMarker = function(latlng, title) {
    return this.addMarker({
        position: latlng,
        title: title,
        icon: new google.maps.MarkerImage("/images/marker.png")
    });
};

CNM.Map.prototype.addHighlightedDCMarker = function(latlng, title) {
    return this.addMarker({
        position: latlng,
        title: title,
        icon: new google.maps.MarkerImage("/images/recommended-marker.png")
    });
};
