var CNM = CNM || {};

CNM.AnonApi = function() { };

CNM.AnonApi.prototype.get = function(endpoint, params) {
    var future = new CNM.Future();
    $.getJSON(endpoint, params, function(response) { future.update(response); });
    return future;
};

CNM.AnonApi.prototype.location = function(lat, lng) {
    return (
        this.get("/api/location", { "latlng" : lat+","+lng })
            .map(function (response) { return response.proposals.proposals; }));
};

CNM.AnonApi.prototype.ping = function() {
    return this.get("/api/ping", {});
};

CNM.Api = function(secret) {
    this.secret = secret;
};

CNM.Api.prototype = $.extend({}, CNM.AnonApi.prototype)

CNM.Api.prototype.categories = function() {
    return this.get("/api/categories", { "secret" : this.secret });
};

CNM.Api.prototype.checkin = function(checkinId) {
    return (
        this.get("/api/checkin", { "secret" : this.secret, "checkinId" : checkinId })
            .map(function (response) { return response.proposals.proposals; }));
};

CNM.Api.prototype.checkins = function() {
    return this.get("/api/checkins", { "secret" : this.secret });
};

CNM.Api.prototype.cities = function() {
    return (
        this.get("/api/cities", { "secret" : this.secret })
            .map(function (json) { return json.response }));
};

CNM.Api.prototype.city = function(lat, lng) {
    return (
        this.get("/api/city", { "secret" : this.secret, "latlng" : lat+","+lng })
            .map(function (json) { return json.response.checkins }));
};

CNM.Api.prototype.user = function() {
    return this.get("/api/user", { "secret" : this.secret });
};
