package com.wdb.pdd.api.controller.goods;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pdd.pop.sdk.http.api.request.PddGoodsImageUploadRequest;
import com.pdd.pop.sdk.http.api.response.PddGoodsImageUploadResponse;
import com.wdb.pdd.api.pojo.dto.GoodsSelectUpdateDTO;
import com.wdb.pdd.api.pojo.entity.GoodsDO;
import com.wdb.pdd.api.pojo.entity.GoodsSyncDO;
import com.wdb.pdd.api.pojo.entity.ReguserDO;
import com.wdb.pdd.api.service.goods.IGoodsBizService;
import com.wdb.pdd.api.service.goods.IGoodsService;
import com.wdb.pdd.api.service.goods.IGoodsSyncService;
import com.wdb.pdd.common.annotation.CheckToken;
import com.wdb.pdd.common.exception.MyException;
import com.wdb.pdd.common.exception.handlers.EnumErrorCode;
import com.wdb.pdd.common.task.AsyncTask;
import com.wdb.pdd.common.utils.IdUtil;
import com.wdb.pdd.common.utils.PageUtils;
import com.wdb.pdd.common.utils.Result;
import com.wdb.pdd.common.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/5 0005
 * @描述
 */
@RestController
@RequestMapping("/api/goods")
public class GoodsController {

    @Autowired
    private AsyncTask asyncTask;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private IGoodsBizService goodsBizService;
    @Autowired
    private IGoodsSyncService goodsSyncService;

    /**
     * 商品同步
     * @return
     */
    @CheckToken
    @GetMapping("/sync")
    public Result<?> syncGoods(){
        Integer reguserId = UserUtils.getReguserId();
        if(reguserId == null){
            return Result.build(EnumErrorCode.paramsFail.getCode(),EnumErrorCode.paramsFail.getMsg());
        }
        GoodsSyncDO one = goodsSyncService.getOne(new LambdaQueryWrapper<GoodsSyncDO>()
                .eq(GoodsSyncDO::getReguserId, reguserId)
                .eq(GoodsSyncDO::getStatus, 0));
        if(one != null){
            return Result.build(EnumErrorCode.backing.getCode(),EnumErrorCode.backing.getMsg());
        }
        //执行插入一条新记录
        String goodsSyncId = IdUtil.getStrId();
        GoodsSyncDO goodsSyncDO = new GoodsSyncDO();
        goodsSyncDO.setCreateDate(new Date());
        goodsSyncDO.setReguserId(reguserId);
        goodsSyncDO.setId(goodsSyncId);
        goodsSyncDO.setStatus(0);
        goodsSyncService.save(goodsSyncDO);
        asyncTask.doTaskForSyncGoods(reguserId,goodsSyncId);
        return Result.ok("正在同步中");
    }

    /**
     * 查看商品同步进度
     * @return
     */
    @CheckToken
    @GetMapping("/syncDetail")
    public Result<?> syncDetail(){
        Integer reguserId = UserUtils.getReguserId();
        if(reguserId == null){
            return Result.build(EnumErrorCode.paramsFail.getCode(),EnumErrorCode.paramsFail.getMsg());
        }
        GoodsSyncDO one = goodsSyncService.getOne(new LambdaQueryWrapper<GoodsSyncDO>()
                .eq(GoodsSyncDO::getReguserId, reguserId)
                .eq(GoodsSyncDO::getStatus, 0));
        if(one == null){
            return Result.fail();
        }
        int count = goodsService.count(new LambdaQueryWrapper<GoodsDO>()
                .eq(GoodsDO::getReguserId, reguserId));
        Integer allCount = goodsBizService.getAllCount(reguserId);
        HashMap<String, Integer> res = new HashMap<>();
        res.put("count",count);
        res.put("allCount",allCount);
        return Result.ok(res);
    }

    /**
     * 获取用户商品列表
     * @param req
     *  -limit
     *  -page
     *  -costTemplateId 运费模板id
     *  -isPreSale 1预售 0非预售
     * @return
     */
    @CheckToken
    @GetMapping("/listByPage")
    public Result<?> listByPage(@RequestParam Map<String,Object> req){
        req.put("reguserId",UserUtils.getReguserId());
        PageUtils pageUtils = goodsService.queryPage(req);
        Map<String, Integer> stringLongMap = goodsService.countByStatus(req);
        HashMap<String, Object> data = new HashMap<>();
        data.put("page",pageUtils);
        data.put("title",stringLongMap);
        return Result.ok(data);
    }

    /**
     * 删除下架商品接口
     * @return
     */
    @CheckToken
    @PostMapping("/remove")
    public Result<?> remove(@RequestBody @NotNull GoodsSelectUpdateDTO dto){
        dto.setReguserId(UserUtils.getReguserId());
        List<Long> ids = dto.getIds();
        ids.add(-1L);
        if(ids == null || ids.size() <1){
            throw new MyException("未选择商品");
        }
        if(ids.size() >= 50){
            throw new MyException("选择商品数量请小于50");
        }
        boolean remove = goodsBizService.remove(ids, dto.getReguserId());
        if(remove){
            goodsService.remove(new LambdaQueryWrapper<GoodsDO>()
                .in(GoodsDO::getGoodsId,ids)
                .eq(GoodsDO::getReguserId,dto.getReguserId()));
            return Result.ok("操作成功");
        }
        return Result.ok("操作失败");
    }

    /**
     * 下架商品
     * @return
     */
    @CheckToken
    @PostMapping("/unline")
    public Result<?> unline(@RequestBody @NotNull GoodsSelectUpdateDTO dto){
        dto.setReguserId(UserUtils.getReguserId());
        List<Long> ids = dto.getIds();
        if(ids == null || ids.size() <1){
            throw new MyException("未选择商品");
        }
        if(ids.size() >= 20){
            throw new MyException("选择商品数量请小于20");
        }
        HashMap<String,Object> unline = goodsBizService.unline(ids, dto.getReguserId(), dto.getIsOnsale() == null ? 0 : dto.getIsOnsale());
        if(unline == null){
            throw new MyException("用户认证失败");
        }
        List<String> res = (List<String>) unline.get("res");
        HashSet<Long> set = (HashSet<Long>) unline.get("set");
        HashSet<Long> longs = new HashSet<>(ids);
        longs.removeAll(set);
        ArrayList<Long> newIds = new ArrayList<>(longs);
        if(newIds != null && newIds.size() > 0){
            asyncTask.doTaskForOneGoodsSync(newIds);
        }
        if(res.size() == 0){
            return Result.ok();
        }
        return Result.ok(res);
    }

    /**
     * 上传图片接口 没测试前台也没用到 就这样吧
     * @param request
     * @return
     */
    @CheckToken
    @PostMapping("/imgUpload")
    public Result<?> imgUpload(@RequestBody PddGoodsImageUploadRequest request){
        Integer reguserId = UserUtils.getReguserId();
        System.out.println(request);

        PddGoodsImageUploadResponse pddGoodsImageUploadResponse = goodsBizService.imgUpload(request, reguserId);
        if(pddGoodsImageUploadResponse == null){
            throw new MyException(EnumErrorCode.apiAuthorizationFailed.getCodeStr());
        }
        return new Result(pddGoodsImageUploadResponse);
    }

    /**
     * 返回 id https://mms.pinduoduo.com/goods/goods_add/index?id={这里组装id}&type=edit
     * TODO 该方式返回的id组装链接可以跳转到一个上次修改节点的编辑页  需要用户手动点击网页上方删除草稿进行新的编辑操作
     * @param goodsId
     * @return
     */
    @CheckToken
    @GetMapping("/commitId")
    public Result<?> getGoodsCommitId(@RequestParam("goodsId") Long goodsId){
        Integer reguserId = UserUtils.getReguserId();
        return new Result<>(goodsBizService.getGoodsCommitId(goodsId,reguserId));
    }
    @CheckToken
    @GetMapping("/getgoodsById")
    public  Result<?> getGoodsByID(@RequestParam("goodsId") Long goodsId){
        Integer reguserId = UserUtils.getReguserId();
        return new Result<>(goodsService.getGoodsByID(reguserId,goodsId));
    }

    /**
     * 上传图片接口 没测试前台也没用到 就这样吧
     * @param
     * @return
     */
    @CheckToken
    @PostMapping("/imgUpload2")
    public Result<?> imgUpload2(@RequestBody String image){

        Integer reguserId = UserUtils.getReguserId();
        System.out.println(image);
        System.out.println(reguserId);

//        PddGoodsImageUploadRequest request = new PddGoodsImageUploadRequest();
//        request.setImage("str");
        PddGoodsImageUploadResponse pddGoodsImageUploadResponse=goodsService.imgUpload2(reguserId,image);
        String ss="aa";
        return  Result.ok(pddGoodsImageUploadResponse);
    }


    /**
     * 单个商品编辑
     * @param req
     * @return
     */
    @CheckToken
    @PostMapping("/update")
    public Result<?> update(@RequestBody Map<String,Object> req){
        req.put("reguserId",UserUtils.getReguserId());
        Map<String, Object> stringLongMap = goodsService.goodsUpdate(req);
        return Result.ok(stringLongMap);
    }

    /**
     * 单个商品不编辑
     * @param
     * @return
     */
    @CheckToken
    @GetMapping("/goodsCommitDetailGet")
    public Result<?> goodsCommitDetailGet(@RequestParam  Long goodsCommitId){
        Integer reguserId = UserUtils.getReguserId();
        System.out.println(goodsCommitId);
        Map<String, Object> stringLongMap = goodsService.goodsCommitDetailGet(reguserId,goodsCommitId);
        return Result.ok(stringLongMap);
    }




}
