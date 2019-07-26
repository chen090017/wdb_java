package com.wdb.pdd.api.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/9 0009
 * @描述
 */
@Data
@TableName("wdb_goods_cats")
public class GoodsCatsDO implements Serializable {

    private static final long serialVersionUID = -2168862667211960913L;

    @TableId(type = IdType.INPUT)
    private String id;

    private String catName;
    private Long catId;
    private Long parentCatId;
    private Integer level;
    private Date addTime;
}
