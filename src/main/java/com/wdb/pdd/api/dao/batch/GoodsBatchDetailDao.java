package com.wdb.pdd.api.dao.batch;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wdb.pdd.api.pojo.dto.GoodsBatchDetailCountDTO;
import com.wdb.pdd.api.pojo.entity.GoodsBatchDetailDO;
import com.wdb.pdd.api.pojo.vo.GoodsBatchDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface GoodsBatchDetailDao extends BaseMapper<GoodsBatchDetailDO> {

    List<GoodsBatchDetailVO> getList(@Param("pojo") HashMap pojo);

    List<GoodsBatchDetailCountDTO> countByState(@Param("batchId")String id);
}
