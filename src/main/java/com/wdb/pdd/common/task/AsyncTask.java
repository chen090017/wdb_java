package com.wdb.pdd.common.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wdb.pdd.api.pojo.entity.GoodsBatchDetailDO;
import com.wdb.pdd.api.pojo.entity.GoodsSyncDO;
import com.wdb.pdd.api.pojo.entity.ReguserDO;
import com.wdb.pdd.api.service.batch.IGoodsBatchBizService;
import com.wdb.pdd.api.service.batch.IGoodsBatchDetailService;
import com.wdb.pdd.api.service.batch.IGoodsBatchService;
import com.wdb.pdd.api.service.cats.IGoodsCatsBizService;
import com.wdb.pdd.api.service.goods.IGoodsBizService;
import com.wdb.pdd.api.service.goods.IGoodsSyncService;
import com.wdb.pdd.api.service.sys.IReguserService;
import com.wdb.pdd.common.exception.MyException;
import com.wdb.pdd.common.exception.handlers.EnumErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/5 0005
 * @描述 异步任务执行器
 */
@Component
public class AsyncTask {

    private Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    private IReguserService reguserService;
    @Autowired
    private IGoodsBizService goodsBizService;
    @Autowired
    private IGoodsCatsBizService goodsCatsBizService;
    @Autowired
    private IGoodsBatchBizService goodsBatchBizService;
    @Autowired
    private IGoodsBatchService goodsBatchService;
    @Autowired
    private IGoodsSyncService goodsSyncService;
    @Autowired
    private IGoodsBatchDetailService goodsBatchDetailService;

    /**
     * 异步执行同步线上商品到本地数据库
     */
    @Async("taskExecutor")
    public void doTaskForSyncGoods(Integer reguserId,String goodsSyncId) {
        long l1 = System.currentTimeMillis();
        log.info("=============开始执行同步线上商品到本地数据库 reguser_id:{}================", reguserId);
        //查询是否存在用户
        ReguserDO byId = reguserService.getById(reguserId);
        GoodsSyncDO goodsSyncDO = goodsSyncService.getById(goodsSyncId);
        if (byId == null) {
            log.error("不存在该用户_id:{}", reguserId);
            goodsSyncDO.setStatus(2);
            goodsSyncService.updateById(goodsSyncDO);
            throw new MyException(EnumErrorCode.apiAuthorizationFailed.getCodeStr());
        }
        //校验到期时间
        long l = System.currentTimeMillis();
        if (byId.getExpiresIn() < (l / 1000)) {
            byId = reguserService.refreshToken(reguserId);
            if (byId == null) {
                goodsSyncDO.setStatus(2);
                goodsSyncService.updateById(goodsSyncDO);
                log.error("刷新Token失败_id:{}", reguserId);
                throw new MyException(EnumErrorCode.apiAuthorizationFailed.getCodeStr());
            }
        }
        String accessToken = byId.getAccessToken();
        goodsBizService.getGoodsList2Batch(reguserId, accessToken);
        goodsSyncDO.setStatus(1);
        goodsSyncService.updateById(goodsSyncDO);
        long l2 = System.currentTimeMillis();
        long l3 = l2 - l1;
        log.info("=================异步线程执行完毕=================耗时{}ms", l3);
    }

    @Async("taskExecutor")
    public void doTaskForSyncCats() {
        long l1 = System.currentTimeMillis();
        log.info("=============开始执行同步所有类目到本地数据库===============");
        try {
            goodsCatsBizService.syncCats();
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        long l2 = System.currentTimeMillis();
        long l3 = l2 - l1;
        log.info("=================异步线程执行完毕=================耗时{}ms", l3);
    }

    @Async("batchTaskExecutor")
    public void doTaskForBatchUpdateGoods(String batchId) {
        long l1 = System.currentTimeMillis();
        log.info("=============开始执行批量修改同步任务===============");
        try {
            goodsBatchBizService.reader(batchId);
        } catch (Exception e) {
            log.error(e.toString(), e);
            return;
        }
        try {
            goodsBatchBizService.writer(batchId);
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        List<GoodsBatchDetailDO> list = goodsBatchDetailService.list(new LambdaQueryWrapper<GoodsBatchDetailDO>()
                .eq(GoodsBatchDetailDO::getBatchId, batchId)
                .eq(GoodsBatchDetailDO::getState, 1));
        if(list != null && list.size() > 0){
            ArrayList<Long> goodsIds = new ArrayList<>();
            for (GoodsBatchDetailDO detailDO : list){
                goodsIds.add(detailDO.getGoodsId());
            }
            doTaskForOneGoodsSync(goodsIds);
        }
        long l2 = System.currentTimeMillis();
        long l3 = l2 - l1;
        log.info("=================异步线程执行完毕=================耗时{}ms", l3);
    }

    @Async("taskExecutor")
    public void doTaskForOneGoodsSync(List<Long> goodsIds){
        long l1 = System.currentTimeMillis();
        log.info("=============开始执行同步单商品详情任务===============");
        if (goodsIds != null && goodsIds.size() > 0){
            for (Long goodsId : goodsIds){
                try {
                    goodsBizService.update2Save(goodsId);
                } catch (Exception e) {
                    log.error("该商品同步失败回滚 goodsId:{}",goodsId);
                    log.error(e.toString(),e);
                }
            }
        }
        long l2 = System.currentTimeMillis();
        long l3 = l2 - l1;
        log.info("=================异步线程执行完毕=================耗时{}ms", l3);
    }
}
