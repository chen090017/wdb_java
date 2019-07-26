package com.wdb.pdd.api.service.goods.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pdd.pop.sdk.http.PopBaseHttpResponse;
import com.pdd.pop.sdk.http.PopHttpClient;
import com.pdd.pop.sdk.http.api.request.*;
import com.pdd.pop.sdk.http.api.response.*;
import com.wdb.pdd.api.pojo.entity.*;
import com.wdb.pdd.api.service.goods.IGoodsBizService;
import com.wdb.pdd.api.service.goods.IGoodsService;
import com.wdb.pdd.api.service.sys.IReguserService;
import com.wdb.pdd.common.exception.MyException;
import com.wdb.pdd.common.exception.handlers.EnumErrorCode;
import com.wdb.pdd.common.utils.IdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/5 0005
 * @描述 业务Service 不进行任何持久化ORM操作
 */
@Service
public class GoodsBizServiceImpl implements IGoodsBizService {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private PopHttpClient popHttpClient;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private IReguserService reguserService;

    /**
     * 获取该用户商品总数
     *
     * @param reguserId
     * @return
     */
    @Override
    public Integer getAllCount(Integer reguserId) {
        //查询是否存在用户
        ReguserDO byId = reguserService.getById(reguserId);
        if (byId == null) {
            log.error("不存在该用户_id:{}", reguserId);
            return 0;
        }
        //校验到期时间
        long l = System.currentTimeMillis();
        if (byId.getExpiresIn() < (l / 1000)) {
            byId = reguserService.refreshToken(reguserId);
            if (byId == null) {
                log.error("刷新Token失败_id:{}", reguserId);
                return 0;
            }
        }
        String accessToken = byId.getAccessToken();
        PddGoodsListGetRequest req = getRequest(1, 1, 10);
        try {
            PddGoodsListGetResponse pddGoodsListGetResponse = popHttpClient.syncInvoke(req, accessToken);
            PopBaseHttpResponse.ErrorResponse errorResponse = pddGoodsListGetResponse.getErrorResponse();
            if (errorResponse != null) {
                log.error(JSON.toJSONString(errorResponse));
            }
            PddGoodsListGetResponse.GoodsListGetResponse goodsListGetResponse = pddGoodsListGetResponse.getGoodsListGetResponse();
            return goodsListGetResponse.getTotalCount();
        } catch (Exception e) {
            log.error(e.toString(),e);
        }
        return 0;
    }

    /**
     * 执行商品列表读取分批传入下一个处理方法
     * -> 首先删除该用户下所有商品
     * -> 根据当前内存环境设置每次分页获取的数据量
     * -> 循环获取商品列表 单个传入下一个处理单商品查询
     *
     * @param id
     * @param accessToken
     */
    @Override
    public void getGoodsList2Batch(Integer id, String accessToken) {
        /**
         * 根据用户ID进行批量删除商品数据
         */
        log.info("执行删除所有商品 reguserId:{}", id);
        goodsService.removeAll(id);
        int total = 100;
        for (int i = 1; ; i++) {
            PddGoodsListGetRequest req = getRequest(i, 100, total);
            if (req == null) {
                break;
            }
            try {
                PddGoodsListGetResponse pddGoodsListGetResponse = popHttpClient.syncInvoke(req, accessToken);
                PopBaseHttpResponse.ErrorResponse errorResponse = pddGoodsListGetResponse.getErrorResponse();
                if (errorResponse != null) {
                    log.error(JSON.toJSONString(errorResponse));
                    break;
                }
                PddGoodsListGetResponse.GoodsListGetResponse goodsListGetResponse = pddGoodsListGetResponse.getGoodsListGetResponse();
                total = goodsListGetResponse.getTotalCount();
                List<PddGoodsListGetResponse.GoodsListGetResponseGoodsListItem> goodsList = goodsListGetResponse.getGoodsList();
                goodsList.forEach((v) -> {
                    goods2Bean(v.getGoodsId(), accessToken, id,null);
                });
            } catch (Exception e) {
                log.error("获取商品列表数据失败");
                log.error(e.toString(), e);
                break;
            }
        }
    }

    /**
     * 商品列表请求体分页封装
     *
     * @param page
     * @param pageSize
     * @param total
     * @return
     */
    private PddGoodsListGetRequest getRequest(Integer page, Integer pageSize, Integer total) {
        int handle = (page - 1) * pageSize;
        if ((total - handle) < 1) {
            return null;
        }
        PddGoodsListGetRequest req = new PddGoodsListGetRequest();
        req.setPage(page);
        req.setPageSize(pageSize);
        return req;
    }

    /**
     * 执行单个商品查询详情写入具体单个类
     */
    private void goods2Bean(Long goodsId, String accessToken, Integer reguserId,String id) {
        PddGoodsDetailGetRequest request = new PddGoodsDetailGetRequest();
        request.setGoodsId(goodsId);
        try {
            PddGoodsDetailGetResponse response = popHttpClient.syncInvoke(request, accessToken);
            PopBaseHttpResponse.ErrorResponse errorResponse = response.getErrorResponse();
            if (errorResponse != null) {
                log.error(JSON.toJSONString(errorResponse));
                if (errorResponse.getErrorCode() == 70031 || errorResponse.getErrorCode() == 70032) {
                    log.error("隔1s重试");
                    Thread.sleep(1000);
                    goods2Bean(goodsId, accessToken, reguserId,id);
                }
                return;
            }
            PddGoodsDetailGetResponse.GoodsDetailGetResponse goodsDetailGetResponse = response.getGoodsDetailGetResponse();
            log.info(JSON.toJSONString(goodsDetailGetResponse));
            GoodsDO goodsDO = response2GoodsBean(reguserId, goodsDetailGetResponse);
            goodsDO.setAddTime(new Date());
            if(id != null){
                goodsDO.setId(id);
            }
            goodsService.save(goodsDO);
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
    }

    /**
     * 更新单个商品后执行单个商品详情同步
     *
     * @param goodsId
     */
    @Override
    @Transactional
    public void update2Save(Long goodsId) {
        GoodsDO one = goodsService.getOne(new LambdaQueryWrapper<GoodsDO>().eq(GoodsDO::getGoodsId,goodsId), true);
        String id = one.getId();
        Integer reguserId = one.getReguserId();
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
        goodsService.removeById(id);
        goods2Bean(goodsId,accessToken,reguserId,id);
    }

    /**
     * 商品详情接口相应 置换GoodsBean
     *
     * @param reguserId
     * @param goodsDetailGetResponse
     * @return
     */
    @Override
    public GoodsDO response2GoodsBean(Integer reguserId, PddGoodsDetailGetResponse.GoodsDetailGetResponse goodsDetailGetResponse) {
        GoodsDO goodsDO = new GoodsDO();
        List<GoodsSkuDO> goodsSkuDOs = new ArrayList<>();
        List<GoodsPropertyDO> goodsPropertyDOs = new ArrayList<>();
        List<GoodsCarouselDO> goodsCarouselDOs = new ArrayList<>();
        GoodsOverseaDO goodsOverseaDO = new GoodsOverseaDO();
        //数据复制到bean
        BeanUtil.copyProperties(goodsDetailGetResponse, goodsDO);
        String goodsId = IdUtil.getStrId();
        goodsDO.setId(goodsId);
        goodsDO.setReguserId(reguserId);
        goodsDO.setGoodsPropertyList(null);
        PddGoodsDetailGetResponse.GoodsDetailGetResponseOverseaGoods overseaGoods = goodsDetailGetResponse.getOverseaGoods();
        /**
         * 部分数据写入不正确json格式重新转换
         * private List<String> detailGalleryList;
         * private List<String> carouselGalleryList;
         */
        goodsDO.setDetailGalleryList(null);
        goodsDO.setCarouselGalleryList(null);
        if (goodsDetailGetResponse.getDetailGalleryList() != null && goodsDetailGetResponse.getDetailGalleryList().size() > 0) {
            goodsDO.setDetailGalleryList(JSON.toJSONString(goodsDetailGetResponse.getDetailGalleryList()));
        }
        if (goodsDetailGetResponse.getCarouselGalleryList() != null && goodsDetailGetResponse.getCarouselGalleryList().size() > 0) {
            goodsDO.setCarouselGalleryList(JSON.toJSONString(goodsDetailGetResponse.getCarouselGalleryList()));
        }
        if (overseaGoods != null) {
            //海外数据复制赋值
            BeanUtil.copyProperties(overseaGoods, goodsOverseaDO);
        }
        List<PddGoodsDetailGetResponse.GoodsDetailGetResponseSkuListItem> skuList = goodsDetailGetResponse.getSkuList();
        if (skuList != null && skuList.size() > 0) {
            for (PddGoodsDetailGetResponse.GoodsDetailGetResponseSkuListItem skuItem : skuList) {
                GoodsSkuDO goodsSkuDO = new GoodsSkuDO();
                BeanUtil.copyProperties(skuItem, goodsSkuDO);
                List<PddGoodsDetailGetResponse.GoodsDetailGetResponseSkuListItemSpecItem> spec = skuItem.getSpec();
                PddGoodsDetailGetResponse.GoodsDetailGetResponseSkuListItemOverseaSku overseaSku = skuItem.getOverseaSku();
                List<GoodsSkuSpecDO> goodsSkuSpecDOs = new ArrayList<>();
                if (spec != null && spec.size() > 0) {
                    for (PddGoodsDetailGetResponse.GoodsDetailGetResponseSkuListItemSpecItem specItem : spec) {
                        GoodsSkuSpecDO goodsSkuSpecDO = new GoodsSkuSpecDO();
                        BeanUtil.copyProperties(specItem, goodsSkuSpecDO);
                        goodsSkuSpecDOs.add(goodsSkuSpecDO);
                    }
                    goodsSkuDO.setSpec(Base64Encoder.encode(JSONArray.toJSONString(goodsSkuSpecDOs)));
                }
                if (overseaSku != null) {
                    GoodsSkuOverseaDO goodsSkuOverseaDO = new GoodsSkuOverseaDO();
                    BeanUtil.copyProperties(overseaSku, goodsSkuOverseaDO);
                    goodsSkuDO.setOverseaSku(Base64Encoder.encode(JSON.toJSONString(goodsSkuOverseaDO)));
                }
                //处理完Bean 将sku数据塞入
                goodsSkuDOs.add(goodsSkuDO);
            }
        }
        List<PddGoodsDetailGetResponse.GoodsDetailGetResponseGoodsPropertyListItem> goodsPropertyList = goodsDetailGetResponse.getGoodsPropertyList();
        if (goodsPropertyList != null && goodsPropertyList.size() > 0) {
            for (PddGoodsDetailGetResponse.GoodsDetailGetResponseGoodsPropertyListItem propertyItem : goodsPropertyList) {
                GoodsPropertyDO goodsPropertyDO = new GoodsPropertyDO();
                BeanUtil.copyProperties(propertyItem, goodsPropertyDO);
                goodsPropertyDOs.add(goodsPropertyDO);
            }
        }
        List<PddGoodsDetailGetResponse.GoodsDetailGetResponseCarouselVideoItem> carouselVideo = goodsDetailGetResponse.getCarouselVideo();
        if (carouselVideo != null && carouselVideo.size() > 0) {
            for (PddGoodsDetailGetResponse.GoodsDetailGetResponseCarouselVideoItem carouselItem : carouselVideo) {
                GoodsCarouselDO goodsCarouselDO = new GoodsCarouselDO();
                BeanUtil.copyProperties(carouselItem, goodsCarouselDO);
                goodsCarouselDOs.add(goodsCarouselDO);
            }
        }
        if (goodsSkuDOs != null && goodsSkuDOs.size() > 0) {
            goodsDO.setSkuList(JSONArray.toJSONString(goodsSkuDOs));
        }
        if (goodsPropertyDOs != null && goodsPropertyDOs.size() > 0) {
            goodsDO.setGoodsPropertyList(JSONArray.toJSONString(goodsPropertyDOs));
        }
        if (goodsCarouselDOs != null && goodsCarouselDOs.size() > 0) {
            goodsDO.setCarouselVideo(JSONArray.toJSONString(goodsCarouselDOs));
        }
        if (goodsOverseaDO != null && goodsOverseaDO.isNotNull()) {
            goodsDO.setOverseaGoods(JSON.toJSONString(goodsOverseaDO));
        }
        return goodsDO;
    }

    /**
     * 删除下架商品
     *
     * @param ids
     * @param reguserId
     */
    @Override
    public boolean remove(List<Long> ids, Integer reguserId) {
        //查询是否存在用户
        ReguserDO byId = reguserService.getById(reguserId);
        if (byId == null) {
            log.error("不存在该用户_id:{}", reguserId);
            return false;
        }
        //校验到期时间
        long l = System.currentTimeMillis();
        if (byId.getExpiresIn() < (l / 1000)) {
            byId = reguserService.refreshToken(reguserId);
            if (byId == null) {
                log.error("刷新Token失败_id:{}", reguserId);
                return false;
            }
        }
        String accessToken = byId.getAccessToken();
        PddDeleteGoodsCommitRequest pddDeleteGoodsCommitRequest = new PddDeleteGoodsCommitRequest();
        pddDeleteGoodsCommitRequest.setGoodsIds(ids);
        PddDeleteGoodsCommitResponse pddDeleteGoodsCommitResponse = null;
        try {
            pddDeleteGoodsCommitResponse = popHttpClient.syncInvoke(pddDeleteGoodsCommitRequest, accessToken);
        } catch (Exception e) {
            log.error(e.toString(), e);
            throw new MyException(e.getMessage());
        }
        PopBaseHttpResponse.ErrorResponse errorResponse = pddDeleteGoodsCommitResponse.getErrorResponse();
        if (errorResponse != null) {
            throw new MyException(errorResponse.getErrorMsg());
        }
        Boolean openApiResponse = pddDeleteGoodsCommitResponse.getOpenApiResponse();
        return openApiResponse;
    }

    /**
     * 批量上下架商品
     *
     * @param ids
     * @param reguserId
     * @param isOnsale
     */
    @Override
    public HashMap<String,Object> unline(List<Long> ids, Integer reguserId, Integer isOnsale) {
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
        List<String> res = new ArrayList<>();
        HashSet<Long> set = new HashSet<>();
        for (Long id : ids) {
            PddGoodsSaleStatusSetRequest pddGoodsSaleStatusSetRequest = new PddGoodsSaleStatusSetRequest();
            pddGoodsSaleStatusSetRequest.setGoodsId(id);
            pddGoodsSaleStatusSetRequest.setIsOnsale(isOnsale);
            try {
                one2Unline(pddGoodsSaleStatusSetRequest,accessToken);
            } catch (Exception e) {
                log.error(e.toString(),e);
                res.add("id:"+id+" "+"异常:"+e.getMessage());
                set.add(id);
            }
        }
        HashMap<String, Object> resMap = new HashMap<>();
        resMap.put("res",res);
        resMap.put("set",set);
        return resMap;
    }

    private void one2Unline(PddGoodsSaleStatusSetRequest request, String accessToken) throws Exception {
        PddGoodsSaleStatusSetResponse pddGoodsSaleStatusSetResponse = popHttpClient.syncInvoke(request, accessToken);
        PopBaseHttpResponse.ErrorResponse errorResponse = pddGoodsSaleStatusSetResponse.getErrorResponse();
        if(errorResponse != null){
            if(errorResponse.getErrorCode() == 70031 || errorResponse.getErrorCode() == 70032){
                Thread.sleep(1000);
                one2Unline(request,accessToken);
                return;
            }
            throw new MyException(errorResponse.getErrorMsg());
        }
        PddGoodsSaleStatusSetResponse.GoodsSaleStatusSetResponse goodsSaleStatusSetResponse = pddGoodsSaleStatusSetResponse.getGoodsSaleStatusSetResponse();
        Boolean isSuccess = goodsSaleStatusSetResponse.getIsSuccess();
        if(!isSuccess){
            throw new MyException("更新失败，原因未知。请同步商品后重试！");
        }
    }

    /**
     * 商品图片上传
     *
     * @param request
     * @return
     */
    @Override
    public PddGoodsImageUploadResponse imgUpload(PddGoodsImageUploadRequest request,Integer reguserId) {
        //查询是否存在用户
        ReguserDO byId = reguserService.getById(reguserId);
//        if (byId == null) {
//            log.error("不存在该用户_id:{}", reguserId);
//            throw new MyException(EnumErrorCode.apiAuthorizationFailed.getCodeStr());
//        }
//        //校验到期时间
//        long l = System.currentTimeMillis();
//        if (byId.getExpiresIn() < (l / 1000)) {
//            byId = reguserService.refreshToken(reguserId);
//            if (byId == null) {
//                log.error("刷新Token失败_id:{}", reguserId);
//                throw new MyException(EnumErrorCode.apiAuthorizationFailed.getCodeStr());
//            }
//        }
        String accessToken = byId.getAccessToken();
        try {
            PddGoodsImageUploadResponse pddGoodsImageUploadResponse = popHttpClient.syncInvoke(request, accessToken);
            return pddGoodsImageUploadResponse;
        } catch (Exception e) {
            log.error(e.toString(),e);
        }
        return null;
    }

    /**
     * 获取商品更新编辑id
     *
     * @param goodsId
     * @param reguserId
     * @return
     */
    @Override
    public PddGoodsInformationUpdateResponse getGoodsCommitId(Long goodsId, Integer reguserId) {
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
        PddGoodsDetailGetRequest pddGoodsDetailGetRequest = new PddGoodsDetailGetRequest();
        PddGoodsInformationUpdateRequest pddGoodsInformationUpdateRequest = new PddGoodsInformationUpdateRequest();
        pddGoodsDetailGetRequest.setGoodsId(goodsId);
        try {
            PddGoodsDetailGetResponse pddGoodsDetailGetResponse = popHttpClient.syncInvoke(pddGoodsDetailGetRequest, accessToken);
            PopBaseHttpResponse.ErrorResponse errorResponse = pddGoodsDetailGetResponse.getErrorResponse();
            if (errorResponse != null) {
                log.error(JSON.toJSONString(errorResponse));
                if (errorResponse.getErrorCode().equals(70031) || errorResponse.getErrorCode().equals(70032)) {
                    log.error("隔1s重试");
                    Thread.sleep(1000);
                    return getGoodsCommitId(goodsId, reguserId);
                }
                throw new MyException(errorResponse.getErrorMsg());
            }
            PddGoodsDetailGetResponse.GoodsDetailGetResponse goodsDetailGetResponse = pddGoodsDetailGetResponse.getGoodsDetailGetResponse();
            //拷贝详情生成
            BeanUtil.copyProperties(goodsDetailGetResponse, pddGoodsInformationUpdateRequest,"isCustoms","isPreSale","isRefundable","secondHand","isFolt");
            pddGoodsInformationUpdateRequest.setIsCustoms(goodsDetailGetResponse.getIsCustoms()==1);
            pddGoodsInformationUpdateRequest.setIsPreSale(goodsDetailGetResponse.getIsPreSale()==1);
            pddGoodsInformationUpdateRequest.setIsRefundable(goodsDetailGetResponse.getIsRefundable()==1);
            pddGoodsInformationUpdateRequest.setSecondHand(goodsDetailGetResponse.getSecondHand()==1);
            pddGoodsInformationUpdateRequest.setIsFolt(goodsDetailGetResponse.getIsFolt()==1);
            Long buy = (goodsDetailGetResponse.getBuyLimit() != null && goodsDetailGetResponse.getBuyLimit().equals(99999L)) ? 999999 : goodsDetailGetResponse.getBuyLimit();
            pddGoodsInformationUpdateRequest.setBuyLimit(buy);
            Long order = (goodsDetailGetResponse.getOrderLimit() != null && goodsDetailGetResponse.getOrderLimit().equals(99999L)) ? 999999 : goodsDetailGetResponse.getOrderLimit();
            pddGoodsInformationUpdateRequest.setOrderLimit(order);
            List<PddGoodsDetailGetResponse.GoodsDetailGetResponseSkuListItem> skuList = goodsDetailGetResponse.getSkuList();
            //这里将sku规格写入
            if(skuList != null && skuList.size() > 0){
                List<PddGoodsInformationUpdateRequest.SkuListItem> items = new ArrayList<>();
                for(PddGoodsDetailGetResponse.GoodsDetailGetResponseSkuListItem item : skuList){
                    PddGoodsInformationUpdateRequest.SkuListItem skuListItem = new PddGoodsInformationUpdateRequest.SkuListItem();
                    BeanUtil.copyProperties(item,skuListItem);
                    //这里为增量而不是全量 故设置0
                    skuListItem.setQuantity(0L);
                    List<PddGoodsDetailGetResponse.GoodsDetailGetResponseSkuListItemSpecItem> spec = item.getSpec();
                    List<Long> specIds = new ArrayList<>();
                    for(PddGoodsDetailGetResponse.GoodsDetailGetResponseSkuListItemSpecItem i : spec){
                        Long specId = i.getSpecId();
                        specIds.add(specId);
                    }
                    skuListItem.setSpecIdList(JSON.toJSONString(specIds));
                    items.add(skuListItem);
                }
                pddGoodsInformationUpdateRequest.setSkuList(items);
            }
            //这里将商品属性值写入
            List<PddGoodsDetailGetResponse.GoodsDetailGetResponseGoodsPropertyListItem> goodsPropertyList = goodsDetailGetResponse.getGoodsPropertyList();
            if(goodsPropertyList != null && goodsPropertyList.size() > 0){
                List<PddGoodsInformationUpdateRequest.GoodsPropertiesItem> items = new ArrayList<>();
                for(PddGoodsDetailGetResponse.GoodsDetailGetResponseGoodsPropertyListItem item : goodsPropertyList){
                    PddGoodsInformationUpdateRequest.GoodsPropertiesItem goodsPropertiesItem = new PddGoodsInformationUpdateRequest.GoodsPropertiesItem();
                    goodsPropertiesItem.setVid(item.getVid());
                    goodsPropertiesItem.setValue(item.getVvalue());
                    goodsPropertiesItem.setTemplatePid(item.getTemplatePid());
                    goodsPropertiesItem.setValueUnit(item.getPunit());
                    items.add(goodsPropertiesItem);
                }
                pddGoodsInformationUpdateRequest.setGoodsProperties(items);
            }
            //这里将商品轮播图写入
            List<String> detailGalleryList = goodsDetailGetResponse.getDetailGalleryList();
            List<String> carouselGalleryList = goodsDetailGetResponse.getCarouselGalleryList();
            pddGoodsInformationUpdateRequest.setDetailGallery(detailGalleryList);
            pddGoodsInformationUpdateRequest.setCarouselGallery(carouselGalleryList);
            //一些其他参数
            pddGoodsInformationUpdateRequest.setOutGoodsId(goodsDetailGetResponse.getOuterGoodsId());
            PddGoodsInformationUpdateResponse pddGoodsInformationUpdateResponse = popHttpClient.syncInvoke(pddGoodsInformationUpdateRequest, accessToken);
            return pddGoodsInformationUpdateResponse;
        } catch (Exception e) {
            log.error(e.toString(), e);
            return null;
        }
    }
}
