var CNM = CNM || {};

CNM.Geo = {
	supported: !!navigator.geolocation,
	defaultPosition: new google.maps.LatLng(40.75166109, -73.99088802),
	currentPosition: function() {
		var future = new CNM.Future();
		if (!this.supported) {
			future.update(defaultPosition);
		} else {
			navigator.geolocation.getCurrentPosition(function (position) {
				var latlng = new google.maps.LatLng(position.coords.latitude, position.coords.longitude);
				future.update(latlng)
			});
		}
		return future;
	}
};
