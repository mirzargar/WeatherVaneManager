<?php

class IndexController extends BaseController
{
	
//	private $runs_path = '/home/dillonl/weathervane/svg/';
    // MAHSA
	//private $runs_path = '/Users/dillonl/Documents/weathervane/svg/';
    private $runs_path = '/usr/local/weather/WeatherManager/svg/';
	private $level_set_map = array(248 => '-25C', 253 => '-20C', 258 => '-15C', 283 => '+10C', 288 => '+15C', 5460 => '5460', 5580 => '5580', 5700 => '5700', 5820 => '5820');

    public function init()
    {
		$this->_helper->layout->setLayout('layout');
        /* Initialize action controller here */
    }

    public function indexAction()
    {
        $this->view->headScript()->appendFile(Model_Helpers_URL::AssetURL() .'/scripts/run_helper.js');
    }
	
	public function testAction()
	{
        // MAHSA
		//$this->runs_path = '/Users/dillonl/Documents/weathervane/svg/';
        $this->runs_path = '/usr/local/weather/WeatherManager/svg/';
		$dates = array_diff(scandir($this->runs_path), array('.', '..', '.DS_Store'));
		$runs = array();
		foreach ($dates as $date)
		{
			$path = $this->runs_path . '/' . $date;
			$times = array_diff(scandir($path), array('.', '..', '.DS_Store'));
			foreach ($times as $time)
			{
				$path = $this->runs_path . '/' . $date . '/' . $time;
				$fields = array_diff(scandir($path), array('.', '..', '.DS_Store'));
				foreach ($fields as $field)
				{
					$runs[] = $field;
				}
			}
		}
		return $this->_helper->json($runs);
	}

	public function getRunsAction()
	{
		$path = $this->runs_path;
		$runs = array();
		$dates = array_diff(scandir($this->runs_path), array('.', '..', '.DS_Store'));
		foreach ($dates as $date)
		{
			$path = $this->runs_path . '/' . $date;
			$times = array_diff(scandir($path), array('.', '..', '.DS_Store'));
			foreach ($times as $time)
			{
				$run_label = $this->getRunLabel($date, $time);
				$path = $this->runs_path . '/' . $date . '/' . $time;
				$fields = array_diff(scandir($path), array('.', '..', '.DS_Store'));
				foreach ($fields as $field)
				{
					$path = $this->runs_path . '/' . $date . '/' . $time . '/' . $field;
					$heights = array_diff(scandir($path), array('.', '..', '.DS_Store'));
					foreach ($heights as $height)
					{
						$path = $this->runs_path . '/' . $date . '/' . $time . '/' . $field . '/' . $height;
						$level_sets = array_diff(scandir($path), array('.', '..', '.DS_Store'));
						foreach ($level_sets as $level_set)
						{
							$run = array('date' => $date, 'time' => $time, 'height' => $height, 'field' => $field, 'level_set' => $level_set, 'level_set_map' => $this->level_set_map, 'run_label' => $run_label);
							$runs[] = $run;
						}
					}
				}
			}
		}
		$this->view->layout()->disableLayout();
        $this->_helper->viewRenderer->setNoRender(true);
		return $this->_helper->json($runs);
	}
	
	/*
	 * $date is yyyymmdd
	 * $time is either 03, 09, 15, or 21
	 */
	private function getRunLabel($date, $time)
	{
		$year = substr($date, 0, 4);
		$month = substr($date, 4, 2);
		$day = substr($date, 6, 2);
		
		$run_string = $month . '/' . $day . '/' . $year;
		if (intval($time) > 12)
		{
			$run_string .= ' ' . ($time - 12) . ':00 pm';
		}
		else
		{
			$run_string .= ' ' . (intval($time)) . ':00 am';
		}
		return $run_string;
	}

	public function getSvgFileListAction($run, $height, $field, $level_set)
	{
//		$run = strtolower($this->sanitizeFilePath($run));
//		$height = strtolower($this->sanitizeFilePath($height));
//		$field = strtolower($this->sanitizeFilePath($field));
		
		$svgPath = $this->runs_path . str_replace('.' , '/', $run) . '/'. $field . '/' . $height . '/' . $level_set;
		
		if (!file_exists($svgPath))
		{
			return $this->_helper->json(array('success' => false, 'message' => 'Weather information is unavailable', 'path' =>$svgPath));
		}
		$files = array_diff(scandir($svgPath), array('.','..'));
		$tmp_files = array();
		foreach ($files as $file)
		{
			if (strpos($file, "anim") === false)
			{
				$tmp_files[] = $file;
			}
		}
		usort($tmp_files, array('IndexController', 'sortSVGFiles'));
		return $this->_helper->json(array('success' => true, 'filenames' =>$tmp_files, 'path' => 'svg/' . $run . '/' . $height . '/' . $field . '/'));
	}
	
	private static function sortSVGFiles($a, $b)
	{
		$a_forecast_hour = str_replace(".svg", "", substr($a, strrpos($a, '_') + 1));
		$b_forecast_hour = str_replace(".svg", "", substr($b, strrpos($b, '_') + 1));
		return intval($a_forecast_hour) > intval($b_forecast_hour);
	}
	
	public function getSVGFileAction($run, $field, $height, $level_set, $filename)
	{	
		$svgFilePath = $this->runs_path . str_replace('.' , '/', $run) . '/'. $field . '/' . $height . '/' . $level_set . '/' . $filename;
		
		header('Content-Type: image/svg+xml');
		header('Content-Disposition: attachment; filename="' . $filename . '"');
		readfile($svgFilePath);

		// disable the view and layout
		$this->view->layout()->disableLayout();
		$this->_helper->viewRenderer->setNoRender(true);
	}

}

