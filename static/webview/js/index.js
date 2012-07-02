$(document).ready( function() {
  var api = new CNM.AnonApi();
  var title, description, image;

  api.project(772762).foreach(function(project) {
    var proposal = project.proposals[0];

    $("img").attr({
      src: proposal.imageURL
    });
    $("h2").text(proposal.title);
    $("p").text(proposal.shortDescription);
  });
});
