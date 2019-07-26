package com.wdb.pdd.api.service.logistics;

/**
 * 商品运费模板业务Service
 */
public interface ILogisticsTemplateBizService {
    /**
     * 获取同步该用户运费模板
     * @param reguserId
     */
    void syncLogisticsTemplate(Integer reguserId);
}
