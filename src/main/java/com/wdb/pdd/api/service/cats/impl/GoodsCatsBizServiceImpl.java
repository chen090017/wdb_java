package com.wdb.pdd.api.service.cats.impl;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pdd.pop.sdk.http.PopBaseHttpResponse;
import com.pdd.pop.sdk.http.PopHttpClient;
import com.pdd.pop.sdk.http.api.request.PddGoodsAuthorizationCatsRequest;
import com.pdd.pop.sdk.http.api.request.PddGoodsCatsGetRequest;
import com.pdd.pop.sdk.http.api.response.PddGoodsAuthorizationCatsResponse;
import com.pdd.pop.sdk.http.api.response.PddGoodsCatsGetResponse;
import com.wdb.pdd.api.pojo.entity.GoodsCatsDO;
import com.wdb.pdd.api.pojo.entity.ReguserDO;
import com.wdb.pdd.api.pojo.vo.GoodsCatsVO;
import com.wdb.pdd.api.service.cats.IGoodsCatsBizService;
import com.wdb.pdd.api.service.cats.IGoodsCatsService;
import com.wdb.pdd.api.service.sys.IReguserService;
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
 * @创建时间 2019/6/9 0009
 * @描述
 */
@Service
public class GoodsCatsBizServiceImpl implements IGoodsCatsBizService {
    private Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    private IReguserService reguserService;
    @Autowired
    private IGoodsCatsService goodsCatsService;
    @Autowired
    private PopHttpClient popHttpClient;

    /**
     * 同步类目
     */
    @Override
    @Transactional
    public void syncCats() throws Exception {
        Date now = new Date();
        Long parentId = 0L;
        queryAndSave(now, parentId);
    }

    private void queryAndSave(Date now, Long parentId) throws Exception {
        PddGoodsCatsGetRequest request = new PddGoodsCatsGetRequest();
        request.setParentCatId(parentId);
        PddGoodsCatsGetResponse pddGoodsCatsGetResponse = popHttpClient.syncInvoke(request);
        PopBaseHttpResponse.ErrorResponse errorResponse = pddGoodsCatsGetResponse.getErrorResponse();
        if (errorResponse != null) {
            String s = JSON.toJSONString(errorResponse);
            log.error(s);
            if(errorResponse.getErrorCode().equals(70031) || errorResponse.getErrorCode().equals(70032)){
                log.error("隔1s重试");
                Thread.sleep(1000);
                queryAndSave(now,parentId);
            }
            return ;
        }


        PddGoodsCatsGetResponse.GoodsCatsGetResponse goodsCatsGetResponse = pddGoodsCatsGetResponse.getGoodsCatsGetResponse();
        if (goodsCatsGetResponse != null) {
            List<PddGoodsCatsGetResponse.GoodsCatsGetResponseGoodsCatsListItem> goodsCatsList = goodsCatsGetResponse.getGoodsCatsList();
            if(goodsCatsList != null && goodsCatsList.size() > 0){
                ArrayList<GoodsCatsDO> cats = new ArrayList<>();
                for(PddGoodsCatsGetResponse.GoodsCatsGetResponseGoodsCatsListItem item : goodsCatsList){
                    GoodsCatsDO goodsCatsDO = new GoodsCatsDO();
                    goodsCatsDO.setCatName(item.getCatName());
                    goodsCatsDO.setCatId(item.getCatId());
                    goodsCatsDO.setId(IdUtil.getStrId());
                    goodsCatsDO.setAddTime(now);
                    goodsCatsDO.setLevel(item.getLevel());
                    goodsCatsDO.setParentCatId(item.getParentCatId());
                    cats.add(goodsCatsDO);
                }
                //批量储存
                goodsCatsService.saveBatch(cats);
                for(GoodsCatsDO aDo : cats){
                    queryAndSave(now,aDo.getCatId());
                }
            }
        }
    }

    /**
     * 正向查询下级类目树
     *
     * @param reguserId
     * @param catId
     */
    @Override
    public GoodsCatsVO getSub(Integer reguserId, Long catId) {
        //查询是否存在用户
        ReguserDO byId = reguserService.getById(reguserId);
        if(byId == null){
            log.error("不存在该用户_id:{}",reguserId);
            return null;
        }
        //校验到期时间
        long l = System.currentTimeMillis();
        if(byId.getExpiresIn() < (l/1000)){
            byId = reguserService.refreshToken(reguserId);
            if(byId == null){
                log.error("刷新Token失败_id:{}",reguserId);
                return null;
            }
        }
        String accessToken = byId.getAccessToken();
        PddGoodsAuthorizationCatsRequest request = new PddGoodsAuthorizationCatsRequest();
        request.setParentCatId(catId);
        try {
            PddGoodsAuthorizationCatsResponse pddGoodsAuthorizationCatsResponse = popHttpClient.syncInvoke(request, accessToken);
            PopBaseHttpResponse.ErrorResponse errorResponse = pddGoodsAuthorizationCatsResponse.getErrorResponse();
            if(errorResponse != null){
                if(errorResponse.getErrorCode().equals(70031) || errorResponse.getErrorCode().equals(70032)){
                    Thread.sleep(1000);
                    return getSub(reguserId,catId);
                }
                return null;
            }
            PddGoodsAuthorizationCatsResponse.GoodsAuthCatsGetResponse goodsAuthCatsGetResponse = pddGoodsAuthorizationCatsResponse.getGoodsAuthCatsGetResponse();
            if(goodsAuthCatsGetResponse != null){
                List<PddGoodsAuthorizationCatsResponse.GoodsAuthCatsGetResponseGoodsCatsListItem> goodsCatsList = goodsAuthCatsGetResponse.getGoodsCatsList();
                if(goodsCatsList != null && goodsCatsList.size() > 0){
                    GoodsCatsDO one = goodsCatsService.getOne(new LambdaQueryWrapper<GoodsCatsDO>()
                            .eq(GoodsCatsDO::getCatId, catId));
                    if(one == null){
                        one = new GoodsCatsDO();
                    }
                    List<GoodsCatsVO> resList = new ArrayList<>();
                    GoodsCatsVO goodsCatsVO = new GoodsCatsVO();
                    goodsCatsVO.setCatId(catId);
                    goodsCatsVO.setCatName(one.getCatName());
                    goodsCatsVO.setParentCatId(one.getParentCatId());
                    for (PddGoodsAuthorizationCatsResponse.GoodsAuthCatsGetResponseGoodsCatsListItem item : goodsCatsList){
                        GoodsCatsVO sub = new GoodsCatsVO();
                        sub.setParentCatId(catId);
                        sub.setCatName(item.getCatName());
                        sub.setCatId(item.getCatId());
                        resList.add(sub);
                    }
                    goodsCatsVO.setSubGoodsCats(resList);
                    return goodsCatsVO;
                }
            }
        } catch (Exception e) {
            log.error(e.toString(),e);
        }
        return null;
    }

    /**
     * 逆向反查类目树
     *
     * @param catId
     */
    @Override
    public List<GoodsCatsDO> getParent(Long catId) {
        //查询当前叶子节点
        GoodsCatsDO one = goodsCatsService.getOne(new LambdaQueryWrapper<GoodsCatsDO>()
                .eq(GoodsCatsDO::getCatId, catId));
        if(one == null){
            return null;
        }
        List<GoodsCatsDO> goodsCatsDOs = new ArrayList<>();
        //分级向上查询 固定值4级 无需轮训递归
        if(one.getLevel().equals(1)){
            goodsCatsDOs.add(one);
        }else if(one.getLevel().equals(2)){
            GoodsCatsDO one1 = goodsCatsService.getOne(new LambdaQueryWrapper<GoodsCatsDO>()
                    .eq(GoodsCatsDO::getCatId, one.getParentCatId()));
            goodsCatsDOs.add(one1);
            goodsCatsDOs.add(one);
        }else if(one.getLevel().equals(3)){
            GoodsCatsDO one2 = goodsCatsService.getOne(new LambdaQueryWrapper<GoodsCatsDO>()
                    .eq(GoodsCatsDO::getCatId, one.getParentCatId()));
            GoodsCatsDO one3 = goodsCatsService.getOne(new LambdaQueryWrapper<GoodsCatsDO>()
                    .eq(GoodsCatsDO::getCatId, one2.getParentCatId()));
            goodsCatsDOs.add(one3);
            goodsCatsDOs.add(one2);
            goodsCatsDOs.add(one);
        }else if(one.getLevel().equals(4)){
            GoodsCatsDO one2 = goodsCatsService.getOne(new LambdaQueryWrapper<GoodsCatsDO>()
                    .eq(GoodsCatsDO::getCatId, one.getParentCatId()));
            GoodsCatsDO one3 = goodsCatsService.getOne(new LambdaQueryWrapper<GoodsCatsDO>()
                    .eq(GoodsCatsDO::getCatId, one2.getParentCatId()));
            GoodsCatsDO one4 = goodsCatsService.getOne(new LambdaQueryWrapper<GoodsCatsDO>()
                    .eq(GoodsCatsDO::getCatId, one3.getParentCatId()));
            goodsCatsDOs.add(one4);
            goodsCatsDOs.add(one3);
            goodsCatsDOs.add(one2);
            goodsCatsDOs.add(one);
        }
        return goodsCatsDOs;
    }
}
