package com.wdb.pdd.api.dao.activity;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wdb.pdd.api.pojo.entity.Activity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ActivityDao extends BaseMapper<Activity> {
}
