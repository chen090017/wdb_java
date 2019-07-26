package com.wdb.pdd.api.service.sys.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wdb.pdd.api.dao.sys.AdminActionDao;
import com.wdb.pdd.api.pojo.entity.AdminAction;
import com.wdb.pdd.api.service.sys.AdminActionService;
import org.springframework.stereotype.Service;

@Service
public class AdminActionServiceImpl extends ServiceImpl<AdminActionDao, AdminAction> implements AdminActionService {
}
