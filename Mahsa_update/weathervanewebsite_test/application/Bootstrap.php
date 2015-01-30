<?php

class Bootstrap extends Zend_Application_Bootstrap_Bootstrap
{

	protected function _initAutoLoad()
	{
		Zend_Layout::startMvc(array('layoutPath' => APPLICATION_PATH . '/views/layouts'));
		$modelLoader = new Zend_Application_Module_Autoloader(
						array('namespace' => '', 'basePath' => APPLICATION_PATH)
		);
		return $modelLoader;
	}

	protected function _initDoctype()
	{
		$viewRenderer = Zend_Controller_Action_HelperBroker::getStaticHelper('viewRenderer');
		$viewRenderer->initView();
		$view = $viewRenderer->view;
		$view->doctype('XHTML1_STRICT');
	}

	protected function _initRouting()
	{
		$router = Zend_Controller_Front::getInstance()->getRouter();
		$id_route = new Zend_Controller_Router_Route(
						':controller/:action/:id',
						array()
		);
		$router->addRoute('id_route', $id_route);
	}

	protected function _initSetTimeZone()
	{
		date_default_timezone_set('America/Boise');
	}

	protected function _initAdditionalIncludes()
	{
		require_once APPLICATION_PATH . '/controllers/BaseController.php'; // include the Base controller
	}

}

