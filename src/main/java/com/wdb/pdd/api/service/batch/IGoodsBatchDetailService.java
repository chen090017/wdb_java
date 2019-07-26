package com.wdb.pdd.api.service.batch;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wdb.pdd.api.pojo.entity.GoodsBatchDetailDO;
import com.wdb.pdd.api.pojo.vo.GoodsBatchDetailVO;

import java.util.HashMap;
import java.util.List;

public interface IGoodsBatchDetailService extends IService<GoodsBatchDetailDO> {
    List<GoodsBatchDetailVO> getList(HashMap req);
}
