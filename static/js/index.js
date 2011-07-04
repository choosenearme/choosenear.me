$(function() {
    // The choosenear.me API
    var anonApi = new CNM.AnonApi();

    // The google map and it's info window
    var map = new CNM.Map(document.getElementById('map_canvas'), CNM.Geo.defaultPosition);
    var infoWindow = new google.maps.InfoWindow();

    // A channel that gets updated whenever the position changes
    var positionChanges = new CNM.Channel();
    CNM.Geo.currentPosition().foreach(function (latlng) {
        // To start, update the channel with the current position
        positionChanges.send(latlng);

        // Also add a current position marker, and update the channel
        // whenever the marker is dragged.
        (map.addCurrentPositionMarker(latlng)
            .dragEnds()
            .map(function(marker) { return marker.getPosition(); })
            .pipeTo(positionChanges));
    });

    // When the position changes, recenter the map
    positionChanges.foreach(function (latlng) {
        map.map.setCenter(latlng);
        map.currentPosition = latlng;
    });

    // When the position changes, grab new DonorsChoose proposals
    // and send those changes to its own channel.
    var proposalChanges = positionChanges.flatMap(function (latlng) {
        return anonApi.location(latlng.lat(), latlng.lng());
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
            var clicks = map.addDCMarker(latlng, proposal.title).clicks();
            (clicks
                .map(function(marker) { return { marker: marker, proposal: proposal }; })
                .pipeTo(markerClicks));
        });
    });

    // Every time a marker is clicked, pop the info window
    markerClicks.foreach(function (payload) {
        var proposal = payload.proposal;
        var marker = payload.marker;

        var tmpl = $("#info-bubble-template").tmpl(proposal);
        var content = $("<div/>").append(tmpl).html();
        infoWindow.close();
        infoWindow.setContent(content);
        infoWindow.open(map.map, marker);
    });
});


// $(function(){
//         if(getUrlVars()["secret"] != undefined){
//             $.getJSON("/api/cities?secret="+getUrlVars()["secret"], function(data){
//                 var cities = data.response;
//                 for(var i = 0, len = cities.length;i<len;i++){
//                     var city = cities[i];
//                     $("#cities").append("<option value='"+city.lat+","+city.lng+"'>"+city.name+"</option>");
//                 }
//                 $(".ui-select").css("display", "block");
//                 $(".ui-btn-text").append("choose your city");

//                 var fetchCity = function(latlng) {
//                     $.getJSON("/api/city?secret="+getUrlVars()["secret"]+"&latlng="+latlng, function(data){
//                         $("#map_canvas").css("width","650px");

//                         var ll = latlng.split(',');
//                         var lat = parseFloat(ll[0]);
//                         var lng = parseFloat(ll[1]);
//                         CNM.currentPosition = new google.maps.LatLng(lat, lng)
//                         window.setMapPosition()

//                         $("#check-in-info").empty();
//                         var checkins = data.response.checkins;
//                         $("#check-in-info").append("<p data-lng='"+CNM.currentPosition.lng()+"' data-lat='"+CNM.currentPosition.lat()+"'>Current Position</p>");
//                         for(var i = 0, len = checkins.length;i<len;i++){
//                             var checkin = checkins[i];
//                             var recommended = ""
//                             if ((checkin.matchingProposals || []).length > 0) {
//                                 recommended = "<br /><span class=\"recommended\">&rarr; recommended projects</span>"
//                             }
//                             $("#check-in-info").append("<p class='"+checkin.id+"' data-lng='"+checkin.lng+"' data-lat='"+checkin.lat+"'>"+checkin.venuename+
//                               "<br /><span class=\"crossStreet\">"+(checkin.crossStreet || "")+"</span>"+recommended+"</p>");
//                         }
//                     });
//                 }

//                 var firstCity = cities[0];
//                 fetchCity(firstCity.lat+","+firstCity.lng);
//                 $("#check-in-info p").live("click",function(){
//                     var el = $(this);
//                     var lat = parseFloat(el.data("lat"));
//                     var lng = parseFloat(el.data("lng"));
//                     var checkinId = el.attr("class");
//                     CNM.currentPosition = new google.maps.LatLng(lat, lng);
//                     window.setMapPosition(checkinId);
//                 });
//                 $("#cities").change(function(){
//                     var el = $(this);
//                     var latlng = $(this).val()
//                     fetchCity(latlng)
//                 });
//             });
//         }
//         createMap();
//         $("#map").live("pageshow", function(event){
//                 window.setMapPosition();
//             });
//         $("#map").live("pagebeforeshow", function(event){
//                 CNM.markers.clear();
//             });
//         $("#more-information").live("pagebeforeshow", function(event){
//                 if($("#more-information").find("h2").length == 0){
//                     loadProposals();
//                     var linkText = localStorage.getItem("lastProposalLinkText");
//                     var currentProposal = window.proposals.filter(function(proposal){
//                             return proposal.title == linkText; 
//                         });
//                     $("#proposal-info").html($("#more-information-template").tmpl(currentProposal));
//                 }
//             });
//         window.setMapPosition = function setMapPosition(checkinId) 
//         {
//             CNM.markers.clear();
//             var marker = new google.maps.Marker({
//                     position: CNM.currentPosition,
//                     map: CNM.map, 
//                     title:"This is where you are",
//                 });   
//             CNM.markers.push(marker);
//             google.maps.event.trigger(CNM.map, 'resize');
//             CNM.map.setCenter(CNM.currentPosition);
//             var jsonpUrl = "/api/location?latlng="+CNM.currentPosition.lat()+","+CNM.currentPosition.lng()+"&callback=handleDonorsChooseData";
//             if (getUrlVars()["secret"] && checkinId){
//               jsonpUrl = "/api/checkin?secret="+getUrlVars()["secret"]+"&checkinId="+checkinId+"&callback=handleDonorsChooseData";
//             }
//             var script = document.createElement("script");
//             script.src = jsonpUrl;
//             document.getElementsByTagName("head")[0].appendChild(script);

//             var infoWindow = new google.maps.InfoWindow();

//             window.handleDonorsChooseData = function(data){
//                 proposals = window.proposals = data.proposals.proposals;
//                 saveProposals(proposals);
//                 var markerImage = "";
//                 for(var i = 0, len = proposals.length;i<len;i++){
//                     var proposal = proposals[i];
//                     if (proposal.matchesCheckin) {
//                       markerImage = new google.maps.MarkerImage("/images/recommended-marker.png")
//                     } else {
//                       markerImage = new google.maps.MarkerImage("/images/marker.png")
//                     }
//                     var proposalLatLng = new google.maps.LatLng(parseFloat(proposal.latitude, 10), parseFloat(proposal.longitude, 10));
//                     var marker = new google.maps.Marker({
//                             position: proposalLatLng, 
//                             map: CNM.map, 
//                             animation: google.maps.Animation.DROP,
//                             title:proposal.title,
//                             icon:markerImage
//                         });
//                     CNM.markers.push(marker);
//                     addMarkerClickEvent(marker, proposal);
//                 }
//             }

//             function getInfoWindow(){
//                 infoWindow.close();
//                 infoWindow.setContent("");
//                 return infoWindow;
//             }

//             function addMarkerClickEvent(marker, proposal){
//                 google.maps.event.addListener(marker, 'click', function() {
//                         var infoWindow = getInfoWindow();
//                         var content = $("<div/>").append($("#info-bubble-template").tmpl(proposal)).html();
//                         infoWindow.setContent(content);
//                         infoWindow.open(CNM.map, marker);
//                         $(".proposal-link").click(proposalClickLinkHandler);
//                     });
//             }

//             function proposalClickLinkHandler(event){
//                 event.preventDefault();
//                 var link = $(this);
//                 localStorage.setItem("lastProposalLinkText", link.html());
//                 var currentProposal = window.proposals.filter(function(proposal){
//                         return proposal.title == link.html(); 
//                     });
//                 $("#proposal-info").html($("#more-information-template").tmpl(currentProposal));
//                 $.mobile.changePage("#more-information");
//             }

//         } 
//     });
    
// function getCheckinsFromLocation(latlng){
//   $.getJSON("/api/checkins?secret="+getUrlVars()["secret"]+"&latlng="+latlng, function(data){
//       return data.response.response.checkins.items; 
//   });
// }

// function getProposalsFromCity(latlng){
//   $.getJSON("/api/city?secret="+getUrlVars()["secret"]+"&latlng="+latlng, function(data){
//       return data.response.proposals; 
//   });
// }

// function saveProposals(proposals){
//     if('localStorage' in window && window['localStorage'] !== null){
//         localStorage.setItem("proposals", JSON.stringify(proposals));
//     }
// }
// function loadProposals(){
//     if('localStorage' in window && window['localStorage'] !== null){
//         window.proposals = localStorage.getItem("proposals");
//         window.proposals = JSON.parse(window.proposals);
//     }
// }
