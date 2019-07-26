package com.wdb.pdd.api.service.cats.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wdb.pdd.api.dao.cats.GoodsCatsDao;
import com.wdb.pdd.api.pojo.entity.GoodsCatsDO;
import com.wdb.pdd.api.service.cats.IGoodsCatsService;
import org.springframework.stereotype.Service;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/9 0009
 * @描述
 */
@Service
public class GoodsCatsServiceImpl extends ServiceImpl<GoodsCatsDao, GoodsCatsDO> implements IGoodsCatsService {
}
