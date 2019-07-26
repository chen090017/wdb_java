package com.wdb.pdd.api.service.goods.impl;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pdd.pop.sdk.http.api.request.*;
import com.pdd.pop.sdk.http.api.request.PddGoodsInformationUpdateRequest.SkuListItem;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pdd.pop.sdk.http.PopBaseHttpResponse;
import com.pdd.pop.sdk.http.PopHttpClient;
import com.pdd.pop.sdk.http.api.response.*;
import com.wdb.pdd.api.dao.goods.GoodsDao;
import com.wdb.pdd.api.pojo.entity.*;
import com.wdb.pdd.api.pojo.vo.*;
import com.wdb.pdd.api.service.cats.IGoodsCatsBizService;
import com.wdb.pdd.api.service.goods.IGoodsBizService;
import com.wdb.pdd.api.service.goods.IGoodsService;
import com.wdb.pdd.api.service.logistics.ILogisticsTemplateService;
import com.wdb.pdd.api.service.sys.IReguserService;
import com.wdb.pdd.common.exception.MyException;
import com.wdb.pdd.common.exception.handlers.EnumErrorCode;
import com.wdb.pdd.common.utils.PageUtils;
import com.wdb.pdd.common.utils.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pdd.pop.sdk.common.util.JsonUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/5 0005
 * @描述
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsDao, GoodsDO> implements IGoodsService {

    @Autowired
    private IGoodsCatsBizService goodsCatsBizService;
    @Autowired
    private ILogisticsTemplateService logisticsTemplateService;

    @Autowired
    private IReguserService reguserService;

    @Autowired
    private PopHttpClient popHttpClient;




    private Logger log = LoggerFactory.getLogger(getClass());

    @Transactional
    @Override
    public void removeAll(Integer reguserId) {
        this.remove(new LambdaQueryWrapper<GoodsDO>()
            .eq(GoodsDO::getReguserId,reguserId));
    }

    /**
     * 分页查询用户商品表数据
     *
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPage(Map params) {
        IPage<GoodsDO> listByPage = this.baseMapper.selectPage(new Query<GoodsDO>(params).getPage(), new LambdaQueryWrapper<GoodsDO>()
                .eq(GoodsDO::getReguserId, params.get("reguserId"))
                .eq(params.get("costTemplateId")!=null,GoodsDO::getCostTemplateId, params.get("costTemplateId"))
                .eq(params.get("catId")!=null,GoodsDO::getCatId,params.get("catId"))
                .eq(params.get("isPreSale")!=null,GoodsDO::getIsPreSale,params.get("isPreSale"))
                .eq(params.get("status") != null,GoodsDO::getStatus,params.get("status"))
                .eq(params.get("goodsId") != null,GoodsDO::getGoodsId,params.get("goodsId"))
                .like(params.get("goodsName") != null,GoodsDO::getGoodsName,params.get("goodsName"))
        );
        List<GoodsDO> records = listByPage.getRecords();
        //新建VO承载变量数据
        List<GoodsVO> resList = new ArrayList<>();
        if(records != null && records.size() > 0 ){
            for(GoodsDO goodsDO : records){
                GoodsVO goodsVO = goodsDO2Response(goodsDO);
                //加上类目名称
                List<GoodsCatsDO> cats = goodsCatsBizService.getParent(goodsVO.getCatId());
                GoodsLogisticsTemplateDO one = logisticsTemplateService.getOne(new LambdaQueryWrapper<GoodsLogisticsTemplateDO>()
                        .eq(GoodsLogisticsTemplateDO::getReguserId, goodsDO.getReguserId())
                        .eq(GoodsLogisticsTemplateDO::getTemplateId, goodsDO.getCostTemplateId()));
                goodsVO.setCostTemplateName(one == null ? null : one.getTemplateName());
                goodsVO.setCats(cats);
                resList.add(goodsVO);
            }
        }
        //新建Page承载新数据
        IPage<GoodsVO> resPage = new Page<>();
        resPage.setCurrent(listByPage.getCurrent());
        resPage.setPages(listByPage.getPages());
        resPage.setSize(listByPage.getSize());
        resPage.setTotal(listByPage.getTotal());
        resPage.setRecords(resList);
        return new PageUtils(resPage);
    }

    /**
     * 将数据库中存储的转换数据 转成相应对象
     * @param goodsDO
     * @return
     */
    public GoodsVO goodsDO2Response(GoodsDO goodsDO) {
        GoodsVO goodsVO = new GoodsVO();
        /**
         * 将数据格式不一致的取出并置null防止无法Bean拷贝
         *  private List<GoodsPropertyDO> goodsPropertyList;
         *  private List<GoodsSkuVO> skuList;
         *  private GoodsOverseaDO overseaGoods;
         *  private List<GoodsCarouselDO> carouselVideo;
         *  private List<String> detailGalleryList;
         *  private List<String> carouselGalleryList;
         */
        String skuList = goodsDO.getSkuList();
        String goodsPropertyList = goodsDO.getGoodsPropertyList();
        String overseaGoods = goodsDO.getOverseaGoods();
        String carouselVideo = goodsDO.getCarouselVideo();
        String carouselGalleryList = goodsDO.getCarouselGalleryList();
        String detailGalleryList = goodsDO.getDetailGalleryList();
        goodsDO.setSkuList(null);
        goodsDO.setGoodsPropertyList(null);
        goodsDO.setOverseaGoods(null);
        goodsDO.setCarouselVideo(null);
        goodsDO.setCarouselGalleryList(null);
        goodsDO.setDetailGalleryList(null);
        //直接拷贝第一层数据
        BeanUtil.copyProperties(goodsDO,goodsVO);
        //二层数据处理JSON转对象
        Long quantity = 0L;
        List<Long> multiPrices = new ArrayList<>();
        List<Long> prices = new ArrayList<>();
        if(skuList != null){
            List<GoodsSkuDO> goodsSkuDOs = JSON.parseArray(skuList, GoodsSkuDO.class);
            List<GoodsSkuVO> goodsSkuVOs = new ArrayList<>();
            if(goodsSkuDOs != null && goodsSkuDOs.size() > 0){
                for(GoodsSkuDO goodsSkuDO : goodsSkuDOs){
                    quantity += goodsSkuDO.getQuantity();
                    multiPrices.add(goodsSkuDO.getMultiPrice());
                    prices.add(goodsSkuDO.getPrice());
                    GoodsSkuVO goodsSkuVO = new GoodsSkuVO();
                    String spec = goodsSkuDO.getSpec();
                    String overseaSku = goodsSkuDO.getOverseaSku();
                    //去除多余无法转换参数
                    goodsSkuDO.setSpec(null);
                    goodsSkuDO.setOverseaSku(null);
                    //参数复制赋值
                    BeanUtil.copyProperties(goodsSkuDO,goodsSkuVO);
                    if(!StrUtil.isEmpty(spec)){
                        /**
                         * 这是嵌套参数值 为防止转义保存时base64转码
                         */
                        String decode = Base64Decoder.decodeStr(spec);
                        List<GoodsSkuSpecDO> goodsSkuSpecDOs = JSON.parseArray(decode, GoodsSkuSpecDO.class);
                        goodsSkuVO.setSpec(goodsSkuSpecDOs);
                    }
                    if(!StrUtil.isEmpty(overseaSku)){
                        /**
                         * 这是嵌套参数值 为防止转义保存时base64转码
                         */
                        String decode = Base64Decoder.decodeStr(overseaSku);
                        GoodsSkuOverseaDO goodsSkuOverseaDO = JSON.parseObject(decode, GoodsSkuOverseaDO.class);
                        goodsSkuVO.setOverseaSku(goodsSkuOverseaDO);
                    }
                    goodsSkuVOs.add(goodsSkuVO);
                }
            }
            goodsVO.setSkuList(goodsSkuVOs);
        }
        if(multiPrices.size() > 1){
            List<Long> sort = CollUtil.sort(multiPrices, (o1, o2) -> {
                return o1.compareTo(o2);
            });
            goodsVO.setMinMultiPrice(sort.get(0));
            goodsVO.setMaxMultiPrice(sort.get(sort.size() - 1));
        }else if(multiPrices.size() == 1){
            goodsVO.setMinMultiPrice(multiPrices.get(0));
            goodsVO.setMaxMultiPrice(multiPrices.get(0));
        }
        if(prices.size() > 1){
            List<Long> sort = CollUtil.sort(prices, (o1, o2) -> {
                return o1.compareTo(o2);
            });
            goodsVO.setMinPrice(sort.get(0));
            goodsVO.setMaxPrice(sort.get(sort.size() - 1));
        }else if(prices.size() == 1){
            goodsVO.setMinPrice(prices.get(0));
            goodsVO.setMaxPrice(prices.get(0));
        }
        goodsVO.setQuantity(quantity);
        if(goodsPropertyList != null){
            List<GoodsPropertyDO> goodsPropertyDOs = JSON.parseArray(goodsPropertyList, GoodsPropertyDO.class);
            goodsVO.setGoodsPropertyList(goodsPropertyDOs);
        }
        if(overseaGoods != null){
            GoodsOverseaDO goodsOverseaDO = JSON.parseObject(overseaGoods, GoodsOverseaDO.class);
            goodsVO.setOverseaGoods(goodsOverseaDO);
        }
        if(carouselVideo != null){
            List<GoodsCarouselDO> goodsCarouselDOs = JSON.parseArray(carouselVideo, GoodsCarouselDO.class);
            goodsVO.setCarouselVideo(goodsCarouselDOs);
        }
        if(carouselGalleryList != null){
            List<String> strings = JSON.parseArray(carouselGalleryList, String.class);
            goodsVO.setCarouselGalleryList(strings);
        }
        if(detailGalleryList != null){
            List<String> strings = JSON.parseArray(detailGalleryList, String.class);
            goodsVO.setDetailGalleryList(strings);
        }
        return goodsVO;
    }

    /**
     * 获取各商品状态数量
     *
     * @param params
     */
    @Override
    public Map<String,Integer> countByStatus(Map params) {
        //商品状态 1:上架，2：下架，3：售罄 4：已删除
        int online = this.count(new LambdaQueryWrapper<GoodsDO>()
                .eq(GoodsDO::getReguserId, params.get("reguserId"))
                .eq(GoodsDO::getStatus,1));
        int offline = this.count(new LambdaQueryWrapper<GoodsDO>()
                .eq(GoodsDO::getReguserId, params.get("reguserId"))
                .eq(GoodsDO::getStatus,2));
        int sellout = this.count(new LambdaQueryWrapper<GoodsDO>()
                .eq(GoodsDO::getReguserId, params.get("reguserId"))
                .eq(GoodsDO::getStatus,3));
        HashMap<String, Integer> res = new HashMap<>();
        res.put("online",online);
        res.put("offline",offline);
        res.put("sellout",sellout);
        return res;
    }
    @Override
      public GoodsDetail getGoodsByID(Integer reguserId, Long goodsId)  {
        GoodsDetail goodsDetail=new GoodsDetail();
         ReguserDO byId = reguserService.getById(reguserId);

         String accessToken = byId.getAccessToken();
         PddGoodsDetailGetRequest request = new PddGoodsDetailGetRequest();
        request.setGoodsId(goodsId);
        try {

             PddGoodsDetailGetResponse  response = popHttpClient.syncInvoke(request, accessToken);


             if(response!=null){

                 PopBaseHttpResponse.ErrorResponse errorResponse = response.getErrorResponse();
                 if (errorResponse != null) {
                     throw new MyException(errorResponse.getErrorMsg());
                 }
                 PddGoodsDetailGetResponse.GoodsDetailGetResponse getGoodsDetailGetResponse= response.getGoodsDetailGetResponse();
                 goodsDetail.setSkuList(getGoodsDetailGetResponse.getSkuList());
                 goodsDetail.setGoodsName(getGoodsDetailGetResponse .getGoodsName());
                 goodsDetail.setGoodsId(getGoodsDetailGetResponse.getGoodsId());
                 goodsDetail.setCarouselGalleryList(getGoodsDetailGetResponse.getCarouselGalleryList());
                 goodsDetail.setCostTemplateId(getGoodsDetailGetResponse.getCostTemplateId());
                 goodsDetail.setGoodsDesc(getGoodsDetailGetResponse.getGoodsDesc());
                 goodsDetail.setMarketPrice(getGoodsDetailGetResponse.getMarketPrice());
                 goodsDetail.setIsPreSale(getGoodsDetailGetResponse.getIsPreSale());
                 goodsDetail.setPreSaleTime(getGoodsDetailGetResponse.getPreSaleTime());
                 goodsDetail.setShipmentLimitSecond(getGoodsDetailGetResponse.getShipmentLimitSecond());
                 goodsDetail.setIsRefundable(getGoodsDetailGetResponse.getIsRefundable());
                 goodsDetail.setIsFolt(getGoodsDetailGetResponse.getIsFolt());
                 goodsDetail.setImageUrl(getGoodsDetailGetResponse.getImageUrl());
                 goodsDetail.setDetailGalleryList(getGoodsDetailGetResponse.getDetailGalleryList());
                 goodsDetail.setTinyName(getGoodsDetailGetResponse.getTinyName());
                 goodsDetail.setCountryId(getGoodsDetailGetResponse.getCountryId());
                 goodsDetail.setGoodsType(getGoodsDetailGetResponse.getGoodsType());
                 goodsDetail.setCatId(getGoodsDetailGetResponse.getCatId());
                 List<PddGoodsDetailGetResponse.GoodsDetailGetResponseSkuListItem> skuListItemList= getGoodsDetailGetResponse.getSkuList();
                 List<Long> multiPrices = new ArrayList<>();
                 List<Long> prices = new ArrayList<>();
                 Long quantity = 0L;
                 for(PddGoodsDetailGetResponse.GoodsDetailGetResponseSkuListItem goodsSkuItem : skuListItemList ) {
                     quantity += goodsSkuItem.getQuantity();
                     multiPrices.add(goodsSkuItem.getMultiPrice());
                     prices.add(goodsSkuItem.getPrice());
                 }


                 if(multiPrices.size() > 1){
                     List<Long> sort = CollUtil.sort(multiPrices, (o1, o2) -> {
                         return o1.compareTo(o2);
                     });
                     goodsDetail.setMinMultiPrice(sort.get(0));
                     goodsDetail.setMaxMultiPrice(sort.get(sort.size() - 1));
                 }else if(multiPrices.size() == 1){
                     goodsDetail.setMinMultiPrice(multiPrices.get(0));
                     goodsDetail.setMaxMultiPrice(multiPrices.get(0));
                 }
                 if(prices.size() > 1){
                     List<Long> sort = CollUtil.sort(prices, (o1, o2) -> {
                         return o1.compareTo(o2);
                     });
                     goodsDetail.setMinPrice(sort.get(0));
                     goodsDetail.setMaxPrice(sort.get(sort.size() - 1));
                 }else if(prices.size() == 1){
                     goodsDetail.setMinPrice(prices.get(0));
                     goodsDetail.setMaxPrice(prices.get(0));
                 }
                 goodsDetail.setQuantity(quantity);
                 goodsDetail.setOuterGoodsId(getGoodsDetailGetResponse.getOuterGoodsId());


                 List<GoodsCatsDO> cats = goodsCatsBizService.getParent(getGoodsDetailGetResponse.getCatId());
                 goodsDetail.setCats(cats);




             }
             }catch (Exception e) {
             log.error(e.toString(), e);
             throw new MyException(e);
         }

         return  goodsDetail;
    }
    @Override
    public PddGoodsImageUploadResponse imgUpload2(Integer reguserId, String img){
        ReguserDO byId = reguserService.getById(reguserId);
        String accessToken = byId.getAccessToken();
        PddGoodsImageUploadRequest request = new PddGoodsImageUploadRequest();
         System.out.println(img);
        request.setImage(img);
        try {
            PddGoodsImageUploadResponse pddGoodsImageUploadResponse = popHttpClient.syncInvoke(request, accessToken);
            return pddGoodsImageUploadResponse;
        } catch (Exception e) {
            log.error(e.toString(),e);
        }
         return null;
    }

    @Override
    public Map<String, Object> goodsUpdate(Map<String,Object>  params) {
       Object reguserId=params.get("reguserId");
       Long goodsId= Long.valueOf(String.valueOf(params.get("goodsId"))).longValue();
        ReguserDO byId = reguserService.getById(Integer.parseInt(reguserId.toString()));
        String accessToken = byId.getAccessToken();

       Boolean isPreSale=null;
        isPreSale=params.get("isPreSale").toString().equals("1")?true:false;
        Boolean IsRefundable=null;//是否7天无理由退换货，true-支持，false-不支持
        IsRefundable=params.get("isRefundable").toString().equals("1")?true:false;
        Boolean isFolt=null;//是否7天无理由退换货，true-支持，false-不支持
        isFolt=params.get("isFolt").toString().equals("1")?true:false;

        GoodsDetail goodsDetail=new GoodsDetail();
//        goodsDetail.setGoodsName(params.get("costTemplateId"));
        HashMap<String, Object> data = new HashMap<>();
        data.put("cdz2","冬至");
        data.put("TemplateId",reguserId);
        data.put("name",params.get("name"));
        data.put("cats",params.get("cats"));
        data.put("goodsId",goodsId);
        PddGoodsInformationUpdateRequest request = new PddGoodsInformationUpdateRequest();
        request.setGoodsId(goodsId);
        request.setGoodsName(params.get("goodsName").toString());
        request.setGoodsType(Integer.parseInt(params.get("goodsType").toString()));
        request.setGoodsDesc(params.get("goodsDesc").toString());
        request.setCatId(Long.valueOf(String.valueOf(params.get("catId"))).longValue());
        request.setTinyName(params.get("tinyName").toString());
        request.setCountryId(Integer.parseInt(params.get("countryId").toString()));
//        request.setWarehouse(params.get());
//        request.setCustoms(params.get());
//        request.setIsCustoms(params.get());
        request.setMarketPrice(Long.valueOf(String.valueOf(params.get("marketPrice"))).longValue());
        request.setIsPreSale(isPreSale);
        if(isPreSale){

            request.setPreSaleTime(Long.valueOf(String.valueOf(params.get("preSaleTime"))));
        }
        request.setShipmentLimitSecond(Long.valueOf(String.valueOf(params.get("shipmentLimitSecond"))).longValue());
        request.setCostTemplateId(Long.valueOf(String.valueOf(params.get("costTemplateId"))).longValue());
//        request.setCustomerNum(params.get());
//        request.setBuyLimit(params.get());
//        request.setOrderLimit(params.get());
        request.setIsRefundable(IsRefundable);
//        request.setSecondHand(params.get());
        request.setIsFolt(isFolt);


        List<GoodsSku> ss=( List<GoodsSku>)params.get("skuList");

        String skustr=JSONArray.toJSONString(params.get("skuList"));
        System.out.println(params.get("skuList"));
        List<GoodsSkuVON> list2 = JSONArray.parseArray(skustr,  GoodsSkuVON.class);
        List<SkuListItem> skuList = new ArrayList<SkuListItem>();
        List<GoodsSkuVON> goodsSkuVOs = new ArrayList<>();

   for(GoodsSkuVON goodsSkuVO : list2){
      SkuListItem item = new SkuListItem();
      item.setThumbUrl(goodsSkuVO.getThumbUrl());
      item.setLength(goodsSkuVO.getLength());
      item.setSkuId(goodsSkuVO.getSkuId());

      Long marketPrice = goodsSkuVO.getMultiPrice();


      List<Long>  SpecIdListArr=new ArrayList<>();
     for( GoodsSkuSpecDO skuSpe:goodsSkuVO.getSpec()){
         SpecIdListArr.add(skuSpe.getSpecId());
      }
      item.setSpecIdList(SpecIdListArr.toString());
      item.setWeight(goodsSkuVO.getWeight());
//       goodsSkuVO.getQuantity()
      item.setQuantity(0L);// 库存 全量修改

       quantityUpdate(accessToken,goodsId,goodsSkuVO.getSkuId(),goodsSkuVO.getQuantity());
       item.setOutSkuSn(goodsSkuVO.getOutSkuSn());

      item.setMultiPrice(goodsSkuVO.getNewMultiPrice());
      item.setPrice(goodsSkuVO.getNewPrice());
      item.setLimitQuantity(goodsSkuVO.getLimitQuantity());
      item.setIsOnsale(goodsSkuVO.getIsOnsale());

      skuList.add(item);
  }

//        PddGoodsQuantityUpdateRequest  QuantityUpdateRequest = new PddGoodsQuantityUpdateRequest();
//        QuantityUpdateRequest.setGoodsId(goodsId);
//        QuantityUpdateRequest.setQuantity(555L);
//        QuantityUpdateRequest.setSkuId(138930400623L);
//        try {
//            PddGoodsQuantityUpdateResponse QuantityUpdateresponse = popHttpClient.syncInvoke(QuantityUpdateRequest,accessToken);
//        }catch (Exception e) {
//            log.error(e.toString(), e);
//            throw new MyException("拼多多库存接口调用异常");
//        }

        unline(goodsId,accessToken, 1);// 更新状态

        request.setSkuList(skuList);

        request.setOutGoodsId(params.get("outerGoodsId").toString());

         request.setImageUrl(params.get("imageUrl").toString());
//        List<String> carouselGallery = new ArrayList<String>();
//        carouselGallery.add("str");
//        request.setCarouselGallery(carouselGallery);

        String carouselGalleryListstr=JSONObject.toJSONString(params.get("carouselGalleryList"));
        List<String> carouselGallery = JSONObject.parseArray(carouselGalleryListstr,  String.class);
        request.setCarouselGallery(carouselGallery);


        String detailGalleryListstr=JSONObject.toJSONString(params.get("carouselGalleryList"));
        List<String> detailGallery = JSONObject.parseArray(detailGalleryListstr,  String.class);
        request.setDetailGallery(detailGallery);
//        request.setGoodsProperties(goodsProperties);  //商品属性列表
//        request.setQuanGuoLianBao(0);
//        request.setZhiHuanBuXiu(0);
//        request.setOverseaGoods(overseaGoods);
//        request.setSongHuoRuHu("str");
//        request.setShangMenAnZhuang("str");
//        request.setSongHuoAnZhuang("str");
//        request.setMaiJiaZiTi("str");
//        request.setBadFruitClaim(0);
//        request.setLackOfWeightClaim(0);
//        request.setCarouselVideo(carouselVideo);
//        request.setOriginCountryId(0);
        data.put("data",params.get("skuList"));
        data.put("list",list2);
        data.put("list2",skuList);
        data.put("dat",request);
        data.put("isFolt",params.get("isFolt").toString().equals("1"));
        data.put("skutr",skustr);
        data.put("detailGallery",detailGallery);
try {
        PddGoodsInformationUpdateResponse response = popHttpClient.syncInvoke(request, accessToken);
if(response.getErrorResponse()!=null){
    data.put("isok",false);
    data.put("Erro",response.getErrorResponse());
}
    if(response.getGoodsUpdateResponse()!=null){
        data.put("isok",true);
        data.put("Success",response.getGoodsUpdateResponse());
    }



//    data.put("isok",response.getGoodsUpdateResponse());
//    data.put("response",response);
    }catch (Exception e) {
        log.error(e.toString(), e);
        throw new MyException("拼多多接口调用异常");
    }


        return data;
    }

    @Override
    public Map<String, Object> goodsCommitDetailGet(Integer reguserId,Long  CommitId) {
        HashMap<String, Object> data = new HashMap<>();
        ReguserDO byId = reguserService.getById(reguserId);
        String accessToken = byId.getAccessToken();
        PddGoodsCommitDetailGetRequest request = new PddGoodsCommitDetailGetRequest();
        request.setGoodsCommitId(CommitId);
        try {
            PddGoodsCommitDetailGetResponse response = popHttpClient.syncInvoke(request, accessToken);
            data.put("response",response);
        }catch (Exception e) {
            log.error(e.toString(), e);
            throw new MyException("拼多多接口调用异常");
        }
        return data;
    }

    private void quantityUpdate(String accessToken,Long goodsId,Long skuId,Long quantity){
        //具体处理单个Sku全量覆盖库存
        PddGoodsQuantityUpdateRequest pddGoodsQuantityUpdateRequest = new PddGoodsQuantityUpdateRequest();
        pddGoodsQuantityUpdateRequest.setGoodsId(goodsId);
        pddGoodsQuantityUpdateRequest.setSkuId(skuId);
        pddGoodsQuantityUpdateRequest.setQuantity(quantity);
        try {
            PddGoodsQuantityUpdateResponse pddGoodsQuantityUpdateResponse = popHttpClient.syncInvoke(pddGoodsQuantityUpdateRequest, accessToken);
            if(pddGoodsQuantityUpdateResponse != null){
                PopBaseHttpResponse.ErrorResponse errorResponse = pddGoodsQuantityUpdateResponse.getErrorResponse();
                if(errorResponse != null){
                    if(errorResponse.getErrorCode().equals(70031) || errorResponse.getErrorCode().equals(70032)){
                        log.error("隔1s重试");
                        Thread.sleep(1000);
                        quantityUpdate(accessToken,goodsId,skuId,quantity);
                    }else {
//                        msg.put(skuId,errorResponse.getErrorMsg());
                    }
                    return;
                }
                PddGoodsQuantityUpdateResponse.GoodsQuantityUpdateResponse goodsQuantityUpdateResponse = pddGoodsQuantityUpdateResponse.getGoodsQuantityUpdateResponse();
                if(goodsQuantityUpdateResponse != null){
                    if(goodsQuantityUpdateResponse.getIsSuccess()){
                        return;
                    }
                }

            }
        } catch (Exception e) {
            log.error(e.toString(),e);

        }

    }


    public void unline( Long id, String accessToken, Integer isOnsale){
        PddGoodsSaleStatusSetRequest pddGoodsSaleStatusSetRequest = new PddGoodsSaleStatusSetRequest();
        pddGoodsSaleStatusSetRequest.setGoodsId(id);
        pddGoodsSaleStatusSetRequest.setIsOnsale(isOnsale);
        try {
            PddGoodsSaleStatusSetResponse pddGoodsSaleStatusSetResponse = popHttpClient.syncInvoke(pddGoodsSaleStatusSetRequest, accessToken);
            PopBaseHttpResponse.ErrorResponse errorResponse = pddGoodsSaleStatusSetResponse.getErrorResponse();
            PddGoodsSaleStatusSetResponse.GoodsSaleStatusSetResponse goodsSaleStatusSetResponse = pddGoodsSaleStatusSetResponse.getGoodsSaleStatusSetResponse();
            Boolean isSuccess = goodsSaleStatusSetResponse.getIsSuccess();
            if(!isSuccess){
                throw new MyException("更新失败，原因未知。请同步商品后重试！");
            }
        } catch (Exception e) {
            log.error(e.toString(),e);

        }
       return;


    }




}
