package com.wdb.pdd.api.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/14 0014
 * @描述
 */
@TableName("wdb_goods_sync")
@Data
public class GoodsSyncDO implements Serializable {
    private static final long serialVersionUID = 3097468630084487997L;
    @TableId(type = IdType.INPUT)
    private String id;
    private Integer reguserId;
    /**
     * 0默认正在同步 1同步完成 2同步失败
     */
    private Integer status;
    private Date createDate;
    private Date modifyDate;

}
