package com.wdb.pdd.api.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/10 0010
 * @描述
 */
@Data
public class GoodsSelectUpdateDTO {
    /**
     * 选择id
     */
    private List<Long> ids;
    /**
     * 上下架状态
     * 1:上架 0:下架
     */
    private Integer isOnsale;
    private Integer reguserId;
}
