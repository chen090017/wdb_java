package com.wdb.pdd.api.pojo.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/5 0005
 * @描述 商品视频 Object[]
 */
@Data
public class GoodsCarouselDO implements Serializable {
    private static final long serialVersionUID = -7951083242472783687L;

    /**
     * 商品视频id
     */
    private String fileId;

    /**
     * 商品视频url
     */
    private String videoUrl;
}
