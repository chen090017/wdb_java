package com.wdb.pdd.api.controller.cats;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wdb.pdd.api.pojo.entity.GoodsCatsDO;
import com.wdb.pdd.api.pojo.entity.GoodsDO;
import com.wdb.pdd.api.pojo.vo.GoodsCatsVO;
import com.wdb.pdd.api.service.cats.IGoodsCatsBizService;
import com.wdb.pdd.api.service.cats.IGoodsCatsService;
import com.wdb.pdd.api.service.goods.IGoodsService;
import com.wdb.pdd.common.annotation.CheckToken;
import com.wdb.pdd.common.task.AsyncTask;
import com.wdb.pdd.common.utils.Result;
import com.wdb.pdd.common.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/9 0009
 * @描述
 */
@RestController
@RequestMapping("/api/goodsCats")
public class GoodsCatsController {
    @Autowired
    private AsyncTask asyncTask;
    @Autowired
    private IGoodsCatsBizService goodsCatsBizService;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private IGoodsCatsService goodsCatsService;
    /**
     * 同步商品类目
     *
     * @return
     */
    @GetMapping("/sync")
    public Result<?> syncCats(@RequestParam("vid") String vid) throws Exception{
        if(!StrUtil.equals("3074cca3-9250-4931-875c-c4a020f2ee33",vid)){
            return Result.fail();
        }
        asyncTask.doTaskForSyncCats();
        return Result.ok("后台线程执行中");
    }

    /**
     * 获取下一级的商家可用的类目
     * @param catId
     * @return
     */
    @CheckToken
    @GetMapping("/sub")
    public Result<?> getSub(@RequestParam("catId") Long catId){
        Integer reguserId = UserUtils.getReguserId();
        GoodsCatsVO sub = goodsCatsBizService.getSub(reguserId, catId);
        return Result.ok(sub);
    }

    /**
     * 获取该类目的所有上级类目
     * @param catId
     * @return
     */
    @CheckToken
    @GetMapping("/parent")
    public Result<?> getParent(@RequestParam("catId") Long catId){
        List<GoodsCatsDO> parent = goodsCatsBizService.getParent(catId);
        return Result.ok(parent);
    }

    /**
     * 根据当前商品列表获取子类目
     * @param catId
     * @return
     */
    @CheckToken
    @GetMapping("/subByGoods")
    public Result<?> getSubByGoodsAndParent(@RequestParam("catId") Long catId){
        Integer reguserId = UserUtils.getReguserId();
        List<GoodsDO> list = goodsService.list(new LambdaQueryWrapper<GoodsDO>().eq(GoodsDO::getReguserId, reguserId));
        HashSet<Long> level = new HashSet<>();
        if(list != null && list.size() > 0){
            HashSet<Long> catIds = new HashSet<>();
            for (GoodsDO aDo : list){
                Long catId1 = aDo.getCatId();
                catIds.add(catId1);
            }
            //这里将所有类目放到指定hashset中防重
            /*catIds.forEach(catId1 -> {
                List<GoodsCatsDO> parent = goodsCatsBizService.getParent(catId1);
                if(parent == null){
                    return;
                }
                if(parent.size() > 0){
                    level.add(parent.get(0).getCatId());
                }
                if(parent.size() > 1){
                    level.add(parent.get(1).getCatId());
                }
                if(parent.size() > 2){
                    level.add(parent.get(2).getCatId());
                }
                if(parent.size() > 3){
                    level.add(parent.get(3).getCatId());
                }
            });*/
            //查询所有叶子节点 朝上继续查询父节点
            List<GoodsCatsDO> level4List = goodsCatsService.list(new LambdaQueryWrapper<GoodsCatsDO>()
                    .in(GoodsCatsDO::getCatId, catIds));
            level.addAll(catIds);
            HashSet<Long> level3 = new HashSet<>();
            if(level4List != null){
                for(GoodsCatsDO catsDO : level4List){
                    Long parentCatId = catsDO.getParentCatId();
                    level.add(parentCatId);
                    level3.add(parentCatId);
                }
            }
            //查询所有父一级 叶子节点 朝上继续查询父节点
            List<GoodsCatsDO> level3List = goodsCatsService.list(new LambdaQueryWrapper<GoodsCatsDO>()
                    .in(GoodsCatsDO::getCatId, level3));
            HashSet<Long> level2 = new HashSet<>();
            if(level3List != null){
                for(GoodsCatsDO catsDO : level3List){
                    Long parentCatId = catsDO.getParentCatId();
                    level.add(parentCatId);
                    level2.add(parentCatId);
                }
            }
            //查询所有二级叶子节点 朝上继续查询父节点
            List<GoodsCatsDO> level2List = goodsCatsService.list(new LambdaQueryWrapper<GoodsCatsDO>()
                    .in(GoodsCatsDO::getCatId, level2));
            if(level2List != null){
                for(GoodsCatsDO catsDO : level2List){
                    Long parentCatId = catsDO.getParentCatId();
                    level.add(parentCatId);
                }
            }
        }
        List<GoodsCatsDO> res = new ArrayList<>();
        if(level.size() > 0){
            res = goodsCatsService.list(new LambdaQueryWrapper<GoodsCatsDO>()
                    .eq(GoodsCatsDO::getParentCatId, catId)
                    .in(GoodsCatsDO::getCatId, level));
        }
        return Result.ok(res);
    }
}
