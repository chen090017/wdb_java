package com.wdb.pdd.api.service.batch;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wdb.pdd.api.pojo.entity.GoodsBatchDO;
import com.wdb.pdd.common.utils.PageUtils;

import java.util.Map;

public interface IGoodsBatchService extends IService<GoodsBatchDO> {

    PageUtils queryPage(Map params);
}
