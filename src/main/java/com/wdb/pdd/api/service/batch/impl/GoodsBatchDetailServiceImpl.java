package com.wdb.pdd.api.service.batch.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wdb.pdd.api.dao.batch.GoodsBatchDetailDao;
import com.wdb.pdd.api.pojo.entity.GoodsBatchDetailDO;
import com.wdb.pdd.api.pojo.vo.GoodsBatchDetailVO;
import com.wdb.pdd.api.service.batch.IGoodsBatchDetailService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/9 0009
 * @描述
 */
@Service
public class GoodsBatchDetailServiceImpl extends ServiceImpl<GoodsBatchDetailDao, GoodsBatchDetailDO> implements IGoodsBatchDetailService {

    @Override
    public List<GoodsBatchDetailVO> getList(HashMap req) {
        return this.baseMapper.getList(req);
    }
}
