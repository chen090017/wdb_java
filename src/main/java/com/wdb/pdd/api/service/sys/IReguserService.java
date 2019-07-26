package com.wdb.pdd.api.service.sys;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wdb.pdd.api.pojo.entity.ReguserDO;

public interface IReguserService extends IService<ReguserDO> {

    /**
     * 请求Token过期刷新Token
     * @param id
     * @return
     */
    ReguserDO refreshToken(Integer id);
}
