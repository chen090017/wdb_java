package com.wdb.pdd.api.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/6 0006
 * @描述
 */
@TableName("wdb_goods_logistics_template")
@Data
public class GoodsLogisticsTemplateDO implements Serializable {
    private static final long serialVersionUID = 148222911797235405L;

    @TableId(type = IdType.INPUT)
    private String id;

    private Integer reguserId;

    /**
     * 模板id
     */
    private Long templateId;

    /**
     * 运费模板名称
     */
    private String templateName;

    /**
     * 同步时间
     */
    private Date addTime;
}
