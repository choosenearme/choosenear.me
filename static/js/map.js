
$(function(){
  if(navigator.geolocation) 
  {   
     navigator.geolocation.getCurrentPosition(setPosition);
  }

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

     $.ajax({
      url: "/js/exampleJSONP?callback=getDonorsChooseData",
      dataType: 'jsonp',
    }); 

    var markerImage = new google.maps.MarkerImage("https://choosenear.me/marker.png")
      window.handleDonorsChooseData = function(data){
        var proposals = data.proposals.proposals;
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
        return new google.maps.InfoWindow();
      }
      
      function addMarkerClickEvent(marker, proposal){
       google.maps.event.addListener(marker, 'click', function() {
               var infoWindow = getInfoWindow();
               infoWindow.setContent(proposal.title);
               infoWindow.open(map, marker);
       });
      }
  } 
});
