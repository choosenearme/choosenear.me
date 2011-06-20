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
        $("#map").live("pagecreate", function(event){
                if(navigator.geolocation) 
                {   
                    navigator.geolocation.getCurrentPosition(setPosition);
                }
            });
        function setPosition(position) 
        {
            var latitude = position.coords.latitude;
            var longitude = position.coords.longitude;
            var myLatlng = new google.maps.LatLng(latitude, longitude);
            var myOptions = {
                zoom: 12,
                center: myLatlng,
                mapTypeId: google.maps.MapTypeId.ROADMAP
            };
            var map = new google.maps.Map(document.getElementById("map_canvas"),
                myOptions);
            var marker = new google.maps.Marker({
                    position: myLatlng, 
                    map: map, 
                    title:"This is where you are",
                });   

            var jsonpUrl = "/api/location?latlng="+latitude+","+longitude+"&callback=handleDonorsChooseData";
            var script = document.createElement("script");
            script.src = jsonpUrl;
            document.getElementsByTagName("head")[0].appendChild(script);

            var markerImage = new google.maps.MarkerImage("https://choosenear.me/marker.png")
            var infoWindow = new google.maps.InfoWindow();
            window.handleDonorsChooseData = function(data){
                var proposals = window.proposals = data.proposals.proposals;
                for(var i = 0, len = proposals.length;i<len;i++){
                    var proposal = proposals[i];
                    var proposalLatLng = new google.maps.LatLng(parseFloat(proposal.latitude, 10), parseFloat(proposal.longitude, 10));
                    var marker = new google.maps.Marker({
                            position: proposalLatLng, 
                            map: map, 
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
                        var content = "<a class='proposal-link' onclick=''>"+proposal.title +"</a>";
                        infoWindow.setContent(content);
                        infoWindow.open(map, marker);
                        $(".proposal-link").click(proposalClickLinkHandler);
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
            google.maps.event.addListener(map,'tilesloaded',scrollToMap);

            function scrollToMap(){
                $.mobile.silentScroll($("#map-canvas-container").position().top);
            }
        } 
    });
