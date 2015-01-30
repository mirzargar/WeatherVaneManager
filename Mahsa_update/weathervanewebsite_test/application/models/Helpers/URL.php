<?php

class Model_Helpers_URL 
{
	
	public static function AssetURL()
	{
		return str_replace('index.php', '', Zend_Controller_Front::getInstance()->getBaseUrl());
	}
	
	public static function NavigationURL()
	{
		return Zend_Controller_Front::getInstance()->getBaseUrl();
	}
}
