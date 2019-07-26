package com.wdb.pdd.common.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/5 0005
 * @描述 SpringBoot定时器配置类 用于调用SpringBatch进行批处理
 */
//@Component
//@Configurable
//@EnableScheduling
public class ScheduledTasks{

    private Logger log = LoggerFactory.getLogger(getClass());
    //@Autowired
    //private AsyncTask asyncTask;

    /**
     * 每隔多少时间运行
     */
    /*@Scheduled(fixedRate = 1000 * 30)
    public void reportCurrentTime(){
        System.out.println ("Scheduling Tasks Examples: The time is now " + DateUtil.now());
    }*/

    /**
     * cron表达式运行
     */
    //@Scheduled(cron = "0,30 * * * * ? ")
    public void reportCurrentByCron(){
        log.info("定时任务调用批处理商品更新");
        //asyncTask.doTaskForBatchUpdateGoods();
    }
}