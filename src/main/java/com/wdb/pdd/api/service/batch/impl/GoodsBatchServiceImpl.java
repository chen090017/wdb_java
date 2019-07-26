package com.wdb.pdd.api.service.batch.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wdb.pdd.api.dao.batch.GoodsBatchDao;
import com.wdb.pdd.api.dao.batch.GoodsBatchDetailDao;
import com.wdb.pdd.api.pojo.dto.GoodsBatchDetailCountDTO;
import com.wdb.pdd.api.pojo.entity.GoodsBatchDO;
import com.wdb.pdd.api.pojo.entity.GoodsBatchDetailDO;
import com.wdb.pdd.api.pojo.vo.GoodsBatchVO;
import com.wdb.pdd.api.service.batch.IGoodsBatchService;
import com.wdb.pdd.common.utils.PageUtils;
import com.wdb.pdd.common.utils.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/9 0009
 * @描述
 */
@Service
public class GoodsBatchServiceImpl extends ServiceImpl<GoodsBatchDao, GoodsBatchDO> implements IGoodsBatchService {
    @Autowired
    private GoodsBatchDetailDao goodsBatchDetailDao;

    @Override
    public PageUtils queryPage(Map params) {
        IPage<GoodsBatchDO> res = this.baseMapper.selectPage(new Query<>(params).getPage(), new LambdaQueryWrapper<GoodsBatchDO>()
                .eq(GoodsBatchDO::getReguserId, params.get("reguserId"))
                .orderByDesc(GoodsBatchDO::getAddTime));
        if(res != null){
            List<GoodsBatchDO> records = res.getRecords();
            System.out.println(records);
            List<GoodsBatchVO> newRecords = new ArrayList<>();
            if(records != null && records.size() > 0){
                for(GoodsBatchDO batchDO : records){
                    GoodsBatchVO goodsBatchVO = new GoodsBatchVO();
                    BeanUtil.copyProperties(batchDO,goodsBatchVO);
                    List<GoodsBatchDetailCountDTO> goodsBatchDetailCountDTOS = goodsBatchDetailDao.countByState(batchDO.getId());
                    Integer all = 0;
                    Integer success = 0;
                    Integer fail = 0;
                    if(goodsBatchDetailCountDTOS != null && goodsBatchDetailCountDTOS.size() > 0){
                        for(GoodsBatchDetailCountDTO dto : goodsBatchDetailCountDTOS){
                            all += dto.getNum();
                            if(dto.getState().equals(1)){
                                success = dto.getNum();
                            }else if(dto.getState().equals(2)){
                                fail = dto.getNum();
                            }
                        }
                    }
                    goodsBatchVO.setAllNum(all);
                    goodsBatchVO.setSuccessNum(success);
                    goodsBatchVO.setFailNum(fail);
                    newRecords.add(goodsBatchVO);
                }
                IPage<GoodsBatchVO> newRes = new Page<>();
                BeanUtil.copyProperties(res,newRes,"records","optimizeCountSql");
                newRes.setRecords(newRecords);
                return new PageUtils(newRes);
            }
        }
        return new PageUtils(res);
    }
}
