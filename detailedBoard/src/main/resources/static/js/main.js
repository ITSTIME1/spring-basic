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

let loginTag = document.getElementById("loginTag");
let profile = document.getElementById("profile");
let logOut = document.getElementById("logout");
// 쿠키
let cookie = document.cookie;
console.log(cookie);
loginTag.innerText = cookie.length > 0 ? "로그인 중" : "로그인";


// 로그인을 눌러을때
loginTag.addEventListener("click", (event) => {

    console.log(cookie.length);
	// null이 아니라면 로그인중인거고
	// 대신 cookike가 null이거나 빈 문자열이라면 로그인할 수 있지.
	if(cookie.length > 0){
		alert("로그인 중입니다.");
	}

	if(cookie.length !== 0) {
		event.preventDefault();
	}
});

// 로그아웃을 눌렀을때
