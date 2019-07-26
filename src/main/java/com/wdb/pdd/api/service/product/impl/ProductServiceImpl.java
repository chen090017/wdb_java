package com.wdb.pdd.api.service.product.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wdb.pdd.api.dao.product.ProductDao;
import com.wdb.pdd.api.pojo.entity.Product;
import com.wdb.pdd.api.service.product.ProductService;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductDao, Product> implements ProductService {
}
