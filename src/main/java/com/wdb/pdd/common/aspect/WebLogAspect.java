package com.wdb.pdd.common.aspect;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wdb.pdd.common.utils.IPUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @oauth Yeohwah
 * @createDate 2019/6/6 0021
 * @info
 */
@Aspect
@Component
public class WebLogAspect {
    //统计请求的处理时间
    private ThreadLocal<Long> localTime = new ThreadLocal<>();
    private static final Logger logger = LoggerFactory.getLogger(WebLogAspect.class);

    @Pointcut("execution(public * com.wdb.pdd.api.controller..*.*(..))")
    public void webLog(){

    }

    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint){
        localTime.set(System.currentTimeMillis());
        logger.info("==================== REQUEST START ===================");
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            // 记录下请求内容
            logger.info("      URL : " + request.getRequestURL().toString());
            logger.info("      HTTP_METHOD : " + request.getMethod());
            logger.info("      IP : " + IPUtils.getIpAddr(request));
            logger.info("      REQUEST：" + JSONObject.toJSONString(joinPoint.getArgs()));
        } catch (Exception e) {
            logger.error("================= 切面日志错误 ================");
            logger.error(e.toString(),e);
        }
    }

    @AfterReturning(returning = "ret", pointcut = "webLog()")
    public void doAfterReturning(Object ret) throws Throwable {
        // 处理完请求，返回内容
        logger.info("      RESPONSE : {}", ret==null? ret: JSON.toJSONString(ret));
        logger.info("==================== RESPONSE END ===================");
        logger.info("      耗时(ms) : "+ (System.currentTimeMillis() - localTime.get()));
        localTime.remove();
    }
}