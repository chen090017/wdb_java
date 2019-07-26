package com.wdb.pdd.api.service.activity.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wdb.pdd.api.dao.activity.ActivityDao;

import com.wdb.pdd.api.pojo.entity.Activity;

import com.wdb.pdd.api.service.activity.IActivityService;

import org.springframework.stereotype.Service;
@Service
public class IActivityServiceImpl extends ServiceImpl<ActivityDao, Activity> implements IActivityService  {


}
