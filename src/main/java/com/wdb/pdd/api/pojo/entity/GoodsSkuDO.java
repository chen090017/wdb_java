package com.wdb.pdd.api.pojo.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/5 0005
 * @描述 商品Sku列表 一对多 Object[]
 * 表关联的表名_id 为id 非其他字段同名字段
 */
@Data
public class GoodsSkuDO implements Serializable {
    private static final long serialVersionUID = 3750521484454143703L;

    /**
     * sku送装参数：长度
     */
    private Long length;

    /**
     * sku编码
     */
    private Long skuId;

    /**
     * 上下架状态 1：上架 0 ：下架
     */
    private Integer isOnsale;

    /**
     * sku购买限制
     */
    private Long limitQuantity;

    /**
     * 商品团购价格 单位分
     */
    private Long multiPrice;

    /**
     * 商品单买价格 单位分
     */
    private Long price;

    /**
     * 库存
     */
    private Long quantity;

    /**
     * 重量，单位为g
     */
    private Long weight;

    /**
     * sku预览图
     */
    private String thumbUrl;

    /**
     * 商家编码（sku维度），同其他接口中的outer_id 、out_id、out_sku_sn、outer_sku_sn、out_sku_id、outer_sku_id 都为商家编码（sku维度）。
     */
    private String outSkuSn;

    /**
     * oversea_sku
     */
    private String overseaSku;

    /**
     * 商品规格列表
     */
    private String spec;
}
