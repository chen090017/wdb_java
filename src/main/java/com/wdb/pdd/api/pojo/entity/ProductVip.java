package com.wdb.pdd.api.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("wdb_sys_product_vip")
public class ProductVip {
    @TableId(type = IdType.INPUT)
    int id;
    int proId;
    int vipId;
    String vipTime;
    String addTime;
}
