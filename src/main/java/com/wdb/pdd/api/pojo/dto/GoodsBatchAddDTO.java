package com.wdb.pdd.api.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/9 0009
 * @描述 这是新增批处理任务的实体DTO对象
 */
@Data
public class GoodsBatchAddDTO {
    /**
     * 用户注册id
     */
    private Integer reguserId;
    /**
     * 已选择商品
     */
    private List<String> select;
    /**
     * 全选状态下 未选择商品集合
     */
    private List<String> unSelect;
    /**
     * 选择状态 0默认 1当前页全选 2所有全选
     */
    private Integer selectType;
    /**
     * 当前商品状态 默认为空 取goods表status
     */
    private Integer status;
    /**
     * 商品品类id
     */
    private Long catId;
    /**
     * 批处理类型
     *       1 批量修改 标题
     *       2 批量修改 价格
     *       3 批量修改 库存
     *       4 批量修改 分类
     *       5 批量修改 预售 TODO 预售无法设置发货时间
     *       6 批量修改 描述
     *       7 批量修改 物流重量
     *       8 批量修改 运费
     *       9 批量修改 食品属性 TODO 没找到食品属性在哪
     *       10批量修改 团购参数
     *       11批量修改 服务承诺
     */
    private Integer batchType;

    private String batchDetail;
}
