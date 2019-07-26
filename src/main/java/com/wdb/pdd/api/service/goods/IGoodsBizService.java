package com.wdb.pdd.api.service.goods;

import com.pdd.pop.sdk.http.api.request.PddGoodsImageUploadRequest;
import com.pdd.pop.sdk.http.api.response.PddGoodsDetailGetResponse;
import com.pdd.pop.sdk.http.api.response.PddGoodsImageUploadResponse;
import com.pdd.pop.sdk.http.api.response.PddGoodsInformationUpdateResponse;
import com.wdb.pdd.api.pojo.entity.GoodsDO;

import java.util.HashMap;
import java.util.List;

/**
 * 商品业务服务类 非POJO对应Service
 */
public interface IGoodsBizService {

    /**
     * 获取该用户商品总数
     * @param id
     * @return
     */
    Integer getAllCount(Integer id);

    /**
     * 执行商品列表读取分批传入下一个处理方法
     * -> 首先删除该用户下所有商品
     * -> 根据当前内存环境设置每次分页获取的数据量
     * -> 循环获取商品列表 单个传入下一个处理单商品查询
     */
    void getGoodsList2Batch(Integer id,String accessToken);

    /**
     * 商品详情接口相应 置换GoodsBean
     * @param reguserId
     * @param goodsDetailGetResponse
     * @return
     */
    GoodsDO response2GoodsBean(Integer reguserId, PddGoodsDetailGetResponse.GoodsDetailGetResponse goodsDetailGetResponse);

    /**
     * 删除下架商品
     * @param ids
     * @param reguserId
     */
    boolean remove(List<Long> ids,Integer reguserId);

    /**
     * 批量上下架商品
     * @param ids
     * @param reguserId
     */
    HashMap<String,Object> unline(List<Long> ids, Integer reguserId, Integer isOnsale);

    /**
     * 更新单个商品后执行单个商品详情同步
     * @param goodsId
     */
    void update2Save(Long goodsId);

    /**
     * 商品图片上传
     * @param request
     * @return
     */
    PddGoodsImageUploadResponse imgUpload(PddGoodsImageUploadRequest request,Integer reguserId);

    /**
     *  获取商品更新编辑id
     * @param goodsId
     * @param reguserId
     * @return
     */
    PddGoodsInformationUpdateResponse getGoodsCommitId(Long goodsId,Integer reguserId);
}
