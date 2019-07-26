package com.wdb.pdd.api.service.logistics.impl;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pdd.pop.sdk.http.PopBaseHttpResponse;
import com.pdd.pop.sdk.http.PopHttpClient;
import com.pdd.pop.sdk.http.api.request.PddGoodsLogisticsTemplateGetRequest;
import com.pdd.pop.sdk.http.api.response.PddGoodsLogisticsTemplateGetResponse;
import com.wdb.pdd.api.pojo.entity.GoodsLogisticsTemplateDO;
import com.wdb.pdd.api.pojo.entity.ReguserDO;
import com.wdb.pdd.api.service.logistics.ILogisticsTemplateBizService;
import com.wdb.pdd.api.service.logistics.ILogisticsTemplateService;
import com.wdb.pdd.api.service.sys.IReguserService;
import com.wdb.pdd.common.exception.MyException;
import com.wdb.pdd.common.exception.handlers.EnumErrorCode;
import com.wdb.pdd.common.utils.IdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/6 0006
 * @描述
 */
@Service
public class LogisticsTemplateBizServiceImpl implements ILogisticsTemplateBizService {

    private Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    private PopHttpClient popHttpClient;
    @Autowired
    private IReguserService reguserService;
    @Autowired
    private ILogisticsTemplateService logisticsTemplateService;

    /**
     * 获取同步该用户运费模板
     *
     * @param reguserId
     */
    @Override
    @Transactional
    public void syncLogisticsTemplate(Integer reguserId) {
        //查询是否存在用户
        ReguserDO byId = reguserService.getById(reguserId);
        if (byId == null) {
            log.error("不存在该用户_id:{}", reguserId);
            throw new MyException(EnumErrorCode.apiAuthorizationFailed.getCodeStr());
        }
        //校验到期时间
        long l = System.currentTimeMillis();
        if (byId.getExpiresIn() < (l / 1000)) {
            byId = reguserService.refreshToken(reguserId);
            if (byId == null) {
                log.error("刷新Token失败_id:{}", reguserId);
                throw new MyException(EnumErrorCode.apiAuthorizationFailed.getCodeStr());
            }
        }
        String accessToken = byId.getAccessToken();
        logisticsTemplateService.remove(new LambdaQueryWrapper<GoodsLogisticsTemplateDO>()
                .eq(GoodsLogisticsTemplateDO::getReguserId, reguserId));
        PddGoodsLogisticsTemplateGetRequest request = new PddGoodsLogisticsTemplateGetRequest();
        request.setPage(1);
        request.setPageSize(1);
        try {
            PddGoodsLogisticsTemplateGetResponse response = popHttpClient.syncInvoke(request, accessToken);

            if (response != null) {
                PopBaseHttpResponse.ErrorResponse errorResponse = response.getErrorResponse();
                if (errorResponse != null) {
                    throw new MyException(errorResponse.getErrorMsg());
                }
                PddGoodsLogisticsTemplateGetResponse.GoodsLogisticsTemplateGetResponse goodsLogisticsTemplateGetResponse = response.getGoodsLogisticsTemplateGetResponse();
                Integer totalCount = goodsLogisticsTemplateGetResponse.getTotalCount();
                Integer page = totalCount / 20 + (totalCount % 20 == 0 ? 0 : 1);
                List<GoodsLogisticsTemplateDO> goodsLogisticsTemplateDOs = new ArrayList<>();
                for (int i = 1; i <= page; i++) {
                    PddGoodsLogisticsTemplateGetRequest req = new PddGoodsLogisticsTemplateGetRequest();
                    request.setPage(i);
                    request.setPageSize(20);
                    PddGoodsLogisticsTemplateGetResponse res = popHttpClient.syncInvoke(req, accessToken);
                    if (res != null) {
                        PopBaseHttpResponse.ErrorResponse errorRes = res.getErrorResponse();
                        if (errorRes != null) {
                            throw new MyException(errorRes.getErrorMsg());
                        }
                        PddGoodsLogisticsTemplateGetResponse.GoodsLogisticsTemplateGetResponse goodsLogisticsTemplateGetRes = res.getGoodsLogisticsTemplateGetResponse();
                        List<PddGoodsLogisticsTemplateGetResponse.GoodsLogisticsTemplateGetResponseLogisticsTemplateListItem> logisticsTemplateList = goodsLogisticsTemplateGetRes.getLogisticsTemplateList();
                        Date now = new Date();
                        for (PddGoodsLogisticsTemplateGetResponse.GoodsLogisticsTemplateGetResponseLogisticsTemplateListItem item : logisticsTemplateList){
                            GoodsLogisticsTemplateDO goodsLogisticsTemplateDO = new GoodsLogisticsTemplateDO();
                            goodsLogisticsTemplateDO.setId(IdUtil.getStrId());
                            goodsLogisticsTemplateDO.setReguserId(reguserId);
                            goodsLogisticsTemplateDO.setTemplateId(item.getTemplateId());
                            goodsLogisticsTemplateDO.setTemplateName(item.getTemplateName());
                            goodsLogisticsTemplateDO.setAddTime(now);
                            goodsLogisticsTemplateDOs.add(goodsLogisticsTemplateDO);
                        }
                    }
                }
                logisticsTemplateService.saveBatch(goodsLogisticsTemplateDOs);
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
            throw new MyException("拼多多接口调用异常");
        }
    }

}
