package com.wdb.pdd.api.service.logistics.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pdd.pop.sdk.http.PopHttpClient;
import com.pdd.pop.sdk.http.api.request.PddGoodsLogisticsTemplateGetRequest;
import com.pdd.pop.sdk.http.api.response.PddGoodsLogisticsTemplateGetResponse;
import com.wdb.pdd.api.dao.logistics.GoodsLogisticsTemplateDao;
import com.wdb.pdd.api.pojo.entity.GoodsLogisticsTemplateDO;
import com.wdb.pdd.api.pojo.entity.ReguserDO;
import com.wdb.pdd.api.service.logistics.ILogisticsTemplateService;
import com.wdb.pdd.api.service.logistics.LServiceTemplate;
import com.wdb.pdd.api.service.sys.IReguserService;
import com.wdb.pdd.common.exception.MyException;
import com.wdb.pdd.common.utils.IdUtil;
import com.wdb.pdd.common.utils.UserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@Service
public class LServiceTemplateImpl extends ServiceImpl<GoodsLogisticsTemplateDao, GoodsLogisticsTemplateDO>  implements LServiceTemplate {


    private Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    private PopHttpClient popHttpClient;
    @Autowired
    private IReguserService reguserService;
    @Autowired
    private ILogisticsTemplateService logisticsTemplateService;

    @Override
    public List<GoodsLogisticsTemplateDO> Llist(Integer reguserId) {
        List<GoodsLogisticsTemplateDO> goodsLogisticsTemplateDOs = new ArrayList<>();

        ReguserDO byId = reguserService.getById(reguserId);
        String accessToken = byId.getAccessToken();
        System.out.println(accessToken);
        PddGoodsLogisticsTemplateGetRequest request = new PddGoodsLogisticsTemplateGetRequest();
        request.setPage(1);
        request.setPageSize(1);
        try {
            PddGoodsLogisticsTemplateGetResponse response = popHttpClient.syncInvoke(request, accessToken);
            PddGoodsLogisticsTemplateGetResponse.GoodsLogisticsTemplateGetResponse goodsLogisticsTemplateGetRes = response.getGoodsLogisticsTemplateGetResponse();
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


        }catch (Exception e) {
            log.error(e.toString(), e);
            throw new MyException("拼多多接口调用异常");
        }


        return goodsLogisticsTemplateDOs;
    }
}
