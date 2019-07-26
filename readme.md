#### 商品采集功能
```
拼多多所有数据封装js对象在<script>标签中
淘宝爬虫第一页不动态加载可以获取除详情图之外的所有信息 详情图需要另外加载获取
```
#### 拼多多接口SDK调用 JAVA_API
##### 基础构建
```
    Controller切面日志
        com.wdb.pdd.commons.aspects.WebLogAspect
    统一异常处理类
        com.wdb.pdd.commons.exceptions.handlers.ApplicationExceptionHandler
    异步线程池配置
        com.wdb.pdd.commons.configs.TaskPoolConfig
    异步线程启动配置类
        com.wdb.pdd.commons.task.AsyncTask
    SpringBatch批处理Demo包
        com.wdb.pdd.commons.task.demo
        定时任务Task
            com.wdb.pdd.commons.task.demo.ScheduledTasks
```
##### 业务实现
```
    商品模块
        商品同步
            /api/goods/sync/{id}
        商品列表
            /api/goods/listByPage?page=1&limit=10&reguserId=5
```

###### 请求地址:http://192.168.1.177:8088/wdb
```
商品:/api/goods 
		同步商品:/sync GET
		商品列表:/listByPage GET 
			请求参数: limit页面条数 page页数 catId类目id isPreSale是否预售 status商品状态
		删除商品:/remove POST 
			请求参数：[id] 商品id
		上下架商品:/unline POST 
			请求参数：[id] 商品id isOnsale上下架状态
        同步进度：/syncDetail
        获取编辑页面组装id:/commitId
```
```
运费模板:/api/goodsLogisticsTemplate
		同步运费模板:/sync GET
		模板列表:/list GET
```
```
商品类目:/api/goodsCats
	    所有类目同步:/sync GET不要轻易调用,拼多多类目不会轻易变动 2C8G需要9分钟
		获取可用子类目:/sub GET 
			请求参数：catId当前类目id
		获取当前类目父祖类目:/parent GET 
			请求参数：catId当前类目id
        获取该商品的类目子类目：subByGoods GET
```
```
批量更新:/api/goodsBatch
		保存修改添加新任务:/add POST 
			请求参数：{
							'select':[],已选择
							'unSelect':[],未选择
							'selectType':0,选择状态 0默认 1当前页全选 2所有全选
							'status':123,当前筛选条件商品状态
							'catId':111,当前筛选条件类目id
							'batchType':1,批处理类型
							'batchDetail':'ewonYmF0Y2hDaGFubmVsJzonYWRkJywKJ3YxJzoyCn0='实际操作值JSON的base64
						}
				batchType:
					1 批量修改 标题
					2 批量修改 价格
					3 批量修改 库存
					4 批量修改 分类
					5 批量修改 预售
					6 批量修改 描述
					7 批量修改 物流重量 TODO 没找到物流重量在哪
					8 批量修改 运费
					9 批量修改 食品属性 TODO 没找到食品属性在哪
					10批量修改 团购参数
					11批量修改 服务承诺
					12批量修改 单买价
					13批量修改 团购价
				batchDetail:联调的时候具体告知参数
                        ｛
                                2-1 批量 标题 不支持多设置 batchType:1
                                    3
                                        add 增加关键字  v1+(原标题)+v2
                                        update 替换关键字 v1 -> v2
                                        del 删除关键字 -v1
                                2-2 批量 价格 batchType:2
                                    2
                                        update 一口价 =v1
                                        compute 公式计算 *v1 +v2 -v3 =>v4(此为类型 是否去除角分位)
                                2-3 批量 库存 batchType:3
                                    3
                                        update 修改 =v1
                                        add 加库存 +v1
                                        del 减库存且不低于 -v1 >=v2
                                2-4 批量 分类 batchType:4
                                    1
                                        update 直接修改分类 =v1
                                2-5 批量 预售 batchType:5
                                    1
                                        v1 0:1 v2 48/24*60*60:**23:59:59
                                2-6 批量 描述 batchType:6
                                    3
                                        add v1+(原描述)+v2
                                        del -v1
                                        update =v1
                                2-7 批量 物流重量 batchType:7
                                    1
                                        update =v1
                                2-8 批量 运费模板 50% batchType:8
                                    1
                                        update =v1
                                2-9 批量 食品属性 batchType:9
                                    1
                                        ?没有API
                                2-10 批量 团购人数 batchType:10
                                    1
                                        v1单次限量orderLimit v2限购次数buyLimit
                                2-11 批量 服务承诺 batchType:11
                                    1
                                        v1 7天isRefundable v2 假一isFolt  1为true

                                2-12 批量 价格 batchType:12  单买价
                                    2
                                        update 一口价 =v1
                                        compute 公式计算 *v1 +v2 -v3 =>v4(此为类型 是否去除角分位)
                                2-13 批量 价格 batchType:13  团购价
                                    2
                                        update 一口价 =v1
                                        compute 公式计算 *v1 +v2 -v3 =>v4(此为类型 是否去除角分位)
                        ｝
        获取批处理任务列表：/listByPage
        获取批处理任务详情：/info
```	

    TODO 上线部署server_jdk在resources目录中 如果没有自行去oracle官网下载191-202版本的ServerJdk
    解包
    tar -zxvf
    设置环境变量
    vi /etc/profile
    export JAVA_HOME=/soft/jdk1.8.0_191
    export JRE_HOME=/soft/jdk1.8.0_191/jre
    export CLASSPATH=.:$JAVA_HOME/lib:$JRE_HOME/lib:$CLASSPATH
    export PATH=$JAVA_HOME/bin:$JRE_HOME/bin:$PATH
    
    保存构建
    source /etc/profile
    构建快捷链接
    ln -s /soft/jdk1.8.0_191/bin/java /usr/bin

    启动项目shell脚本 shell/start.sh 设置堆内存
    
    项目目录在/www/wwwroot/pdd_wdb_java下 启动start.sh即可  停项目直接jps查启动jar kill -s 9 {PID}
    项目日志文件/www/wwwroot/pdd_wdb_java/logs下 由于日志打的多,仅保留5天日志 子文件夹/info /error为所有日志和错误日志
    
    本地虚机账号密码：root/q1w2e3r4 
    
    