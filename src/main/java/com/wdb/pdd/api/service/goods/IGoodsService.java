package com.wdb.pdd.api.service.goods;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pdd.pop.sdk.http.api.response.PddGoodsImageUploadResponse;
import com.wdb.pdd.api.pojo.entity.*;
import com.wdb.pdd.api.pojo.vo.GoodsDetail;
import com.wdb.pdd.api.pojo.vo.GoodsVO;
import com.wdb.pdd.common.utils.PageUtils;

import java.util.List;
import java.util.Map;

public interface IGoodsService extends IService<GoodsDO> {

    /**
     * 删除用户商品数据列表
     * @param reguserId
     */
    void removeAll(Integer reguserId);

    /**
     * 分页查询用户商品表数据
     * @param params
     * @return
     */
    PageUtils queryPage(Map params);

    /**
     * 将数据库中存储的转换数据 转成相应对象
     * @param goodsDO
     * @return
     */
    GoodsVO goodsDO2Response(GoodsDO goodsDO);

    /**
     * 获取各商品状态数量
     */
    Map<String,Integer> countByStatus(Map params);


    GoodsDetail getGoodsByID(Integer reguserId, Long goodsId);

    PddGoodsImageUploadResponse imgUpload2(Integer reguserId, String img);

    /**
     * 商品编辑
     * @param params
     * @return
     */
    Map<String,Object>  goodsUpdate(Map<String,Object>  params);

    /**
     * 商品编辑状态
     * @param
     * @return
     */
    Map<String,Object>  goodsCommitDetailGet(Integer reguserId,Long  goodsCommitId);



}
