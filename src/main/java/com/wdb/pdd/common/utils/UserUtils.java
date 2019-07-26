package com.wdb.pdd.common.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wdb.pdd.api.pojo.entity.ReguserDO;
import com.wdb.pdd.api.service.sys.IReguserService;
import com.wdb.pdd.common.exception.MyException;
import com.wdb.pdd.common.exception.handlers.EnumErrorCode;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/12 0012
 * @描述
 */
public class UserUtils {

    /**
     * 获取用户id
     *
     * @return
     */
    public static Integer getReguserId() {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        String authToken = request.getHeader("auth_token");
        StaticLog.info("当前请求头的auth_token:{}", authToken);
        return header2ReguserId(authToken);
    }

    /**
     * 请求头转用户id
     *
     * @param authToken
     * @return
     */
    public static Integer header2ReguserId(String authToken) {
//        IReguserService bean = SpringContextHolder.getBean(IReguserService.class);
//        ReguserDO one = null;
//        try {
//            one = bean.getOne(new LambdaQueryWrapper<ReguserDO>()
//                    .eq(ReguserDO::getSysUserToken, authToken), true);
//        } catch (Exception e) {
//            throw new MyException("当前token在数据库中重复");
//        }
//        if (one == null) {
//            //校验是否存在token
//            throw new MyException(EnumErrorCode.apiAuthorizationFailed.getCodeStr());
//        } else {
//            //校验是否过期
//            Long sysTokenExpiresTime = one.getSysTokenExpiresTime();
//            if (sysTokenExpiresTime == null) {
//                //这里是压根没有过期时间
//                throw new MyException(EnumErrorCode.apiAuthorizationFailed.getCodeStr());
//            }
//            long now = System.currentTimeMillis() / 1000;
//            if (sysTokenExpiresTime < now) {
//                //这里是当前时间戳大于要过期的时间戳
//                throw new MyException(EnumErrorCode.apiAuthorizationFailed.getCodeStr());
//            }
//        }
//        return one.getId();
        return 4;
    }

    /**
     * 弃用
     * @param reguserId
     */
    @Deprecated
    public static void checkUser(Integer reguserId) {
        IReguserService bean = SpringContextHolder.getBean(IReguserService.class);
        //这里最好能加个三级缓存 避免每次请求读库
        ReguserDO byId = bean.getById(reguserId);
        if (byId == null) {
            throw new MyException(EnumErrorCode.reguserNull.getCodeStr());
        }
    }
}
