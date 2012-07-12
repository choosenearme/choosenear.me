$(document).ready( function() {
  var api = new CNM.AnonApi();
  var title, description, image;

  api.project(getProposalId()).foreach(function(project) {
    var proposal = project.proposals[0];
    var secureImageURL = proposal.imageURL.replace("http://cdn.donorschoose.net/",
                                                   "https://choosenear.me/cdn/");
    var donationBaseURL = "https://secure.donorschoose.org/donors/givingCart.html?proposalid=";
    var donationAmount = "&donationAmount=5";
    var donationOptions = "&utm_source=api&utm_medium=feed&utm_content=fundlink&utm_campaign=f8h932pdcukv";
    var donationURL = donationBaseURL + proposal.id + donationAmount + donationOptions;

    $("img").attr({
      src: secureImageURL
    });
    $("h2").text(proposal.title);
    $("p").text(proposal.shortDescription);
    $(".proposal-webview-button").attr('href', donationURL);
  });

  function getProposalId(){
    return window.location.hash.substr(2); 
  }

});
