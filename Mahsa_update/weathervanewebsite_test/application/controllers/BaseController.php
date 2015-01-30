<?php
/**
 *
 * @author dlee
 */
abstract class BaseController extends Zend_Controller_Action
{
	protected $isJsonRequest = false;

	public function __construct(Zend_Controller_Request_Abstract $request, Zend_Controller_Response_Abstract $response, array $invokeArgs = array())
	{
		parent::__construct($request, $response, $invokeArgs);
		$this->isJsonRequest = (!empty($_SERVER['HTTP_X_REQUESTED_WITH']) && strtolower($_SERVER['HTTP_X_REQUESTED_WITH']) == 'xmlhttprequest'); // sets isJsonRequest to true if a JSON request

		// if this is  a json request then disable the layout
		if ($this->isJsonRequest)
		{
			$this->_helper->layout->disableLayout();
		}
	}

	public function dispatch($action)
	{
		$this->view->addScriptPath(APPLICATION_PATH . '/views/helpers');
		$this->view->actionName = $this->_request->getActionName();

		 // Notify helpers of action preDispatch state
        $this->_helper->notifyPreDispatch();
        $this->preDispatch();
        if ($this->getRequest()->isDispatched()) {
            if (null === $this->_classMethods) {
                $this->_classMethods = get_class_methods($this);
            }

			$params = $this->_getAllParams(); // get all available params
			$methodParamsArray = $this->GetActionParams($action); // put them in an array

			$data = array();
			foreach ($methodParamsArray as $param)
			{
				$name = $param->getName();
//				$value = $params[$name];
				if ($param->isOptional()) // if it's an optional param, then check if it exists and use the appropriate value
				{
					$data[$name] = (isset($params[$name]) && $params[$name] != null) ? $params[$name] : $param->getDefaultValue();
				}
				else if (!isset($params[$name]) || $params[$name] == null) {
					$data[$name] = null;
				}
				else
				{
					$data[$name] = $params[$name];
				}
			}


            // preDispatch() didn't change the action, so we can continue
            if ($this->getInvokeArg('useCaseSensitiveActions') || in_array($action, $this->_classMethods)) {
                if ($this->getInvokeArg('useCaseSensitiveActions')) {
                    trigger_error('Using case sensitive actions without word separators is deprecated; please do not rely on this "feature"');
                }
				call_user_func_array(array($this, $action), $data);
//                $this->$action();
            } else {
				call_user_func_array(array($this, $action), $data);
//                $this->__call($action, array());
            }
            $this->postDispatch();
        }

        // whats actually important here is that this action controller is
        // shutting down, regardless of dispatching; notify the helpers of this
        // state
        $this->_helper->notifyPostDispatch();

	}

	protected function GetActionParams($action)
	{
		$classRef = new ReflectionObject($this);
		$className = $classRef->getName();
		$funcRef = new ReflectionMethod($className, $action);
		$paramsRef = $funcRef->getParameters();
		return $paramsRef;
	}

	static function sanitize($text)
	{
		return trim(htmlentities($text));
	}

	public function sanitizeFilePath($path)
	{
		$path = str_replace("/", "", $path);
		$path = str_replace("\\", "", $path);
		return $path;
	}
}
