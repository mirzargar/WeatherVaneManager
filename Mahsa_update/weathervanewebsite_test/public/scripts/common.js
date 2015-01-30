$(function () {
	$('.ui_button').button();
	$('.date_picker').datepicker(
		{
			dateFormat: "mm-dd-yy",
			maxDate: 0,
			yearRange: '1900:' + new Date().getFullYear(),
			changeMonth: true,
			changeYear: true
		}
	);
});

function toggleSection(sectionID) {
	if ($('#' + sectionID).is(':visible')) {
		$('#' + sectionID).hide();
	}
	else {
		$('#' + sectionID).show();
	}
}

/**
 * This checks the response object and redirects as needed.
 */
function checkResponse(jsonData) {
	if (jsonData && jsonData['redirect']) {
		alert('Due to inactivity you have been logged out')
		window.location.href = $navigationBaseUrl + '/' + jsonData['redirect'];
	}
}

/*
 * When used with a onkeypress of a text input it calls the
 * login function when enter is presssed.
 */
function checkForEnter(e, callback) {
	var code = (e.keyCode ? e.keyCode : e.which);
	if(code == 13) { //Enter keycode
		callback(); // if we pressed enter then call the callback
	}
}