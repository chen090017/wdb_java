package com.wdb.pdd.api.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/18 0018
 * @描述
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsBatchVO {
    private String id;

    /**
     * 1 批量修改 标题
     * 2 批量修改 价格
     * 3 批量修改 库存
     * 4 批量修改 分类
     * 5 批量修改 预售 TODO 预售无法设置发货时间
     * 6 批量修改 描述
     * 7 批量修改 物流重量
     * 8 批量修改 运费
     * 9 批量修改 食品属性 TODO 没找到食品属性在哪
     * 10批量修改 团购参数
     * 11批量修改 服务承诺
     */
    private Integer batchType;
    /**
     * base64过的各个参数值
     */
    private String batchDesc;
    private Date addTime;
    private Integer reguserId;
    /**
     * 0待处理 1处理中 2处理完成
     */
    private Integer batchStatus;
    private Integer failNum;
    private Integer allNum;
    private Integer successNum;
}
