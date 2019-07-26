package com.wdb.pdd.api.service.sys.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wdb.pdd.api.dao.sys.VipDao;
import com.wdb.pdd.api.pojo.entity.Vip;
import com.wdb.pdd.api.service.sys.VipService;
import org.springframework.stereotype.Service;

@Service
public class VipServiceImpl extends ServiceImpl<VipDao, Vip> implements VipService {
}
