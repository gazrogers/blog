$(document).ready(function()
{
	$('#signin').click(function(){navigator.id.request(); return false;});
	$('#signout').click(function(){navigator.id.logout(); return false;});
	navigator.id.watch({
		loggedInUser: user,
		onlogin: function(assertion){
			$.ajax({
				type: 'POST',
				url: '/blog/auth/login',
				data: {assertion: assertion},
				success: function(res, status, xhr) {loggedInUser=res;window.location.reload();},
				error: function(xhr, status, err) {alert("Login failure: "+err);}
			});
		},
		onlogout: function(){
			$.ajax({
				type: 'POST',
				url: '/blog/auth/logout',
				success: function(res, status, xhr) {window.location.reload();},
				error: function(xhr, status, err) {alert("Logout failure: "+err);}
			})
		}
	});
	$('textarea[name=article]').on('keyup', function()
	{
		$.post("/blog/articles/format",{string: $(this).val()},function(data)
		{
			$('article').html(data);
		});
	});
	$('header').hover(function()
	{
		$('header ul').stop().delay(500).slideDown('fast');
	}, function()
	{
		$('header ul').stop().clearQueue().slideUp('fast');
	});
	$('header ul').slideUp('fast');
});