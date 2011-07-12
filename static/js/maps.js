var CNM = CNM || {};

CNM.Map = function(elem, defaultPosition) {
    this.markers = [];
    var options = {
        zoom: 12,
        center: defaultPosition,
        mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    this.map = new google.maps.Map(elem, options);

    this.currentPositionMarker = new google.maps.Marker({
        position: defaultPosition,
        title: "This is where you are",
        draggable: true,
        map: this.map
    });
    this.currentPositionChannels = new CNM.MarkerChannels(this.currentPositionMarker);
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

CNM.Map.prototype.setCurrentPosition = function(latlng) {
    this.map.setCenter(latlng);
    this.currentPositionMarker.setPosition(latlng);
};

CNM.Map.prototype.addProposalMarker = function(latlng, title, icon) {
    return this.addMarker({
        position: latlng,
        title: title,
        icon: icon
    });
};
