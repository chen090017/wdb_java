package com.wdb.pdd;

import com.wdb.pdd.common.config.WdbConfig;
import com.wdb.pdd.common.utils.SpringContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication
@EnableTransactionManagement
@ServletComponentScan
@EnableAsync
public class JavaApiApplication {

    private static Logger log = LoggerFactory.getLogger(JavaApiApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(JavaApiApplication.class, args);
        printProjectConfigs();
    }
    private static void printProjectConfigs() {
        DataSourceProperties dataSourceProperties = SpringContextHolder.getApplicationContext().getBean(DataSourceProperties.class);
        WdbConfig config = SpringContextHolder.getApplicationContext().getBean(WdbConfig.class);
        log.info("数据库：" + dataSourceProperties.getUrl());
        log.info("==================> run at "+config.getProjectRootURL()+ "  <==================");
    }
}
