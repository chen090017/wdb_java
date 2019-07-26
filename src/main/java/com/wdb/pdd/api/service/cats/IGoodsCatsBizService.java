package com.wdb.pdd.api.service.cats;

import com.wdb.pdd.api.pojo.entity.GoodsCatsDO;
import com.wdb.pdd.api.pojo.vo.GoodsCatsVO;

import java.util.List;

public interface IGoodsCatsBizService {

    /**
     * 同步类目
     */
    void syncCats() throws Exception;

    /**
     * 正向查询下级类目树
     * @param reguserId
     */
    GoodsCatsVO getSub(Integer reguserId, Long catId);

    /**
     * 逆向反查类目树
     */
    List<GoodsCatsDO> getParent(Long catId);
}
