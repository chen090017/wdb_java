package com.wdb.pdd.api.service.batch;

/**
 * 读取数据库详细批处理时间进行处理调度
 * reader读取并预处理掉
 * writer进行最后读取所有商品最新状态值写入
 */
public interface IGoodsBatchBizService {
    /**
     * 读取数据库当前应当异步处理的ID
     * 预处理到detail表中
     */
    void reader(String batchId);

    /**
     * 执行数据批量请求修改 入库
     */
    void writer(String batchId);
}
