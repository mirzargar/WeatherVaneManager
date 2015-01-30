var $runs = [];
var $heights = {};
var $fields = {};
var $run_selector_id = '#run-selector';
var $height_selector_id = '#height-selector';
var $field_selector_id = '#field-selector';
var $svg_container = '#svg-container';
var $loading_image_id = '#loading-image';
var $svg_controls_id = '#svg-controls';
var $play_id = '#play';
var $pause_id = '#pause';
var $forward_id = '#step-forward';
var $back_id = '#step-back';
var $forecast_hour_header_id = '#forcast_hour_header';
var $forecast_hour_label_id = '#forecast_hour_label';
var $animationStepTracker;
var $zoomValue = 1;
var $timeStepValueInMillisconds = 1000;
var $svgCurrentImageIndex = -1;
var $playing = false;
var $playSetIntervalFunctionHandle = null; // this is so we can stop the animation if we need to.
var $viewBoxX = 30;
var $viewBoxY = 0;
var $xOffset = 150;
var $yOffset = 30;
var $viewBoxWidth = 128;
var $viewBoxHeight = 128;
var $isDragging = false;
var $zoomValues = [130, 100, 80, 60, 40, 20];

$(function () {
	$($run_selector_id).change(function () { selectRun(); });
	$($height_selector_id).change(function () { selectHeight(); });
	$($field_selector_id).change(function () { selectField(); });
	$($loading_image_id).hide();

	$($run_selector_id).attr('disabled', 'disabled');
	$($height_selector_id).attr('disabled', 'disabled');
	$($field_selector_id).attr('disabled', 'disabled');

	$($play_id).click(function () { playAnimation(); });
	$($pause_id).click(function () { pauseAnimation(); });
	$($forward_id).click(function () { forwardAnimation(); });
	$($back_id).click(function () { backAnimation(); });

	populateRunSelector();
	
//	selectField();

});

function getSVGDomObject() {
	return $($svg_container).find('svg')[0];
}

function populateRunSelector() {
	$.get($navigationBaseUrl + '/index/get_runs',
		function (data) {
			for (var i = 0; i < data.length; ++i) {
				var run = data[i];
				var run_time = run.date + "." + run.time;
				if ($runs[run_time] == null) {
					$runs[run_time] = run;
				}
				if ($heights[run_time] == null) {
					$heights[run_time] = [];
				}
				var run_time_height = run_time + run.height;
				if ($heights[run_time_height] == null) {
					$heights[run_time_height] = true;
					$heights[run_time].push(run);
				}
				if ($fields[run_time_height] == null) {
					$fields[run_time_height] = [];
				}
				$fields[run_time_height].push(run);
			}
			populateRunSelectorElement();
		}
	);
}

function populateRunSelectorElement() {
	$($run_selector_id).find('option').remove().end().append('<option value="">--Select a Run--</option>').val(""); // clear the run selector
	for (var run_time in $runs) {
		var run = $runs[run_time];
		$($run_selector_id).append('<option value="' + run.date + "." + run.time + '">' + run.run_label + '</option>');
	}
	$($run_selector_id).removeAttr('disabled');
	$($height_selector_id).attr('disabled', 'disabled');
	$($field_selector_id).attr('disabled', 'disabled');
}

function selectRun() {
	$($height_selector_id).find('option').remove().end().append('<option value="">--Select a Height--</option>').val(""); // clear the run selector
	var run_time = $($run_selector_id).val();
	for (var index in $heights[run_time]) {
		var run = $heights[run_time][index];
		$($height_selector_id).append('<option value="' + run.height + '">' + run.height + '</option>');
	}
	$($height_selector_id).removeAttr('disabled');
	$($field_selector_id).attr('disabled', 'disabled');
	removeSVGAnimation();
}

function selectHeight() {
	$($field_selector_id).find('option').remove().end().append('<option value="">--Select a Field--</option>').val(""); // clear the run selector
	var run_time_height = $($run_selector_id).val() + $($height_selector_id).val();
	for (var index in $fields[run_time_height]) {
		var run = $fields[run_time_height][index];
		// this is where the lookup of the map goes
		$($field_selector_id).append('<option value="' + run.field + '" data-level_set="' + run.level_set + '">' + run.field + ' (' + run.level_set_map[run.level_set] + ')</option>');
	}
	$($field_selector_id).removeAttr('disabled');
	removeSVGAnimation();
}

function selectField() {
	var level_set = $($field_selector_id).find(":selected").attr('data-level_set');
	$($svg_container).css('display', 'none');
	$($loading_image_id).show();
	var files = getFileList(level_set);
	$($svg_container).html('');
	var count = 0;
	$.each(files,
		function (index, fileName) {
			var url = $navigationBaseUrl + '/index/get-svg-file?run=' + $($run_selector_id).val() + '&height=' + $($height_selector_id).val() +'&field=' +$($field_selector_id).val() + '&level_set=' + level_set + "&filename=" + fileName;
			var forecast_hour = fileName.substring(fileName.lastIndexOf('_') + 1).replace(".svg", "");
			$($svg_container).append('<div id="svg_' + index + '" style="display: none;" class="svg_object" data-forecast_hour="' + forecast_hour + '"></div>');
			$('#svg_' + index).load(url,
				function (element) {
					//					$($('#svg_' + index).find('svg')[0]).attr('width', '100%');
//					$($('#svg_' + index).find('svg')[0]).attr('height', '100%');
				}
			);
			if (index >= files.length - 1) {
				$($svg_controls_id).show();
				$($loading_image_id).hide();
				$($svg_container).show();
				$($forecast_hour_header_id).show();
				$playing = false;
				$svgCurrentImageIndex = -1;
				clearInterval($playSetIntervalFunctionHandle);
				playAnimation();
				$('#svg-container').mousedown(function () { $isDragging = true; });
				$('#svg-container').mousemove(function (event) { 
					if ($isDragging) {
//								setViewBox((event.clientX * -0.3) + $xOffset, (event.clientY * -0.3) + $yOffset, $viewBoxWidth, $viewBoxHeight);
					} 
				});
				$(window).mouseup(function () { $isDragging = false;});
			}
		}
	);
	
}

function getCurrentFrame() {
	return $('.svg_object').get($svgCurrentImageIndex);
}

function incrementFrame(incrementor) {
	$(getCurrentFrame()).hide();
	$svgCurrentImageIndex = (($svgCurrentImageIndex + incrementor) % $('.svg_object').length)
	var nextSVGObject = $('.svg_object').get($svgCurrentImageIndex);
	$(nextSVGObject).show();
	console.log($(nextSVGObject).find('image').attr('xlink:href'));
	$($forecast_hour_label_id).html($(nextSVGObject).attr('data-forecast_hour'));
	
}

function playAnimation() {
	if ($playing) { return; } // so there is no way to overlap setIntervals
	$playing = true;
	incrementFrame(1);
	$playSetIntervalFunctionHandle = setInterval(
		function () {
			if (!$playing) { return; }
			incrementFrame(1);
		}, $timeStepValueInMillisconds);
}

function pauseAnimation() {
	clearInterval($playSetIntervalFunctionHandle);
	$playing = false;
}

function forwardAnimation() {
	pauseAnimation();
	incrementFrame(1);
}

function backAnimation() {
	pauseAnimation();
	incrementFrame(-1);
}

function getTimeStep() {
	return 0.75;
}

function getFrameCount() {
	return $(getSVGDomObject()).find('image').length;
}

function getMaxZoomValue() {
	return $('.zoom-indicator').length;
}

function zoomIn() {
	var zoomVal = (getMaxZoomValue() <= $zoomValue) ? $zoomValue : $zoomValue + 1;
	setZoom(zoomVal);
}

function zoomOut() {
	zoomVal = (1 >= $zoomValue) ? $zoomValue : $zoomValue - 1;
	setZoom(zoomVal);
}

function getFileList(level_set) {
	var fileList = [];
	$.ajax({
		url: $navigationBaseUrl + '/index/get-svg-file-list',
		data: {'run': $($run_selector_id).val(), 'height': $($height_selector_id).val(),'field': $($field_selector_id).val(), 'level_set': level_set},
		success:
			function (data) {
				if (data.success) {
					$.each(data.filenames,
						function (index, element) {
							fileList.push(element)
						}
					);
				}
				else {
					alert(data.message);
				}
			},
		async: false
	});	
	return fileList;
}

function removeSVGAnimation() {
	$($svg_container).html('');
	$($svg_controls_id).hide();
	$($forecast_hour_header_id).hide();
	$animationStepTracker = null;
	//setZoom(1);
}
function setViewBox(x, y, width, height) {
	return;
	$($svg_container).find('svg').each( 
		function (index, element) {
			element.setAttribute('viewBox', x + ' ' + y + ' ' + width + ' ' + height);
		}
	);
	$viewBoxX = x;
	$viewBoxY = y;
	$viewBoxWidth = width;
	$viewBoxHeight = height;
}

function setZoom(zoomVal) {
	if (getCurrentFrame() == undefined) { return; }
	$zoomValue = zoomVal;
	$.each($('.zoom-indicator'),
		function (index, element) {
			$(element).removeClass('bold-zoom');
		}
	);
	$('#zoom_' + $zoomValue).addClass('bold-zoom');
//	setViewBox($viewBoxX, $viewBoxY, $zoomValues[zoomVal - 1], $zoomValues[zoomVal - 1]);
}