package com.wdb.pdd.api.service.batch.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pdd.pop.sdk.http.PopBaseHttpResponse;
import com.pdd.pop.sdk.http.PopHttpClient;
import com.pdd.pop.sdk.http.api.request.PddGoodsDetailGetRequest;
import com.pdd.pop.sdk.http.api.request.PddGoodsInformationUpdateRequest;
import com.pdd.pop.sdk.http.api.request.PddGoodsQuantityUpdateRequest;
import com.pdd.pop.sdk.http.api.request.PddGoodsSkuPriceUpdateRequest;
import com.pdd.pop.sdk.http.api.response.PddGoodsDetailGetResponse;
import com.pdd.pop.sdk.http.api.response.PddGoodsInformationUpdateResponse;
import com.pdd.pop.sdk.http.api.response.PddGoodsQuantityUpdateResponse;
import com.pdd.pop.sdk.http.api.response.PddGoodsSkuPriceUpdateResponse;
import com.wdb.pdd.api.pojo.dto.GoodsBatchAddDTO;
import com.wdb.pdd.api.pojo.entity.*;
import com.wdb.pdd.api.service.batch.IGoodsBatchBizService;
import com.wdb.pdd.api.service.batch.IGoodsBatchDetailService;
import com.wdb.pdd.api.service.batch.IGoodsBatchService;
import com.wdb.pdd.api.service.goods.IGoodsService;
import com.wdb.pdd.api.service.sys.IReguserService;
import com.wdb.pdd.common.exception.MyException;
import com.wdb.pdd.common.exception.handlers.EnumErrorCode;
import com.wdb.pdd.common.task.AsyncTask;
import com.wdb.pdd.common.utils.IdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/10 0010
 * @描述
 */
@Service
public class GoodsBatchBizServiceImpl implements IGoodsBatchBizService {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private IGoodsBatchService goodsBatchService;
    @Autowired
    private IGoodsBatchDetailService goodsBatchDetailService;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private PopHttpClient popHttpClient;
    @Autowired
    private IReguserService reguserService;

    /**
     * 读取数据库当前应当异步处理的ID
     * 预处理到detail表中
     *
     * @param batchId
     */
    @Override
    public void reader(String batchId) {
        GoodsBatchDO byId = goodsBatchService.getById(batchId);
        if (byId != null && byId.getBatchStatus() == 0) {
            //交给处理类
            resolve(byId);
        }
    }

    /**
     * 解析数据到detail
     */
    private void resolve(GoodsBatchDO goodsBatchDO) {
        //处理开始更改处理中状态
        goodsBatchDO.setBatchStatus(1);
        goodsBatchService.updateById(goodsBatchDO);
        Integer batchType = goodsBatchDO.getBatchType();
        Integer reguserId = goodsBatchDO.getReguserId();
        String batchDesc = goodsBatchDO.getBatchDesc();
        String batchDescStr = Base64Decoder.decodeStr(batchDesc);
        GoodsBatchAddDTO goodsBatchAddDTO = JSON.parseObject(batchDescStr, GoodsBatchAddDTO.class);
        List<GoodsDO> goodsDOList = new ArrayList<>();
        try {
            if (goodsBatchAddDTO.getSelectType() == 2) {
                //为所有页全选 取未选
                goodsDOList = goodsService.list(new LambdaQueryWrapper<GoodsDO>()
                        .eq(GoodsDO::getReguserId, reguserId)
                        .notIn(GoodsDO::getGoodsId, goodsBatchAddDTO.getUnSelect())
                        .eq(goodsBatchAddDTO.getCatId() != null, GoodsDO::getCatId, goodsBatchAddDTO.getCatId()));
            } else {
                //取已选
                if(goodsBatchAddDTO.getSelect() != null || goodsBatchAddDTO.getSelect().size() > 0){
                    goodsDOList = goodsService.list(new LambdaQueryWrapper<GoodsDO>()
                            .eq(GoodsDO::getReguserId, reguserId)
                            .in(GoodsDO::getGoodsId, goodsBatchAddDTO.getSelect()));
                }
            }
        } catch (Exception e) {
            log.error(e.toString(),e);
            goodsBatchDO.setBatchStatus(2);
            goodsBatchService.updateById(goodsBatchDO);
            return;
        }
        if (goodsDOList == null || goodsDOList.size() < 1) {
            //当前如不存在商品列表需要修改 则取消该任务
            goodsBatchDO.setBatchStatus(2);
            goodsBatchService.updateById(goodsBatchDO);
            return;
        }
        batch4Type(reguserId, goodsDOList, goodsBatchAddDTO.getBatchDetail(), batchType, goodsBatchDO.getId());
    }

    /**
     * 执行数据批量请求修改 入库
     *
     * @param batchId
     */
    @Override
    public void writer(String batchId) {
        //读取 并请求拼多多执行修改
        GoodsBatchDO goodsBatchDO = goodsBatchService.getById(batchId);
        try {
            List<GoodsBatchDetailDO> list = goodsBatchDetailService.list(new LambdaQueryWrapper<GoodsBatchDetailDO>()
                    .eq(GoodsBatchDetailDO::getBatchId, batchId));
            if (list != null && list.size() > 0) {
                Integer reguserId = goodsBatchDO.getReguserId();
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
                for (GoodsBatchDetailDO detailDO : list) {
                    //批处理时校验是否执行过
                    if(detailDO.getState() != 0){
                        //非 要处理的 则抛弃
                        continue;
                    }
                    //这里区别对待库存设置 库存设置在商品设置中只能增减少 不能设置阈值且有一个不正确则影响该商品下所有的sku
                    if(detailDO.getBatchType() == 3){
                        //这里调用单独调用修改sku库存接口
                        log.info("执行修改库存");
                        //改库存 TODO 这里批量改sku库存单独调用库存接口
                        //多个sku分别调用接口 errMsg存储HashMap数据每次错误put刷新 skuId2Msg
                        batchSkuQuantityUpdate(detailDO,accessToken);
                    } else if(detailDO.getBatchType() == 12 || detailDO.getBatchType() == 13){
                        log.info("执行单独的单买价团购价");
                        //TODO 这里批量改sku价格单独调用接口
                        batchSkuPriceUpdate(detailDO,accessToken);
                    } else {
                        //这里调用通用方法
                        coreExec(detailDO, accessToken);
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        //处理完成后更新状态
        goodsBatchDO.setBatchStatus(2);
        goodsBatchService.updateById(goodsBatchDO);
    }

    /**
     * ========================处理数据具体方法========================
     */
    /**
     * 根据type具体修改
     *
     * @param reguserId
     * @param goodsDOList
     * @param batchDetail
     */
    private void batch4Type(Integer reguserId, List<GoodsDO> goodsDOList, String batchDetail, Integer batchType, String batchId) {
        JSONObject jsonObject = null;
        if (!StrUtil.isEmpty(batchDetail)) {
            String s = Base64Decoder.decodeStr(batchDetail);
            jsonObject = JSON.parseObject(s);
        }
        if (jsonObject == null) {
            return;
        }
        List<GoodsBatchDetailDO> detailDOs = new ArrayList<>();
        if (batchType == 1) {
            //标题修改
            String batchChannel = jsonObject.getString("batchChannel");
            String v1 = jsonObject.getString("v1");
            String v2 = jsonObject.getString("v2");
            if (StrUtil.equals("add", batchChannel)) {
                for (GoodsDO goodsDO : goodsDOList) {
                    String goodsName = goodsDO.getGoodsName();
                    String newGoodsName = goodsName + "";
                    if (!StrUtil.isEmpty(v1)) {
                        newGoodsName = v1 + newGoodsName;
                    }
                    if (!StrUtil.isEmpty(v2)) {
                        newGoodsName = newGoodsName + v2;
                    }
                    GoodsBatchDetailDO goodsBatchDetailDO = new GoodsBatchDetailDO();
                    goodsBatchDetailDO.setAddTime(new Date());
                    goodsBatchDetailDO.setBatchId(batchId);
                    goodsBatchDetailDO.setId(IdUtil.getStrId());
                    goodsBatchDetailDO.setOldValue(goodsName);
                    goodsBatchDetailDO.setNewValue(newGoodsName);
                    goodsBatchDetailDO.setReguserId(reguserId);
                    goodsBatchDetailDO.setState(0);
                    goodsBatchDetailDO.setBatchType(batchType);
                    goodsBatchDetailDO.setBatchChannel(batchChannel);
                    goodsBatchDetailDO.setGoodsId(goodsDO.getGoodsId());
                    detailDOs.add(goodsBatchDetailDO);
                }
            } else if (StrUtil.equals("update", batchChannel)) {
                for (GoodsDO goodsDO : goodsDOList) {
                    String goodsName = goodsDO.getGoodsName();
                    String newGoodsName = goodsName + "";
                    if (StrUtil.isEmpty(v1)) {
                        v1 = "";
                    }
                    if (StrUtil.isEmpty(v2)) {
                        v2 = "";
                    }
                    newGoodsName = StrUtil.replace(newGoodsName, v1, v2);
                    GoodsBatchDetailDO goodsBatchDetailDO = new GoodsBatchDetailDO();
                    goodsBatchDetailDO.setAddTime(new Date());
                    goodsBatchDetailDO.setBatchId(batchId);
                    goodsBatchDetailDO.setId(IdUtil.getStrId());
                    goodsBatchDetailDO.setOldValue(goodsName);
                    goodsBatchDetailDO.setNewValue(newGoodsName);
                    goodsBatchDetailDO.setReguserId(reguserId);
                    goodsBatchDetailDO.setState(0);
                    goodsBatchDetailDO.setBatchType(batchType);
                    goodsBatchDetailDO.setBatchChannel(batchChannel);
                    goodsBatchDetailDO.setGoodsId(goodsDO.getGoodsId());
                    detailDOs.add(goodsBatchDetailDO);
                }
            } else if (StrUtil.equals("del", batchChannel)) {
                for (GoodsDO goodsDO : goodsDOList) {
                    String goodsName = goodsDO.getGoodsName();
                    String newGoodsName = goodsName + "";
                    if (StrUtil.isEmpty(v1)) {
                        v1 = "";
                    }
                    v2 = "";
                    newGoodsName = StrUtil.replace(newGoodsName, v1, v2);
                    GoodsBatchDetailDO goodsBatchDetailDO = new GoodsBatchDetailDO();
                    goodsBatchDetailDO.setAddTime(new Date());
                    goodsBatchDetailDO.setBatchId(batchId);
                    goodsBatchDetailDO.setId(IdUtil.getStrId());
                    goodsBatchDetailDO.setOldValue(goodsName);
                    goodsBatchDetailDO.setNewValue(newGoodsName);
                    goodsBatchDetailDO.setReguserId(reguserId);
                    goodsBatchDetailDO.setState(0);
                    goodsBatchDetailDO.setBatchType(batchType);
                    goodsBatchDetailDO.setBatchChannel(batchChannel);
                    goodsBatchDetailDO.setGoodsId(goodsDO.getGoodsId());
                    detailDOs.add(goodsBatchDetailDO);
                }
            }
        } else if (batchType == 2) {
            //价格修改 市场价
            String batchChannel = jsonObject.getString("batchChannel");
            if (StrUtil.equals("update", batchChannel)) {
                for (GoodsDO goodsDO : goodsDOList) {
                    Long marketPrice = goodsDO.getMarketPrice();
                    BigDecimal v1 = new BigDecimal(jsonObject.getString("v1"));
                    Long newValue = NumberUtil.mul(v1, 100).longValue();
                    GoodsBatchDetailDO goodsBatchDetailDO = new GoodsBatchDetailDO();
                    goodsBatchDetailDO.setAddTime(new Date());
                    goodsBatchDetailDO.setBatchId(batchId);
                    goodsBatchDetailDO.setId(IdUtil.getStrId());
                    goodsBatchDetailDO.setOldValue(String.valueOf(marketPrice));
                    goodsBatchDetailDO.setNewValue(String.valueOf(newValue));
                    goodsBatchDetailDO.setReguserId(reguserId);
                    goodsBatchDetailDO.setState(0);
                    goodsBatchDetailDO.setBatchType(batchType);
                    goodsBatchDetailDO.setBatchChannel(batchChannel);
                    goodsBatchDetailDO.setGoodsId(goodsDO.getGoodsId());
                    detailDOs.add(goodsBatchDetailDO);
                }
            } else if (StrUtil.equals("compute", batchChannel)) {
                for (GoodsDO goodsDO : goodsDOList) {
                    String v1 = jsonObject.getString("v1") == null ? "100" : jsonObject.getString("v1");
                    String v2 = jsonObject.getString("v2") == null ? "0" : jsonObject.getString("v2");
                    String v3 = jsonObject.getString("v3") == null ? "0" : jsonObject.getString("v3");
                    Integer v4 = jsonObject.getInteger("v4") == null ? 0 : jsonObject.getInteger("v4");
                    Long marketPrice = goodsDO.getMarketPrice();
                    BigDecimal div = NumberUtil.div(String.valueOf(marketPrice), "100");
                    BigDecimal v1n = NumberUtil.div(v1, String.valueOf(100));
                    BigDecimal mul1 = NumberUtil.mul(div, v1n);
                    BigDecimal add1 = NumberUtil.add(mul1, new BigDecimal(v2));
                    BigDecimal sub = NumberUtil.sub(add1, new BigDecimal(v3));
                    BigDecimal mul = NumberUtil.mul(sub, 100);
                    Long newValue = mul.longValue();
                    if (0 == v4) {
                        //保留
                    } else if (1 == v4) {
                        //去位
                        newValue = (long) NumberUtil.mul(NumberUtil.div(String.valueOf(newValue), "100").longValue(), 100);
                    } else if (2 == v4) {
                        //进位
                        newValue = NumberUtil.mul(NumberUtil.round(NumberUtil.div(String.valueOf(newValue), "100"), 2), 100).longValue();
                    }
                    GoodsBatchDetailDO goodsBatchDetailDO = new GoodsBatchDetailDO();
                    goodsBatchDetailDO.setAddTime(new Date());
                    goodsBatchDetailDO.setBatchId(batchId);
                    goodsBatchDetailDO.setId(IdUtil.getStrId());
                    goodsBatchDetailDO.setOldValue(String.valueOf(marketPrice));
                    goodsBatchDetailDO.setNewValue(String.valueOf(newValue));
                    goodsBatchDetailDO.setReguserId(reguserId);
                    goodsBatchDetailDO.setState(0);
                    goodsBatchDetailDO.setBatchType(batchType);
                    goodsBatchDetailDO.setBatchChannel(batchChannel);
                    goodsBatchDetailDO.setGoodsId(goodsDO.getGoodsId());
                    detailDOs.add(goodsBatchDetailDO);
                }
            }
        } else if (batchType == 12) {
            //价格修改 单买价
            String batchChannel = jsonObject.getString("batchChannel");
            if (StrUtil.equals("update", batchChannel)) {
                for (GoodsDO goodsDO : goodsDOList) {
                    String skuList = goodsDO.getSkuList();
                    BigDecimal v1 = new BigDecimal(jsonObject.getString("v1"));
                    GoodsBatchDetailDO goodsBatchDetailDO = new GoodsBatchDetailDO();
                    Long newValue = NumberUtil.mul(v1, 100).longValue();
                    if(!StrUtil.isEmpty(skuList)){
                        List<GoodsSkuDO> goodsSkuDOS = JSONArray.parseArray(skuList, GoodsSkuDO.class);
                        ArrayList<GoodsSkuDO> newGoodsSkuDOs = new ArrayList<>();
                        for(GoodsSkuDO skuDO : goodsSkuDOS){
                            skuDO.setPrice(newValue);
                            newGoodsSkuDOs.add(skuDO);
                        }
                        goodsBatchDetailDO.setNewValue(JSON.toJSONString(newGoodsSkuDOs));
                    }
                    goodsBatchDetailDO.setAddTime(new Date());
                    goodsBatchDetailDO.setBatchId(batchId);
                    goodsBatchDetailDO.setId(IdUtil.getStrId());
                    goodsBatchDetailDO.setOldValue(skuList);
                    goodsBatchDetailDO.setReguserId(reguserId);
                    goodsBatchDetailDO.setState(0);
                    goodsBatchDetailDO.setBatchType(batchType);
                    goodsBatchDetailDO.setBatchChannel(batchChannel);
                    goodsBatchDetailDO.setGoodsId(goodsDO.getGoodsId());
                    detailDOs.add(goodsBatchDetailDO);
                }
            } else if (StrUtil.equals("compute", batchChannel)) {
                for (GoodsDO goodsDO : goodsDOList) {
                    String v1 = jsonObject.getString("v1") == null ? "100" : jsonObject.getString("v1");
                    String v2 = jsonObject.getString("v2") == null ? "0" : jsonObject.getString("v2");
                    String v3 = jsonObject.getString("v3") == null ? "0" : jsonObject.getString("v3");
                    Integer v4 = jsonObject.getInteger("v4") == null ? 0 : jsonObject.getInteger("v4");
                    GoodsBatchDetailDO goodsBatchDetailDO = new GoodsBatchDetailDO();
                    String skuList = goodsDO.getSkuList();
                    if(!StrUtil.isEmpty(skuList)){
                        List<GoodsSkuDO> goodsSkuDOS = JSONArray.parseArray(skuList, GoodsSkuDO.class);
                        ArrayList<GoodsSkuDO> newGoodsSkuDOs = new ArrayList<>();
                        for(GoodsSkuDO skuDO : goodsSkuDOS){
                            Long marketPrice = skuDO.getPrice();
                            BigDecimal div = NumberUtil.div(String.valueOf(marketPrice), "100");
                            BigDecimal v1n = NumberUtil.div(v1, String.valueOf(100));
                            BigDecimal mul1 = NumberUtil.mul(div, v1n);
                            BigDecimal add1 = NumberUtil.add(mul1, new BigDecimal(v2));
                            BigDecimal sub = NumberUtil.sub(add1, new BigDecimal(v3));
                            BigDecimal mul = NumberUtil.mul(sub, 100);
                            Long newValue = mul.longValue();
                            if (0 == v4) {
                                //保留
                            } else if (1 == v4) {
                                //去位
                                newValue = (long) NumberUtil.mul(NumberUtil.div(String.valueOf(newValue), "100").longValue(), 100);
                            } else if (2 == v4) {
                                //进位
                                newValue = NumberUtil.mul(NumberUtil.round(NumberUtil.div(String.valueOf(newValue), "100"), 2), 100).longValue();
                            }
                            skuDO.setPrice(newValue);
                            newGoodsSkuDOs.add(skuDO);
                        }
                        goodsBatchDetailDO.setNewValue(JSON.toJSONString(newGoodsSkuDOs));
                    }
                    goodsBatchDetailDO.setAddTime(new Date());
                    goodsBatchDetailDO.setBatchId(batchId);
                    goodsBatchDetailDO.setId(IdUtil.getStrId());
                    goodsBatchDetailDO.setOldValue(skuList);
                    goodsBatchDetailDO.setReguserId(reguserId);
                    goodsBatchDetailDO.setState(0);
                    goodsBatchDetailDO.setBatchType(batchType);
                    goodsBatchDetailDO.setBatchChannel(batchChannel);
                    goodsBatchDetailDO.setGoodsId(goodsDO.getGoodsId());
                    detailDOs.add(goodsBatchDetailDO);
                }
            }
        } else if (batchType == 13) {
            //价格修改 团购价
            String batchChannel = jsonObject.getString("batchChannel");
            if (StrUtil.equals("update", batchChannel)) {
                for (GoodsDO goodsDO : goodsDOList) {
                    String skuList = goodsDO.getSkuList();
                    BigDecimal v1 = new BigDecimal(jsonObject.getString("v1"));
                    GoodsBatchDetailDO goodsBatchDetailDO = new GoodsBatchDetailDO();
                    Long newValue = NumberUtil.mul(v1, 100).longValue();
                    if(!StrUtil.isEmpty(skuList)){
                        List<GoodsSkuDO> goodsSkuDOS = JSONArray.parseArray(skuList, GoodsSkuDO.class);
                        ArrayList<GoodsSkuDO> newGoodsSkuDOs = new ArrayList<>();
                        for(GoodsSkuDO skuDO : goodsSkuDOS){
                            skuDO.setMultiPrice(newValue);
                            newGoodsSkuDOs.add(skuDO);
                        }
                        goodsBatchDetailDO.setNewValue(JSON.toJSONString(newGoodsSkuDOs));
                    }
                    goodsBatchDetailDO.setAddTime(new Date());
                    goodsBatchDetailDO.setBatchId(batchId);
                    goodsBatchDetailDO.setId(IdUtil.getStrId());
                    goodsBatchDetailDO.setOldValue(skuList);
                    goodsBatchDetailDO.setReguserId(reguserId);
                    goodsBatchDetailDO.setState(0);
                    goodsBatchDetailDO.setBatchType(batchType);
                    goodsBatchDetailDO.setBatchChannel(batchChannel);
                    goodsBatchDetailDO.setGoodsId(goodsDO.getGoodsId());
                    detailDOs.add(goodsBatchDetailDO);
                }
            } else if (StrUtil.equals("compute", batchChannel)) {
                for (GoodsDO goodsDO : goodsDOList) {
                    String v1 = jsonObject.getString("v1") == null ? "100" : jsonObject.getString("v1");
                    String v2 = jsonObject.getString("v2") == null ? "0" : jsonObject.getString("v2");
                    String v3 = jsonObject.getString("v3") == null ? "0" : jsonObject.getString("v3");
                    Integer v4 = jsonObject.getInteger("v4") == null ? 0 : jsonObject.getInteger("v4");
                    GoodsBatchDetailDO goodsBatchDetailDO = new GoodsBatchDetailDO();
                    String skuList = goodsDO.getSkuList();
                    if(!StrUtil.isEmpty(skuList)){
                        List<GoodsSkuDO> goodsSkuDOS = JSONArray.parseArray(skuList, GoodsSkuDO.class);
                        ArrayList<GoodsSkuDO> newGoodsSkuDOs = new ArrayList<>();
                        for(GoodsSkuDO skuDO : goodsSkuDOS){
                            Long marketPrice = skuDO.getMultiPrice();
                            BigDecimal div = NumberUtil.div(String.valueOf(marketPrice), "100");
                            BigDecimal v1n = NumberUtil.div(v1, String.valueOf(100));
                            BigDecimal mul1 = NumberUtil.mul(div, v1n);
                            BigDecimal add1 = NumberUtil.add(mul1, new BigDecimal(v2));
                            BigDecimal sub = NumberUtil.sub(add1, new BigDecimal(v3));
                            BigDecimal mul = NumberUtil.mul(sub, 100);
                            Long newValue = mul.longValue();
                            if (0 == v4) {
                                //保留
                            } else if (1 == v4) {
                                //去位
                                newValue = (long) NumberUtil.mul(NumberUtil.div(String.valueOf(newValue), "100").longValue(), 100);
                            } else if (2 == v4) {
                                //进位
                                newValue = NumberUtil.mul(NumberUtil.round(NumberUtil.div(String.valueOf(newValue), "100"), 2), 100).longValue();
                            }
                            skuDO.setMultiPrice(newValue);
                            newGoodsSkuDOs.add(skuDO);
                        }
                        goodsBatchDetailDO.setNewValue(JSON.toJSONString(newGoodsSkuDOs));
                    }
                    goodsBatchDetailDO.setAddTime(new Date());
                    goodsBatchDetailDO.setBatchId(batchId);
                    goodsBatchDetailDO.setId(IdUtil.getStrId());
                    goodsBatchDetailDO.setOldValue(skuList);
                    goodsBatchDetailDO.setReguserId(reguserId);
                    goodsBatchDetailDO.setState(0);
                    goodsBatchDetailDO.setBatchType(batchType);
                    goodsBatchDetailDO.setBatchChannel(batchChannel);
                    goodsBatchDetailDO.setGoodsId(goodsDO.getGoodsId());
                    detailDOs.add(goodsBatchDetailDO);
                }
            }
        } else if (batchType == 3) {
            //库存修改
            String batchChannel = jsonObject.getString("batchChannel");
            Long v1 = jsonObject.getLong("v1");
            if (StrUtil.equals("update", batchChannel)) {
                for (GoodsDO goodsDO : goodsDOList) {
                    //skuId:quantity
                    HashMap<Long, Long> oldValue = new HashMap<>();
                    HashMap<Long, Long> newValue = new HashMap<>();
                    String skuList = goodsDO.getSkuList();
                    if (!StrUtil.isEmpty(skuList)) {
                        List<GoodsSkuDO> goodsSkuDOs = JSON.parseArray(skuList, GoodsSkuDO.class);
                        if (goodsSkuDOs != null && goodsSkuDOs.size() > 0) {
                            for (GoodsSkuDO skuDO : goodsSkuDOs) {
                                Long quantity = skuDO.getQuantity();
                                oldValue.put(skuDO.getSkuId(), quantity);
                                newValue.put(skuDO.getSkuId(), v1);
                            }
                        }
                    }
                    GoodsBatchDetailDO goodsBatchDetailDO = new GoodsBatchDetailDO();
                    goodsBatchDetailDO.setAddTime(new Date());
                    goodsBatchDetailDO.setBatchId(batchId);
                    goodsBatchDetailDO.setId(IdUtil.getStrId());
                    goodsBatchDetailDO.setOldValue(JSON.toJSONString(oldValue));
                    goodsBatchDetailDO.setNewValue(JSON.toJSONString(newValue));
                    goodsBatchDetailDO.setReguserId(reguserId);
                    goodsBatchDetailDO.setState(0);
                    goodsBatchDetailDO.setBatchType(batchType);
                    goodsBatchDetailDO.setBatchChannel(batchChannel);
                    goodsBatchDetailDO.setGoodsId(goodsDO.getGoodsId());
                    detailDOs.add(goodsBatchDetailDO);
                }
            } else if (StrUtil.equals("add", batchChannel)) {
                for (GoodsDO goodsDO : goodsDOList) {
                    //skuId:quantity
                    HashMap<Long, Long> oldValue = new HashMap<>();
                    HashMap<Long, Long> newValue = new HashMap<>();
                    String skuList = goodsDO.getSkuList();
                    if (!StrUtil.isEmpty(skuList)) {
                        List<GoodsSkuDO> goodsSkuDOs = JSON.parseArray(skuList, GoodsSkuDO.class);
                        if (goodsSkuDOs != null && goodsSkuDOs.size() > 0) {
                            for (GoodsSkuDO skuDO : goodsSkuDOs) {
                                Long quantity = skuDO.getQuantity();
                                oldValue.put(skuDO.getSkuId(), quantity);
                                newValue.put(skuDO.getSkuId(), quantity + v1);
                            }
                        }
                    }
                    GoodsBatchDetailDO goodsBatchDetailDO = new GoodsBatchDetailDO();
                    goodsBatchDetailDO.setAddTime(new Date());
                    goodsBatchDetailDO.setBatchId(batchId);
                    goodsBatchDetailDO.setId(IdUtil.getStrId());
                    goodsBatchDetailDO.setOldValue(JSON.toJSONString(oldValue));
                    goodsBatchDetailDO.setNewValue(JSON.toJSONString(newValue));
                    goodsBatchDetailDO.setReguserId(reguserId);
                    goodsBatchDetailDO.setState(0);
                    goodsBatchDetailDO.setBatchType(batchType);
                    goodsBatchDetailDO.setBatchChannel(batchChannel);
                    goodsBatchDetailDO.setGoodsId(goodsDO.getGoodsId());
                    detailDOs.add(goodsBatchDetailDO);
                }
            } else if (StrUtil.equals("del", batchChannel)) {
                Long v2 = jsonObject.getLong("v2") == null ? 0L : jsonObject.getLong("v2");
                for (GoodsDO goodsDO : goodsDOList) {
                    //skuId:quantity
                    HashMap<Long, Long> oldValue = new HashMap<>();
                    HashMap<Long, Long> newValue = new HashMap<>();
                    String skuList = goodsDO.getSkuList();
                    if (!StrUtil.isEmpty(skuList)) {
                        List<GoodsSkuDO> goodsSkuDOs = JSON.parseArray(skuList, GoodsSkuDO.class);
                        if (goodsSkuDOs != null && goodsSkuDOs.size() > 0) {
                            for (GoodsSkuDO skuDO : goodsSkuDOs) {
                                Long quantity = skuDO.getQuantity();
                                oldValue.put(skuDO.getSkuId(), quantity);
                                long l = quantity - v1;
                                if (l < v2) {
                                    l = v2;
                                }
                                newValue.put(skuDO.getSkuId(), l);
                            }
                        }
                    }
                    GoodsBatchDetailDO goodsBatchDetailDO = new GoodsBatchDetailDO();
                    goodsBatchDetailDO.setAddTime(new Date());
                    goodsBatchDetailDO.setBatchId(batchId);
                    goodsBatchDetailDO.setId(IdUtil.getStrId());
                    goodsBatchDetailDO.setOldValue(JSON.toJSONString(oldValue));
                    goodsBatchDetailDO.setNewValue(JSON.toJSONString(newValue));
                    goodsBatchDetailDO.setReguserId(reguserId);
                    goodsBatchDetailDO.setState(0);
                    goodsBatchDetailDO.setBatchType(batchType);
                    goodsBatchDetailDO.setBatchChannel(batchChannel);
                    goodsBatchDetailDO.setGoodsId(goodsDO.getGoodsId());
                    detailDOs.add(goodsBatchDetailDO);
                }
            }
        } else if (batchType == 4) {
            //分类修改
            String batchChannel = jsonObject.getString("batchChannel");
            Long v1 = jsonObject.getLong("v1");
            if (StrUtil.equals("update", batchChannel)) {
                for (GoodsDO goodsDO : goodsDOList) {
                    GoodsBatchDetailDO goodsBatchDetailDO = new GoodsBatchDetailDO();
                    goodsBatchDetailDO.setAddTime(new Date());
                    goodsBatchDetailDO.setBatchId(batchId);
                    goodsBatchDetailDO.setId(IdUtil.getStrId());
                    goodsBatchDetailDO.setOldValue(String.valueOf(goodsDO.getCatId()));
                    goodsBatchDetailDO.setNewValue(String.valueOf(v1));
                    goodsBatchDetailDO.setReguserId(reguserId);
                    goodsBatchDetailDO.setState(0);
                    goodsBatchDetailDO.setBatchType(batchType);
                    goodsBatchDetailDO.setBatchChannel(batchChannel);
                    goodsBatchDetailDO.setGoodsId(goodsDO.getGoodsId());
                    detailDOs.add(goodsBatchDetailDO);
                }
            }
        } else if (batchType == 5) {
            //预售修改
            Integer v1 = jsonObject.getInteger("v1");
            Integer isPreSale = v1;
            if (v1 == 0) {
                //普通商品 发货时间 48/24
                Long v2 = jsonObject.getLong("v2");
                for (GoodsDO goodsDO : goodsDOList) {
                    HashMap<String, Long> oldVal = new HashMap<>();
                    HashMap<String, Long> newVal = new HashMap<>();
                    oldVal.put("isPreSale", goodsDO.getIsPreSale().longValue());
                    oldVal.put("preSaleTime", goodsDO.getPreSaleTime());
                    oldVal.put("shipmentLimitSecond", goodsDO.getShipmentLimitSecond());
                    newVal.put("isPreSale", isPreSale.longValue());
                    newVal.put("preSaleTime", 0L);
                    newVal.put("shipmentLimitSecond", v2);
                    GoodsBatchDetailDO goodsBatchDetailDO = new GoodsBatchDetailDO();
                    goodsBatchDetailDO.setAddTime(new Date());
                    goodsBatchDetailDO.setBatchId(batchId);
                    goodsBatchDetailDO.setId(IdUtil.getStrId());
                    goodsBatchDetailDO.setOldValue(JSON.toJSONString(oldVal));
                    goodsBatchDetailDO.setNewValue(JSON.toJSONString(newVal));
                    goodsBatchDetailDO.setReguserId(reguserId);
                    goodsBatchDetailDO.setState(0);
                    goodsBatchDetailDO.setBatchType(batchType);
                    goodsBatchDetailDO.setBatchChannel(String.valueOf(v1));
                    goodsBatchDetailDO.setGoodsId(goodsDO.getGoodsId());
                    detailDOs.add(goodsBatchDetailDO);
                }
            } else if (v1 == 1) {
                //预售商品 预发售时间 2019-06-06
                String date = jsonObject.getString("v2");
                Date parse = DateUtil.parse(date, "yyyy-MM-dd");
                Long v2 = DateUtil.endOfDay(parse).getTime() / 1000;
                for (GoodsDO goodsDO : goodsDOList) {
                    HashMap<String, Long> oldVal = new HashMap<>();
                    HashMap<String, Long> newVal = new HashMap<>();
                    oldVal.put("isPreSale", goodsDO.getIsPreSale().longValue());
                    oldVal.put("preSaleTime", goodsDO.getPreSaleTime());
                    oldVal.put("shipmentLimitSecond", goodsDO.getShipmentLimitSecond());
                    newVal.put("isPreSale", isPreSale.longValue());
                    newVal.put("preSaleTime", v2);
                    newVal.put("shipmentLimitSecond", 0L);
                    GoodsBatchDetailDO goodsBatchDetailDO = new GoodsBatchDetailDO();
                    goodsBatchDetailDO.setAddTime(new Date());
                    goodsBatchDetailDO.setBatchId(batchId);
                    goodsBatchDetailDO.setId(IdUtil.getStrId());
                    goodsBatchDetailDO.setOldValue(JSON.toJSONString(oldVal));
                    goodsBatchDetailDO.setNewValue(JSON.toJSONString(newVal));
                    goodsBatchDetailDO.setReguserId(reguserId);
                    goodsBatchDetailDO.setState(0);
                    goodsBatchDetailDO.setBatchType(batchType);
                    goodsBatchDetailDO.setBatchChannel(String.valueOf(v1));
                    goodsBatchDetailDO.setGoodsId(goodsDO.getGoodsId());
                    detailDOs.add(goodsBatchDetailDO);
                }
            }
        } else if (batchType == 6) {
            //描述修改
            String batchChannel = jsonObject.getString("batchChannel");
            String v1 = jsonObject.getString("v1");
            String v2 = jsonObject.getString("v2");
            if (StrUtil.equals("add", batchChannel)) {
                for (GoodsDO goodsDO : goodsDOList) {
                    String goodsDesc = goodsDO.getGoodsDesc();
                    String newGoodsDesc = goodsDesc + "";
                    if (!StrUtil.isEmpty(v1)) {
                        newGoodsDesc = v1 + newGoodsDesc;
                    }
                    if (!StrUtil.isEmpty(v2)) {
                        newGoodsDesc = newGoodsDesc + v2;
                    }
                    GoodsBatchDetailDO goodsBatchDetailDO = new GoodsBatchDetailDO();
                    goodsBatchDetailDO.setAddTime(new Date());
                    goodsBatchDetailDO.setBatchId(batchId);
                    goodsBatchDetailDO.setId(IdUtil.getStrId());
                    goodsBatchDetailDO.setOldValue(goodsDesc);
                    goodsBatchDetailDO.setNewValue(newGoodsDesc);
                    goodsBatchDetailDO.setReguserId(reguserId);
                    goodsBatchDetailDO.setState(0);
                    goodsBatchDetailDO.setBatchType(batchType);
                    goodsBatchDetailDO.setBatchChannel(batchChannel);
                    goodsBatchDetailDO.setGoodsId(goodsDO.getGoodsId());
                    detailDOs.add(goodsBatchDetailDO);
                }
            } else if (StrUtil.equals("update", batchChannel)) {
                for (GoodsDO goodsDO : goodsDOList) {
                    String goodsDesc = goodsDO.getGoodsDesc();
                    String newGoodsDesc = v1;
                    GoodsBatchDetailDO goodsBatchDetailDO = new GoodsBatchDetailDO();
                    goodsBatchDetailDO.setAddTime(new Date());
                    goodsBatchDetailDO.setBatchId(batchId);
                    goodsBatchDetailDO.setId(IdUtil.getStrId());
                    goodsBatchDetailDO.setOldValue(goodsDesc);
                    goodsBatchDetailDO.setNewValue(newGoodsDesc);
                    goodsBatchDetailDO.setReguserId(reguserId);
                    goodsBatchDetailDO.setState(0);
                    goodsBatchDetailDO.setBatchType(batchType);
                    goodsBatchDetailDO.setBatchChannel(batchChannel);
                    goodsBatchDetailDO.setGoodsId(goodsDO.getGoodsId());
                    detailDOs.add(goodsBatchDetailDO);
                }
            } else if (StrUtil.equals("del", batchChannel)) {
                for (GoodsDO goodsDO : goodsDOList) {
                    String goodsDesc = goodsDO.getGoodsDesc();
                    String newGoodsDesc = goodsDesc + "";
                    if (StrUtil.isEmpty(v1)) {
                        v1 = "";
                    }
                    v2 = "";
                    newGoodsDesc = StrUtil.replace(newGoodsDesc, v1, v2);
                    GoodsBatchDetailDO goodsBatchDetailDO = new GoodsBatchDetailDO();
                    goodsBatchDetailDO.setAddTime(new Date());
                    goodsBatchDetailDO.setBatchId(batchId);
                    goodsBatchDetailDO.setId(IdUtil.getStrId());
                    goodsBatchDetailDO.setOldValue(goodsDesc);
                    goodsBatchDetailDO.setNewValue(newGoodsDesc);
                    goodsBatchDetailDO.setReguserId(reguserId);
                    goodsBatchDetailDO.setState(0);
                    goodsBatchDetailDO.setBatchType(batchType);
                    goodsBatchDetailDO.setBatchChannel(batchChannel);
                    goodsBatchDetailDO.setGoodsId(goodsDO.getGoodsId());
                    detailDOs.add(goodsBatchDetailDO);
                }
            }
        } else if (batchType == 7) {
            //物流重量修改 TODO 拼多多API不存在

        } else if (batchType == 8) {
            //运费模版
            String batchChannel = jsonObject.getString("batchChannel");
            Long v1 = jsonObject.getLong("v1");
            if (StrUtil.equals("update", batchChannel)) {
                for (GoodsDO goodsDO : goodsDOList) {
                    GoodsBatchDetailDO goodsBatchDetailDO = new GoodsBatchDetailDO();
                    goodsBatchDetailDO.setAddTime(new Date());
                    goodsBatchDetailDO.setBatchId(batchId);
                    goodsBatchDetailDO.setId(IdUtil.getStrId());
                    goodsBatchDetailDO.setOldValue(String.valueOf(goodsDO.getCostTemplateId()));
                    goodsBatchDetailDO.setNewValue(String.valueOf(v1));
                    goodsBatchDetailDO.setReguserId(reguserId);
                    goodsBatchDetailDO.setState(0);
                    goodsBatchDetailDO.setBatchType(batchType);
                    goodsBatchDetailDO.setBatchChannel(batchChannel);
                    goodsBatchDetailDO.setGoodsId(goodsDO.getGoodsId());
                    detailDOs.add(goodsBatchDetailDO);
                }
            }
        } else if (batchType == 9) {
            //食品属性 TODO 拼多多API未找到

        } else if (batchType == 10) {
            //团购设置
            //单次限量
            Long v1 = jsonObject.getLong("v1");
            //限购次数
            Long v2 = jsonObject.getLong("v2");
            for (GoodsDO goodsDO : goodsDOList) {
                HashMap<String, Long> oldVal = new HashMap<>();
                HashMap<String, Long> newVal = new HashMap<>();
                oldVal.put("buyLimit", goodsDO.getBuyLimit());
                oldVal.put("orderLimit", goodsDO.getOrderLimit());
                newVal.put("buyLimit", v2);
                newVal.put("orderLimit", v1);
                GoodsBatchDetailDO goodsBatchDetailDO = new GoodsBatchDetailDO();
                goodsBatchDetailDO.setAddTime(new Date());
                goodsBatchDetailDO.setBatchId(batchId);
                goodsBatchDetailDO.setId(IdUtil.getStrId());
                goodsBatchDetailDO.setOldValue(JSON.toJSONString(oldVal));
                goodsBatchDetailDO.setNewValue(JSON.toJSONString(newVal));
                goodsBatchDetailDO.setReguserId(reguserId);
                goodsBatchDetailDO.setState(0);
                goodsBatchDetailDO.setBatchType(batchType);
                goodsBatchDetailDO.setGoodsId(goodsDO.getGoodsId());
                detailDOs.add(goodsBatchDetailDO);
            }
        } else if (batchType == 11) {
            //服务承诺
            //7天无理由退换货
            Integer v1 = jsonObject.getInteger("v1");
            //假一罚十
            Integer v2 = jsonObject.getInteger("v2");
            for (GoodsDO goodsDO : goodsDOList) {
                HashMap<String, Integer> oldVal = new HashMap<>();
                HashMap<String, Integer> newVal = new HashMap<>();
                oldVal.put("isFolt", goodsDO.getIsFolt());
                oldVal.put("isRefundable", goodsDO.getIsRefundable());
                newVal.put("isFolt", v2);
                newVal.put("isRefundable", v1);
                GoodsBatchDetailDO goodsBatchDetailDO = new GoodsBatchDetailDO();
                goodsBatchDetailDO.setAddTime(new Date());
                goodsBatchDetailDO.setBatchId(batchId);
                goodsBatchDetailDO.setId(IdUtil.getStrId());
                goodsBatchDetailDO.setOldValue(JSON.toJSONString(oldVal));
                goodsBatchDetailDO.setNewValue(JSON.toJSONString(newVal));
                goodsBatchDetailDO.setReguserId(reguserId);
                goodsBatchDetailDO.setState(0);
                goodsBatchDetailDO.setBatchType(batchType);
                goodsBatchDetailDO.setGoodsId(goodsDO.getGoodsId());
                detailDOs.add(goodsBatchDetailDO);
            }
        }
        if (detailDOs != null && detailDOs.size() > 0) {
            goodsBatchDetailService.saveBatch(detailDOs);
        }
    }

    /**
     * =================具体调用拼多多API方法=================
     */
    /**
     * 核心处理方法 调用商品编辑接口
     * 1通过商品id获取最新商品数据
     * 2更改部分参数请求
     * 3请求修改
     * 4更新表记录
     * 5更新商品表 //不一定用 多一次请求
     *
     * @param detailDO
     */
    private void coreExec(GoodsBatchDetailDO detailDO, String accessToken) {
        PddGoodsDetailGetRequest pddGoodsDetailGetRequest = new PddGoodsDetailGetRequest();
        PddGoodsInformationUpdateRequest pddGoodsInformationUpdateRequest = new PddGoodsInformationUpdateRequest();
        pddGoodsDetailGetRequest.setGoodsId(detailDO.getGoodsId());
        try {
            PddGoodsDetailGetResponse pddGoodsDetailGetResponse = popHttpClient.syncInvoke(pddGoodsDetailGetRequest, accessToken);
            PopBaseHttpResponse.ErrorResponse errorResponse = pddGoodsDetailGetResponse.getErrorResponse();
            if (errorResponse != null) {
                log.error(JSON.toJSONString(errorResponse));
                if (errorResponse.getErrorCode().equals(70031) || errorResponse.getErrorCode().equals(70032)) {
                    log.error("隔1s重试");
                    Thread.sleep(1000);
                    coreExec(detailDO, accessToken);
                    return;
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
            PddGoodsInformationUpdateRequest waitPddGoodsInformationUpdateRequest = getWaitPddGoodsInformationUpdateRequest(pddGoodsInformationUpdateRequest, detailDO, accessToken);
            PddGoodsInformationUpdateResponse pddGoodsInformationUpdateResponse = getPddGoodsInformationUpdateResponse(detailDO, accessToken, waitPddGoodsInformationUpdateRequest);
            if (pddGoodsInformationUpdateResponse == null) return;
            PddGoodsInformationUpdateResponse.GoodsUpdateResponse goodsUpdateResponse = pddGoodsInformationUpdateResponse.getGoodsUpdateResponse();
            Boolean isSuccess = goodsUpdateResponse.getIsSuccess();
            if (!isSuccess) {
                log.error("修改失败 序列号:{}", goodsUpdateResponse.getGoodsCommitId());
            }
            //1成功 2失败
            detailDO.setState(1);
            detailDO.setErrMsg(String.valueOf(goodsUpdateResponse.getGoodsCommitId()));
            detailDO.setUpdateTime(new Date());
            goodsBatchDetailService.updateById(detailDO);
        } catch (Exception e) {
            log.error(e.toString(), e);
            //1成功 2失败
            detailDO.setState(2);
            detailDO.setErrMsg(e.getMessage());
            detailDO.setUpdateTime(new Date());
            goodsBatchDetailService.updateById(detailDO);
            return;
        }
    }

    private PddGoodsInformationUpdateResponse getPddGoodsInformationUpdateResponse(GoodsBatchDetailDO detailDO, String accessToken, PddGoodsInformationUpdateRequest waitPddGoodsInformationUpdateRequest) throws Exception {
        PddGoodsInformationUpdateResponse pddGoodsInformationUpdateResponse = popHttpClient.syncInvoke(waitPddGoodsInformationUpdateRequest, accessToken);
        PopBaseHttpResponse.ErrorResponse errorResponse1 = pddGoodsInformationUpdateResponse.getErrorResponse();
        if (errorResponse1 != null) {
            log.error(JSON.toJSONString(errorResponse1));
            if (errorResponse1.getErrorCode().equals(70031) || errorResponse1.getErrorCode().equals(70032)) {
                log.error("隔1s重试");
                Thread.sleep(1000);
                return getPddGoodsInformationUpdateResponse(detailDO, accessToken,waitPddGoodsInformationUpdateRequest);
            }
            throw new MyException(errorResponse1.getErrorMsg());
        }
        return pddGoodsInformationUpdateResponse;
    }

    /**
     * 生成待修改详情的请求体
     *
     * @param pddGoodsInformationUpdateRequest
     * @param detailDO
     * @return
     */
    private PddGoodsInformationUpdateRequest getWaitPddGoodsInformationUpdateRequest(PddGoodsInformationUpdateRequest pddGoodsInformationUpdateRequest, GoodsBatchDetailDO detailDO,String accessToken) {
        Integer batchType = detailDO.getBatchType();
        if (batchType == 1) {
            log.info("执行修改标题");
            //改标题
            pddGoodsInformationUpdateRequest.setGoodsName(detailDO.getNewValue());
        } else if (batchType == 2) {
            log.info("执行修改市场价");
            //改市场价
            pddGoodsInformationUpdateRequest.setMarketPrice(Long.parseLong(detailDO.getNewValue()));
        } else if (batchType == 3) {
            //这里上层不在调用
            /*if (skuList != null && skuList.size() > 0) {
                String newValue = detailDO.getNewValue();
                HashMap<Long, Long> id2Quantity = JSON.parseObject(newValue, HashMap.class);
                List<PddGoodsInformationUpdateRequest.SkuListItem> skuListItems = new ArrayList<>();
                for (PddGoodsDetailGetResponse.GoodsDetailGetResponseSkuListItem item : skuList) {
                    PddGoodsInformationUpdateRequest.SkuListItem skuListItem = new PddGoodsInformationUpdateRequest.SkuListItem();
                    BeanUtil.copyProperties(item, skuListItem);
                    if (id2Quantity.containsKey(item.getSkuId())) {
                        skuListItem.setQuantity(id2Quantity.get(item.getSkuId()));
                    }
                    skuListItems.add(skuListItem);
                }
                pddGoodsInformationUpdateRequest.setSkuList(skuListItems);
            }*/
        } else if (batchType == 4) {
            log.info("执行修改分类");
            //修改分类
            pddGoodsInformationUpdateRequest.setCatId(Long.parseLong(detailDO.getNewValue()));
        } else if (batchType == 5) {
            log.info("执行修改预售参数");
            //预售修改
            String newValue = detailDO.getNewValue();
            HashMap<String, Integer> newVal = JSON.parseObject(newValue, HashMap.class);
            boolean isPreSale = newVal.get("isPreSale").equals(1) ? true : false;
            pddGoodsInformationUpdateRequest.setIsPreSale(isPreSale);
            if(isPreSale){
                log.info("预售"+newVal.get("preSaleTime"));
                pddGoodsInformationUpdateRequest.setPreSaleTime(Long.valueOf(newVal.get("preSaleTime")));
            }else {
                log.info("非预售");
                pddGoodsInformationUpdateRequest.setShipmentLimitSecond(Long.valueOf(newVal.get("shipmentLimitSecond")));
            }
        } else if (batchType == 6) {
            log.info("执行修改描述");
            pddGoodsInformationUpdateRequest.setGoodsDesc(detailDO.getNewValue());
        } else if (batchType == 7) {
            log.info("执行修改物流重量");
            //修改物流重量
        } else if (batchType == 8) {
            log.info("执行修改运费模版");
            //运费模板修改
            pddGoodsInformationUpdateRequest.setCostTemplateId(Long.parseLong(detailDO.getNewValue()));
        } else if (batchType == 9) {
            log.info("执行修改食品属性");
            //食品属性修改
        } else if (batchType == 10) {
            log.info("执行修改团购设置阈值");
            //团购设置
            String newValue = detailDO.getNewValue();
            HashMap<String, Long> newVal = JSON.parseObject(newValue, HashMap.class);
            if(newVal.get("buyLimit") != null){
                log.info("限购次数");
                pddGoodsInformationUpdateRequest.setBuyLimit(newVal.get("buyLimit"));
            }
            if(newVal.get("orderLimit") != null){
                log.info("单次限量");
                pddGoodsInformationUpdateRequest.setOrderLimit(newVal.get("orderLimit"));
            }
        } else if (batchType == 11) {
            log.info("执行修改服务承诺");
            String newValue = detailDO.getNewValue();
            HashMap<String, Integer> newVal = JSON.parseObject(newValue, HashMap.class);
            boolean isFolt = newVal.get("isFolt") == null || newVal.get("isFolt") == 0 ? false : true;
            boolean isRefundable = newVal.get("isRefundable") == null || newVal.get("isRefundable") == 0 ? false : true;
            pddGoodsInformationUpdateRequest.setIsFolt(isFolt);
            pddGoodsInformationUpdateRequest.setIsRefundable(isRefundable);
        }
        return pddGoodsInformationUpdateRequest;
    }
    /**
     * 批处理sku库存
     */
    private void batchSkuQuantityUpdate(GoodsBatchDetailDO detailDO,String accessToken){
        try {
            String newValue = detailDO.getNewValue();
            HashMap<Object,Object> hashMap = JSON.parseObject(newValue, new HashMap<Long,Long>().getClass());
            HashMap<Long, String> msg = new HashMap<>();
            if(hashMap != null && hashMap.size() > 0){
                //分别处理各个sku
                hashMap.forEach((k,v) -> {
                    quantityUpdate(accessToken,detailDO.getGoodsId(),Long.parseLong(String.valueOf(k)),Long.parseLong(String.valueOf(v)),detailDO.getId(),msg);
                });
            }
            if(msg == null || msg.size() == 0){
                //执行成功保存
                detailDO.setState(1);
            }else {
                //执行失败保存
                detailDO.setState(2);
                detailDO.setErrMsg(JSON.toJSONString(msg));
            }
            goodsBatchDetailService.updateById(detailDO);
        } catch (Exception e) {
            log.error(e.toString(),e);
        }
    }
    /**
     * 批处理sku价格
     */
    private void batchSkuPriceUpdate(GoodsBatchDetailDO detailDO,String accessToken){
        try {
            String newValue = detailDO.getNewValue();
            if(!StrUtil.isEmpty(newValue)){
                List<GoodsSkuDO> goodsSkuDOS = JSONArray.parseArray(newValue, GoodsSkuDO.class);
                PddGoodsSkuPriceUpdateRequest pddGoodsSkuPriceUpdateRequest = new PddGoodsSkuPriceUpdateRequest();
                pddGoodsSkuPriceUpdateRequest.setGoodsId(detailDO.getGoodsId());
                List<PddGoodsSkuPriceUpdateRequest.SkuPriceListItem> list = new ArrayList<>();
                for(GoodsSkuDO skuDO : goodsSkuDOS){
                    PddGoodsSkuPriceUpdateRequest.SkuPriceListItem skuPriceListItem = new PddGoodsSkuPriceUpdateRequest.SkuPriceListItem();
                    skuPriceListItem.setSkuId(skuDO.getSkuId());
                    skuPriceListItem.setSinglePrice(skuDO.getPrice());
                    skuPriceListItem.setGroupPrice(skuDO.getMultiPrice());
                    list.add(skuPriceListItem);
                }
                pddGoodsSkuPriceUpdateRequest.setSkuPriceList(list);
                PddGoodsSkuPriceUpdateResponse pddGoodsSkuPriceUpdateResponse = popHttpClient.syncInvoke(pddGoodsSkuPriceUpdateRequest, accessToken);
                if(pddGoodsSkuPriceUpdateResponse != null){
                    PopBaseHttpResponse.ErrorResponse errorResponse = pddGoodsSkuPriceUpdateResponse.getErrorResponse();
                    if(errorResponse != null){
                        log.error(JSON.toJSONString(errorResponse));
                        if(errorResponse.getErrorCode().equals(70031) || errorResponse.getErrorCode().equals(70032)){
                            log.error("隔1s重试");
                            Thread.sleep(1000);
                            batchSkuPriceUpdate(detailDO,accessToken);
                        }
                        //执行失败保存
                        detailDO.setState(2);
                        detailDO.setErrMsg(errorResponse.getErrorMsg());
                        goodsBatchDetailService.updateById(detailDO);
                        return;
                    }
                    PddGoodsSkuPriceUpdateResponse.GoodsUpdateSkuPriceResponse goodsUpdateSkuPriceResponse = pddGoodsSkuPriceUpdateResponse.getGoodsUpdateSkuPriceResponse();
                    Boolean isSuccess = goodsUpdateSkuPriceResponse.getIsSuccess();
                    if(isSuccess){
                        detailDO.setState(1);
                        goodsBatchDetailService.updateById(detailDO);
                    }else {
                        detailDO.setState(2);
                        detailDO.setErrMsg("接口调用成功,结果失败");
                        goodsBatchDetailService.updateById(detailDO);
                    }
                }else {
                    detailDO.setState(2);
                    detailDO.setErrMsg("接口无响应");
                    goodsBatchDetailService.updateById(detailDO);
                }
            }else {
                detailDO.setState(2);
                detailDO.setErrMsg("新值为空");
                goodsBatchDetailService.updateById(detailDO);
            }
        } catch (Exception e) {
            log.error(e.toString(),e);
            detailDO.setState(2);
            detailDO.setErrMsg(e.getMessage());
            goodsBatchDetailService.updateById(detailDO);
        }
    }
    private void quantityUpdate(String accessToken,Long goodsId,Long skuId,Long quantity,String batchDetailId,HashMap<Long,String> msg){
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
                        quantityUpdate(accessToken,goodsId,skuId,quantity,batchDetailId,msg);
                    }else {
                        msg.put(skuId,errorResponse.getErrorMsg());
                    }
                    return;
                }
                PddGoodsQuantityUpdateResponse.GoodsQuantityUpdateResponse goodsQuantityUpdateResponse = pddGoodsQuantityUpdateResponse.getGoodsQuantityUpdateResponse();
                if(goodsQuantityUpdateResponse != null){
                    if(goodsQuantityUpdateResponse.getIsSuccess()){
                        return;
                    }
                }
                msg.put(skuId,"更新失败,原因未知");
            }
        } catch (Exception e) {
            log.error(e.toString(),e);
            msg.put(skuId,e.getMessage());
        }

    }
}
