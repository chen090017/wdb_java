package com.wdb.pdd.api.pojo.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/5 0005
 * @描述
 */
@Data
public class GoodsSkuSpecDO implements Serializable {
    private static final long serialVersionUID = -6267035602980829010L;
   private Long	parentId;
   private String parentName;
   private Long	specId;
   private String specName;

}
