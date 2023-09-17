$(function() {

  $('.js-check-all').on('click', function() {

  	if ( $(this).prop('checked') ) {
	  	$('th input[type="checkbox"]').each(function() {
	  		$(this).prop('checked', true);
	  	})
  	} else {
  		$('th input[type="checkbox"]').each(function() {
	  		$(this).prop('checked', false);
	  	})
  	}
  });
});


let back = document.getElementById("stop");

back.addEventListener("click", ()=>{
	history.pushState(null, null, "http://localhost:8080");
	window.onpopstate = function(event) {
		history.go(1);
	};
});