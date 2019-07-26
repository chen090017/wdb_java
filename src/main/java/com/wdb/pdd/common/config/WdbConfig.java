package com.wdb.pdd.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/4 0004
 * @描述
 */
@Data
@Component
@ConfigurationProperties(prefix = "wdb")
public class WdbConfig {
    /**
     * 项目名，末尾不带 "/"
     */
    private String projectName;
    /**
     * 项目根目录，末尾带 "/"
     */
    private String projectRootURL;
    /**
     * 机器ID
     */
    private long workId;
    /**
     * 数据ID
     */
    private long datacenterId;
}
