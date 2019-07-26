package com.wdb.pdd.api.pojo.vo;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/18 0018
 * @描述
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodsBatchDetailVO {
    private String newValue;
    private Long goodsId;
    private String errMsg;
    private String id;
    private String oldValue;
    private String state;
    private String batchId;
    private Integer batchType;
    private String goodsName;
    private String carouselGalleryList;

    public String getImgUrl(){
        try {
            if(carouselGalleryList != null){
                List<String> strings = JSON.parseArray(carouselGalleryList, String.class);
                if (strings!=null && strings.size() > 0){
                    return strings.get(0);
                }
            }
        } catch (Exception e) {
        }
        return null;
    }
}
