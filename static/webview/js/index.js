$(document).ready( function() {
  var api = new CNM.AnonApi();
  var title, description, image;

  api.project(772762).foreach(function(project) {
    var proposal = project.proposals[0];
    var secureImageURL = proposal.imageURL.replace("http://cdn.donorschoose.net/",
                                                   "https://choosenear.me/cdn/");
    $("img").attr({
      src: secureImageURL
    });
    $("h2").text(proposal.title);
    $("p").text(proposal.shortDescription);
  });
});
