$(function() {
    var auth = getUrlVars()['secret'];

    if (supports_html5_storage()) {
        if (auth) {
            window.localStorage['secret'] = auth;
            window.location = '/index2.html';
        } else {
            auth = window.localStorage['secret'];
        }
    }

    // The choosenear.me API
    var api = (auth) ? new CNM.Api(auth) : new CNM.AnonApi();

    // The google map and it's info window
    var map = new CNM.Map(document.getElementById('map_canvas'), CNM.Geo.defaultPosition);
    var infoWindow = new google.maps.InfoWindow();

    // A channel that gets updated whenever the current position marker is dragged.
    var draggedMarker =
        (map
            .currentPositionChannels
            .dragEnds()
            .map(function(marker) { return marker.getPosition(); }))

    CNM.Geo.currentPosition().foreach(function (latlng) {
        // To start, pretend we've dragged the marker
        draggedMarker.send(latlng);
    });

    // A channel that gets updated whenever we should display
    // new DonorsChoose proposals.
    var proposalChanges = new CNM.Channel();

    // Whenever the current position marker is dragged to a new
    // location, find some DonorsChoose proposals nearby.
    draggedMarker.foreach(function (latlng) {
        api.location(latlng.lat(), latlng.lng()).pipeTo(proposalChanges);
    });

    // A channel that gets updated every time a marker is clicked
    var markerClicks = new CNM.Channel();

    // Every time the proposals change, add markers to the map
    proposalChanges.foreach(function (proposals) {
        map.clearMarkers();
        proposals.map(function (proposal) {
            var lat = parseFloat(proposal.latitude, 10);
            var lng = parseFloat(proposal.longitude, 10);
            var latlng = new google.maps.LatLng(lat, lng)
            var icon =
                (proposal.matchesCheckin) ?
                    new google.maps.MarkerImage("/images/recommended-marker.png") :
                    new google.maps.MarkerImage("/images/marker.png");

            var clicks = map.addProposalMarker(latlng, proposal.title, icon).clicks();
            (clicks
                .map(function(marker) { return new CNM.Tuple(marker, proposal); })
                .pipeTo(markerClicks));
        });
    });

    // Every time a marker is clicked, pop the info window
    markerClicks.foreach(function (marker, proposal) {
        var tmpl = $("#info-bubble-template").tmpl(proposal);
        var thingy = $("<div/>").append(tmpl)
        var content = thingy.html();
        infoWindow.close();
        infoWindow.setContent("");
        infoWindow.setContent(content);
        infoWindow.open(map.map, marker);
        $(".proposal-link").click(function(event) {
            event.preventDefault();
            var link = $(this);
            $("#proposal-info").html($("#more-information-template").tmpl(proposal));
            $.mobile.changePage("#more-information");
        });
     });

    if (auth) {
        var cityChanges = new CNM.Channel();

        api.cities().foreach(function (cities) {
            cities.forEach(function (city) {
                $("#cities").append("<option value='"+city.lat+","+city.lng+"'>"+city.name+"</option>");
            });
            $(".ui-select").css("display", "block");
            $(".ui-btn-text").append("choose your city");

            if (cities[0])
                cityChanges.send(cities[0].lat, cities[0].lng);
        })

        $("#cities").change(function(){
            var el = $(this);
            var latlng = $(this).val().split(',');
            var lat = parseFloat(latlng[0]);
            var lng = parseFloat(latlng[1]);

            cityChanges.send(lat, lng);
        });

        cityChanges.foreach(function(lat, lng) {
            var latlng = new google.maps.LatLng(lat, lng);
            map.setCurrentPosition(latlng);
            draggedMarker.send(latlng);
            api.city(lat, lng).foreach(function (checkins) {
                $("#map_canvas").css("width","650px");
                $("#check-in-info").empty();
                $("#check-in-info").append("<p data-lng='"+lng+"' data-lat='"+lat+"'>Current Position</p>");
                checkins.forEach(function (checkin) {
                    var recommended =
                        ((checkin.matchingProposals || []).length > 0) ?
                            "<br /><span class=\"recommended\">&rarr; recommended projects</span>" : "";
                    ($("#check-in-info")
                        .append(
                            "<p class='"+checkin.id+"' data-lng='"+checkin.lng+"' data-lat='"+checkin.lat+"'>"+
                            checkin.venuename+"<br /><span class=\"crossStreet\">"+(checkin.crossStreet || "")+
                            "</span>"+recommended+"</p>"));
                });
            });
        });

        $("#check-in-info p").live("click",function(){
            var el = $(this);
            var lat = parseFloat(el.data("lat"));
            var lng = parseFloat(el.data("lng"));
            var checkinId = el.attr("class");
            map.setCurrentPosition(new google.maps.LatLng(lat, lng));
            if (checkinId)
                api.checkin(checkinId).pipeTo(proposalChanges);
        });
    }
});
