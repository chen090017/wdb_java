package com.wdb.pdd.api.pojo.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/5 0005
 * @描述
 */
@Data
public class GoodsSkuOverseaDO implements Serializable {
    private static final long serialVersionUID = -88673652314886428L;
    private String measurementCode;
    private Integer taxation;
    private String specifications;


}
