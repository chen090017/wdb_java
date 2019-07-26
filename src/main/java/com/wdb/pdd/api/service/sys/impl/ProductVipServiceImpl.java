package com.wdb.pdd.api.service.sys.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wdb.pdd.api.dao.sys.ProductVipDao;
import com.wdb.pdd.api.pojo.entity.ProductVip;
import com.wdb.pdd.api.service.sys.ProductVipService;
import org.springframework.stereotype.Service;

@Service
public class ProductVipServiceImpl extends ServiceImpl<ProductVipDao, ProductVip> implements ProductVipService {
}
