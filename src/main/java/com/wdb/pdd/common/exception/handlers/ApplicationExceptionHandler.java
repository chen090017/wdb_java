package com.wdb.pdd.common.exception.handlers;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.wdb.pdd.common.exception.MyException;
import com.wdb.pdd.common.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * 异常处理器
 * 
 */
@RestControllerAdvice
public class ApplicationExceptionHandler {
    private Logger log = LoggerFactory.getLogger(getClass());
    private Log logger = LogFactory.get();

    /**
     * 自定义异常
     */
    @ExceptionHandler(MyException.class)
    public Result<String> handleMyException(MyException e) {
        logger.info("handleMyException");
        logger.error(e.toString(),e);
        try {
            int code = Integer.parseInt(e.getMessage());
            return Result.build(code, EnumErrorCode.getMsgByCode(code));
        } catch (NumberFormatException e1) {
            return Result.build(500, e.getMessage());
        }
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Result<String> handleDuplicateKeyException(DuplicateKeyException e) {
        log.error(e.toString(),e);
        return Result.build(EnumErrorCode.duplicateKeyExist.getCode(), EnumErrorCode.duplicateKeyExist.getMsg());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<String> noHandlerFoundException(NoHandlerFoundException e) {
        log.error(e.toString(),e);
        return Result.build(EnumErrorCode.pageNotFound.getCode(), EnumErrorCode.pageNotFound.getMsg());
    }

    /*
    加权限再加
     @ExceptionHandler(ShiroException.class)
    public Result<String> handleAuthorizationException(ShiroException e) {
        log.error(e.getMessage());
        if(e instanceof IncorrectCredentialsException) {
        	return Result.build(EnumErrorCode.apiAuthorizationFailed.getCode(), EnumErrorCode.apiAuthorizationFailed.getMsg());
        }else if(e instanceof ExpiredCredentialsException) {
        	return Result.build(EnumErrorCode.apiAuthorizationExpired.getCode(), EnumErrorCode.apiAuthorizationExpired.getMsg());
        }
        return Result.build(EnumErrorCode.notAuthorization.getCode(), EnumErrorCode.notAuthorization.getMsg());
    }
    */

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error(e.toString(),e);
        return Result.build(EnumErrorCode.unknowFail.getCode(), EnumErrorCode.unknowFail.getMsg());
    }
}
