package com.wdb.pdd.api.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pdd.pop.ext.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/5 0005
 * @描述 商品属性列表 一对多 Object[]
 */
@Data
public class GoodsPropertyDO implements Serializable {
    private static final long serialVersionUID = -8313097798550396560L;

    /**
     * 引用属性id
     */
    private Long refPid;

    /**
     * 模板属性Id
     */
    private Long templatePid;

    /**
     * 基础属性值
     */
    private String vvalue;

    /**
     * 基础属性值Id
     */
    private Long vid;

    /**
     * 属性单位
     */
    private String punit;
}
