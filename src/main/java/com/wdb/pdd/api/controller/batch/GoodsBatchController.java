package com.wdb.pdd.api.controller.batch;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.codec.Base64Encoder;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pdd.pop.sdk.http.PopClient;
import com.pdd.pop.sdk.http.PopHttpClient;
import com.pdd.pop.sdk.http.api.request.PddGoodsInformationUpdateRequest;
import com.pdd.pop.sdk.http.api.request.PddGoodsLogisticsSerTemplateListRequest;
import com.pdd.pop.sdk.http.api.response.PddGoodsInformationGetResponse;
import com.pdd.pop.sdk.http.api.response.PddGoodsLogisticsSerTemplateListResponse;
import com.wdb.pdd.api.pojo.dto.GoodsBatchAddDTO;
import com.wdb.pdd.api.pojo.entity.GoodsBatchDO;
import com.wdb.pdd.api.pojo.entity.GoodsBatchDetailDO;
import com.wdb.pdd.api.pojo.vo.GoodsBatchDetailVO;
import com.wdb.pdd.api.service.batch.IGoodsBatchDetailService;
import com.wdb.pdd.api.service.batch.IGoodsBatchService;
import com.wdb.pdd.common.annotation.CheckToken;
import com.wdb.pdd.common.task.AsyncTask;
import com.wdb.pdd.common.utils.IdUtil;
import com.wdb.pdd.common.utils.PageUtils;
import com.wdb.pdd.common.utils.Result;
import com.wdb.pdd.common.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/9 0009
 * @描述
 */
@RestController
@RequestMapping("/api/goodsBatch")
public class GoodsBatchController {
    @Autowired
    private IGoodsBatchService goodsBatchService;
    @Autowired
    private IGoodsBatchDetailService goodsBatchDetailService;
    @Autowired
    private AsyncTask asyncTask;

    @Autowired
    private PopHttpClient popHttpClient;

    /**
     * 新增任务
     */
    @CheckToken
    @PostMapping("/add")
    public Result<?> addBatch(@RequestBody GoodsBatchAddDTO goodsBatchAddDTO){
        goodsBatchAddDTO.setReguserId(UserUtils.getReguserId());
        /**
         * 这里需要校验reguserId 暂时在请求体中
         * 更新将reguserId放进头部校验
         */
        if(goodsBatchAddDTO.getSelectType() == null) {
            goodsBatchAddDTO.setSelectType(0);
        }
        String strId = IdUtil.getStrId();
        GoodsBatchDO goodsBatchDO = new GoodsBatchDO();
        goodsBatchDO.setAddTime(new Date());
        goodsBatchDO.setBatchStatus(0);
        goodsBatchDO.setBatchType(goodsBatchAddDTO.getBatchType());
        goodsBatchDO.setId(strId);
        goodsBatchDO.setReguserId(goodsBatchAddDTO.getReguserId());
        goodsBatchDO.setBatchDesc(Base64Encoder.encode(JSON.toJSONString(goodsBatchAddDTO)));
        goodsBatchService.save(goodsBatchDO);
        asyncTask.doTaskForBatchUpdateGoods(strId);
        return Result.ok();
    }
    /**
     * 获取用户批处理任务列表
     * @param req
     *  -limit
     *  -page
     * @return
     */
    @CheckToken
    @GetMapping("/listByPage")
    public Result<?> listByPage(@RequestParam Map<String,Object> req){
        req.put("reguserId",UserUtils.getReguserId());
        PageUtils pageUtils = goodsBatchService.queryPage(req);
        HashMap<String, Object> data = new HashMap<>();
        data.put("page",pageUtils);
        return Result.ok(data);
    }
    /**
     * 查询任务详情
     */
    @CheckToken
    @GetMapping("/info")
    public Result<?> info(@RequestParam HashMap<String,Object> req){
        List<GoodsBatchDetailVO> list = goodsBatchDetailService.getList(req);
        return Result.ok(list);
    }

    @CheckToken
    @GetMapping("/info2")
    public Result<?> info2(@RequestParam HashMap<String,Object> req){
        PddGoodsLogisticsSerTemplateListRequest request = new PddGoodsLogisticsSerTemplateListRequest();

        String clientId = "35b60b149c5e4dbaa0586213537054cc";
        String clientSecret = "1796f86a4621ce22cd6a7bba60c2b1eb6b3e5848";
        String accessToken = "122";
        PopClient client = new PopHttpClient(clientId, clientSecret);
         Long id=8902644665L;
        request.setTemplateType(0);
        request.setStart(0);
        request.setLength(0);
        request.setQueryType(0);
//        request.setGoodsId(id);
//        PddGoodsInformationGetResponse response = client.syncInvoke(request, accessToken);
//        PddGoodsLogisticsSerTemplateListResponse response2 = popHttpClient.syncInvoke(request, accessToken);


        String ss="asd";
        return  Result.ok(ss);

    }



}
