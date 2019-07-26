package com.wdb.pdd.common.interceptor;

import com.wdb.pdd.common.annotation.CheckToken;
import com.wdb.pdd.common.exception.MyException;
import com.wdb.pdd.common.exception.handlers.EnumErrorCode;
import com.wdb.pdd.common.utils.UserUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/12 0012
 * @描述
 */
public class AuthenticationInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("auth_token");// 从 http 请求头中取出 token
        // 如果不是映射到方法直接通过
        if(!(handler instanceof HandlerMethod)){
            return true;
        }
        HandlerMethod handlerMethod=(HandlerMethod)handler;
        Method method=handlerMethod.getMethod();
        //检查有没有需要用户权限的注解
        if (method.isAnnotationPresent(CheckToken.class)) {
            CheckToken checkToken = method.getAnnotation(CheckToken.class);
            if (checkToken.required()) {
                // 执行认证
                if (token == null) {
                    throw new MyException(EnumErrorCode.tokenNull.getCodeStr());
                }
                // 获取 token 中的 user id
                Integer userId = UserUtils.header2ReguserId(token);
                //UserUtils.checkUser(userId);
                return true;
            }
        }
        return true;
    }
}
