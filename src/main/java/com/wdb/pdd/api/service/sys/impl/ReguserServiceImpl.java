package com.wdb.pdd.api.service.sys.impl;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pdd.pop.sdk.http.PopAccessTokenClient;
import com.pdd.pop.sdk.http.token.AccessTokenResponse;
import com.wdb.pdd.api.dao.sys.ReguserDao;
import com.wdb.pdd.api.pojo.entity.ReguserDO;
import com.wdb.pdd.api.service.sys.IReguserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/5 0005
 * @描述
 */
@Service
public class ReguserServiceImpl extends ServiceImpl<ReguserDao, ReguserDO> implements IReguserService {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private PopAccessTokenClient accessTokenClient;

    /**
     * 请求Token过期刷新Token
     *
     * @param id
     * @return
     */
    @Override
    public ReguserDO refreshToken(Integer id) {
        if(id == null){
            return null;
        }
        ReguserDO byId = this.getById(id);
        if(byId == null){
            return null;
        }
        try {
            AccessTokenResponse refresh = accessTokenClient.refresh(byId.getRefreshToken());
            if(refresh.getErrorResponse() != null){
                log.error(JSON.toJSONString(refresh.getErrorResponse()));
                return null;
            }
            long l = System.currentTimeMillis() / 1000;
            byId.setAccessToken(refresh.getAccessToken());
            byId.setExpiresIn(l + refresh.getExpiresIn());
            this.updateById(byId);
        } catch (Exception e) {
            log.error(e.toString(),e);
            return null;
        }
        return byId;
    }
}
