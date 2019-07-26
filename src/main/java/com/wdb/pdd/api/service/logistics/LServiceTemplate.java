package com.wdb.pdd.api.service.logistics;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wdb.pdd.api.pojo.entity.GoodsLogisticsTemplateDO;

import java.util.List;

public interface LServiceTemplate  extends IService<GoodsLogisticsTemplateDO>  {

    List<GoodsLogisticsTemplateDO> Llist(Integer reguserId);

}
