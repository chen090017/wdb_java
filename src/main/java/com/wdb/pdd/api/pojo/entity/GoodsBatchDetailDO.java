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
@TableName("wdb_goods_batch_detail")
public class GoodsBatchDetailDO implements Serializable {
    private static final long serialVersionUID = 1073126216841645906L;

    @TableId(type = IdType.INPUT)
    private String id;
    private Long goodsId;
    private Integer reguserId;
    private String batchId;
    /**
     * 修改时查询的老值 base64
     */
    private String oldValue;
    /**
     * 需要修改后的值base64
     */
    private String newValue;
    private Date addTime;
    /**
     * 单条处理状态 0处理中 1成功 2失败
     */
    private Integer state;
    /**
     * 处理后错误JSON
     */
    private String errMsg;
    /**
     * 完成时间
     */
    private Date updateTime;

    private Integer batchType;

    private String batchChannel;

}
