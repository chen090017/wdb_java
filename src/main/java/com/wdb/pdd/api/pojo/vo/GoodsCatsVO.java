package com.wdb.pdd.api.pojo.vo;

import lombok.Data;

import java.util.List;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/9 0009
 * @描述
 */
@Data
public class GoodsCatsVO {
    private Long catId;
    private String catName;
    private Long parentCatId;
    private List<GoodsCatsVO> subGoodsCats;
}
