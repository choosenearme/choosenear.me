if(typeof CNM == "undefined"){
    CNM = {};
}

var createMap = function(){

    var myOptions = {
        zoom: 12,
        center:new google.maps.LatLng(41,87),
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
                        $("#check-in-info").append("<p>"+checkins[i].venue.name+"</p>");
                        $.mobile.changePage("check-ins");
                    }
                });
        }
        createMap();
        $("#map").live("pageshow", function(event){
                if(navigator.geolocation) 
                {   
                    navigator.geolocation.getCurrentPosition(setPosition);
                }
            });
        $("#map").live("pageshow", function(event){
                google.maps.event.trigger(CNM.map, 'resize');
            });
        function setPosition(position) 
        {
            var latitude = position.coords.latitude;
            var longitude = position.coords.longitude;
            console.log(latitude, longitude);
            var myLatlng = new google.maps.LatLng(latitude, longitude);
            CNM.map.setCenter(myLatlng);
            var marker = new google.maps.Marker({
                    position: myLatlng, 
                    map: CNM.map, 
                    title:"This is where you are",
                });   

            var jsonpUrl = "/api/location?latlng="+latitude+","+longitude+"&callback=handleDonorsChooseData";
            var script = document.createElement("script");
            script.src = jsonpUrl;
            document.getElementsByTagName("head")[0].appendChild(script);

            var markerImage = new google.maps.MarkerImage("/images/marker.png")
            var infoWindow = new google.maps.InfoWindow();
            window.handleDonorsChooseData = function(data){
                var proposals = window.proposals = data.proposals.proposals;
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
                        console.log($(".proposal-link").click(proposalClickLinkHandler));
                    });
            }

            function proposalClickLinkHandler(event){
                event.preventDefault();
                var link = $(this);
                var currentProposal = window.proposals.filter(function(proposal){
                        return proposal.title == link.html(); 
                    });
                $.mobile.changePage("more-information");
                $("#proposal-info").html($("#more-information-template").tmpl(currentProposal));
            }
            google.maps.event.addListener(CNM.map,'tilesloaded',scrollToMap);

            function scrollToMap(){
                $.mobile.silentScroll($("#map-canvas-container").position().top);
            }
        } 
    });
