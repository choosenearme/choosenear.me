
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
              zoom: 16,
                center: myLatlng,
                  mapTypeId: google.maps.MapTypeId.ROADMAP
          };
          var map = new google.maps.Map(document.getElementById("map_canvas"),
              myOptions);
          var marker = new google.maps.Marker({
              position: myLatlng, 
              map: map, 
              title:"Hello World!"
          });   
  }
});
