package com.wdb.pdd.api.dao.product;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wdb.pdd.api.pojo.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductDao extends BaseMapper<Product> {
}
