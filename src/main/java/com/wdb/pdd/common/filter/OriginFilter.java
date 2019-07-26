package com.wdb.pdd.common.filter;

import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/12 0012
 * @描述 这是跨域的filter
 */
@WebFilter
@Component
public class OriginFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE,PUT");
        response.setHeader("Access-Control-Max-Age", "3600");
        /**
         * 这里加上固定auth_token key值 接收该值跨域 部署生产可去除
         */
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With,auth_token, Content-Type,Accept, Connection, User-Agent, Cookie");
        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }
}
