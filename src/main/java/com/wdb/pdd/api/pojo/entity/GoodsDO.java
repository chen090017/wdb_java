package com.wdb.pdd.api.pojo.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/5 0005
 * @描述 商品数据
 */
@Data
@TableName("wdb_goods")
@AllArgsConstructor
@NoArgsConstructor
public class GoodsDO implements Serializable {
    private static final long serialVersionUID = 6004737720187436352L;

    @TableId(type = IdType.INPUT)
    private String id;
    private Integer reguserId;
    /**
     * 送货入户模版id
     */
    private String songHuoRuHu;

    /**
     * 上门安装模版id
     */
    private String shangMenAnZhuang;

    /**
     * 送货入户并安装模版id
     */
    private String songHuoAnZhuang;

    /**
     * 买家自提模版id
     */
    private String maiJiaZiTi;

    /**
     * 短标题，示例：新包装，保证产品的口感和新鲜度。单颗独立小包装，双重营养，1斤家庭分享装，更实惠新疆一级骏枣夹核桃仁。
     */
    private String tinyName;

    /**
     * 是否支持正品发票；0-不支持、1-支持
     */
    private Integer invoiceStatus;

    /**
     * 只换不修的天数，目前只支持0和365
     */
    private Integer zhiHuanBuXiu;

    /**
     * 0：不支持全国联保；1：支持全国联保
     */
    private Integer quanGuoLianBao;

    /**
     * 商品状态 1:上架，2：下架，3：售罄 4：已删除
     */
    private Integer status;

    /**
     * 商品id
     */
    private Long goodsId;

    /**
     * 限购次数
     */
    private Long buyLimit;

    /**
     * 商品轮播图列表
     */
    private String carouselGalleryList;

    /**
     * 叶子类目ID
     */
    private Long catId;

    /**
     * 运费模版id
     */
    private Long costTemplateId;

    /**
     * 国家id
     */
    private Integer countryId;

    /**
     * 团购人数
     */
    private Long customerNum;

    /**
     * 商品类型：1-国内普通商品，2-进口，3-国外海淘，4-直邮 ,5-流量,6-话费,7,优惠券;8-QQ充值,9-加油卡,18-CC行邮 暂时支持1-普通商品的上架
     */
    private Integer goodsType;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品描述
     */
    private String goodsDesc;

    /**
     * 保税仓
     */
    private String warehouse;

    /**
     * 是否需要上报海关 0:否 1:是
     */
    private Integer isCustoms;

    /**
     * 海关名称
     */
    private String customs;

    /**
     * 市场价格，单位为分
     */
    private Long marketPrice;

    /**
     * 是否预售,1-预售商品，0-非预售商品
     */
    private Integer isPreSale;

    /**
     * 预售时间
     */
    private Long preSaleTime;

    /**
     * 承诺发货时间（ 秒）
     */
    private Long shipmentLimitSecond;

    /**
     * 单次限量
     */
    private Long orderLimit;

    /**
     * 是否7天无理由退换货，1-支持，0-不支持
     */
    private Integer isRefundable;

    /**
     * 是否支持假一赔十，0-不支持，1-支持
     */
    private Integer isFolt;

    /**
     * 水果类目温馨提示
     */
    private String warmTips;


    /**
     * 商品主图
     */
    private String imageUrl;

    /**
     * 商品详情图
     */
    private String detailGalleryList;

    /**
     * 商家编码（商品维度），同其他接口中的outer_goods_id 、out_goods_id、out_goods_sn、outer_goods_sn 都为商家编码（goods维度）。
     */
    private String outerGoodsId;

    /**
     * 是否二手 1:是 0:否
     */
    private Integer secondHand;

    /**
     * 缺重包退
     */
    private Integer lackOfWeightClaim;

    /**
     * 坏果包赔
     */
    private Integer badFruitClaim;

    /**
     * 商品属性列表
     */
    private String goodsPropertyList;
    /**
     * sku列表
     */
    private String skuList;
    /**
     * oversea_goods
     */
    private String overseaGoods;
    /**
     * 商品视频
     */
    private String carouselVideo;

    /**
     * 用户新同步商品时间
     */
    private Date addTime;
    /**
     * 当前商品数据变动数据库时间
     */
    private Date updateTime;
}
