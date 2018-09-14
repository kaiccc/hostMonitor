package com.mon.host.common.exception;


import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.mon.host.common.enums.CodeMessage;
import com.mon.host.common.rest.RestResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 异常处理器
 * 
 * @author xt
 */
@RestControllerAdvice
public class RestExceptionHandler {
    private static final Log logger = LogFactory.get();
	/**
	 * 处理自定义异常
	 */
	@ExceptionHandler(RestException.class)
	public RestResponse<String> handleException(RestException e){
		logger.error(e);
		return RestResponse.failed(e.getCode(),e.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public RestResponse<String> handleException(Exception e){
		logger.error(e);
		return RestResponse.failed(CodeMessage.HTTP_INTERNAL_SERVER_ERROR);
	}
}
