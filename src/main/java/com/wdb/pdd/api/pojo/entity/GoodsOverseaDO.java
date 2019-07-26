package com.wdb.pdd.api.pojo.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/5 0005
 * @描述 商品海外数据参数 一对一 Object
 */
@Data
public class GoodsOverseaDO implements Serializable {
    private static final long serialVersionUID = -5675155022739973618L;

    /**
     * 消费税率
     */
    private Integer consumptionTaxRate;

    /**
     * 增值税率
     */
    private Integer valueAddedTaxRate;

    /**
     * 海关编号
     */
    private String hsCode;

    /**
     * 清关服务商
     */
    private String customsBroker;

    /**
     * 保税仓唯一标识
     */
    private String bondedWarehouseKey;

    public Boolean isNotNull(){
        if(consumptionTaxRate != null
                || valueAddedTaxRate != null
                || hsCode != null
                || customsBroker != null
                ||bondedWarehouseKey != null){
            return true;
        }
        return false;
    }
}
