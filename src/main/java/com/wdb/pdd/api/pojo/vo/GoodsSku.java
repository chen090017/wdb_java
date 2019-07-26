package com.wdb.pdd.api.pojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class GoodsSku {
private String skuName;
private String thumbUrl;
private  Long skuId;
private List<String> spec;
private Long weight;
private Long quantity;
private String outSkuSn;
private Long   multiPrice;
private Long   price;
private Long   limitQuantity;
private Integer isOnsale;
}
