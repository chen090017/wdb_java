package com.wdb.pdd.common.utils;

import com.wdb.pdd.common.config.WdbConfig;

import java.util.UUID;

/**
 * 改为调用Sequence方法
 */
public class IdUtil {

    /**
     * 主机和进程的机器码
     */
    private static final MySequence worker = new MySequence(SpringContextHolder
            .getApplicationContext()
            .getBean(WdbConfig.class)
            .getWorkId()
            ,SpringContextHolder
            .getApplicationContext()
            .getBean(WdbConfig.class)
            .getDatacenterId());

	public static long getId() {
		return worker.nextId();
	}

	public static String getStrId(){
		return String.valueOf(getId());
	}

	/**
	 * <p>
	 * 获取去掉"-" UUID
	 * </p>
	 */
	public static synchronized String get32UUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}

}
