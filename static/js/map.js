if(typeof CNM == "undefined"){
    CNM = {};
    CNM.markers = [];
    CNM.currentPosition = new google.maps.LatLng(40.75166109, -73.99088802);
    CNM.markers.clear = function(){
        for(var i = 0, len = this.length; i<len;i++){
            var marker = this[i];
            marker.setMap(null);
        }
    }
}

navigator.geolocation.getCurrentPosition(function(position){ 
        CNM.currentPosition = new google.maps.LatLng(position.coords.latitude, position.coords.longitude)
    });

var createMap = function(){

    var myOptions = {
        zoom: 12,
        center:CNM.currentPosition,
        mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    CNM.map = new google.maps.Map(document.getElementById("map_canvas"),
        myOptions);
}

$(function(){
        if(getUrlVars()["secret"] != undefined){
            $.getJSON("/api/checkins?secret="+getUrlVars()["secret"], function(data){
                    var checkins = data.response.response.checkins.items;
                    for(var i = 0, len = checkins.length;i<len;i++){
                        var venueLocation = checkins[i].venue.location;
                        $("#check-in-info").append("<p data-lng='"+venueLocation.lng+"' data-lat='"+venueLocation.lat+"'>"+checkins[i].venue.name+"</p>");
                    }
                    $("#check-in-info p").live("click",function(){
                            var el = $(this);
                            var lat = parseFloat(el.data("lat"));
                            var lng = parseFloat(el.data("lng"));
                            CNM.currentPosition = new google.maps.LatLng(lat, lng);
                            window.setMapPosition();
                        });
                });
        }
        createMap();
        $("#map").live("pageshow", function(event){
                window.setMapPosition();
            });
        $("#map").live("pagebeforeshow", function(event){
                CNM.markers.clear();
            });
        $("#more-information").live("pagebeforeshow", function(event){
                if($("#more-information").find("h2").length == 0){
                    loadProposals();
                    var linkText = localStorage.getItem("lastProposalLinkText");
                    var currentProposal = window.proposals.filter(function(proposal){
                            return proposal.title == linkText; 
                        });
                    $("#proposal-info").html($("#more-information-template").tmpl(currentProposal));
                }
            });
        window.setMapPosition = function setMapPosition() 
        {
            CNM.markers.clear();
            var marker = new google.maps.Marker({
                    position: CNM.currentPosition, 
                    map: CNM.map, 
                    title:"This is where you are",
                });   
            CNM.markers.push(marker);
            google.maps.event.trigger(CNM.map, 'resize');
            CNM.map.setCenter(CNM.currentPosition);
            var jsonpUrl = "/api/location?latlng="+CNM.currentPosition.lat()+","+CNM.currentPosition.lng()+"&callback=handleDonorsChooseData";
            var script = document.createElement("script");
            script.src = jsonpUrl;
            document.getElementsByTagName("head")[0].appendChild(script);

            var markerImage = new google.maps.MarkerImage("/images/marker.png")
            var infoWindow = new google.maps.InfoWindow();

            window.handleDonorsChooseData = function(data){
                var proposals = window.proposals = data.proposals.proposals;
                saveProposals(proposals);
                for(var i = 0, len = proposals.length;i<len;i++){
                    var proposal = proposals[i];
                    var proposalLatLng = new google.maps.LatLng(parseFloat(proposal.latitude, 10), parseFloat(proposal.longitude, 10));
                    var marker = new google.maps.Marker({
                            position: proposalLatLng, 
                            map: CNM.map, 
                            animation: google.maps.Animation.DROP,
                            title:proposal.title,
                            icon:markerImage
                        });
                    CNM.markers.push(marker);
                    addMarkerClickEvent(marker, proposal);
                }
            }

            function getInfoWindow(){
                infoWindow.close();
                infoWindow.setContent("");
                return infoWindow;
            }

            function addMarkerClickEvent(marker, proposal){
                google.maps.event.addListener(marker, 'click', function() {
                        var infoWindow = getInfoWindow();
                        var content = $("<div/>").append($("#info-bubble-template").tmpl(proposal)).html();
                        infoWindow.setContent(content);
                        infoWindow.open(CNM.map, marker);
                        $(".proposal-link").click(proposalClickLinkHandler);
                    });
            }

            function proposalClickLinkHandler(event){
                event.preventDefault();
                var link = $(this);
                localStorage.setItem("lastProposalLinkText", link.html());
                var currentProposal = window.proposals.filter(function(proposal){
                        return proposal.title == link.html(); 
                    });
                $("#proposal-info").html($("#more-information-template").tmpl(currentProposal));
                $.mobile.changePage("#more-information");
            }

        } 
    });

function saveProposals(proposals){
    if('localStorage' in window && window['localStorage'] !== null){
        localStorage.setItem("proposals", JSON.stringify(proposals));
    }
}
function loadProposals(){
    if('localStorage' in window && window['localStorage'] !== null){
        window.proposals = localStorage.getItem("proposals");
        window.proposals = JSON.parse(window.proposals);
    }
}
