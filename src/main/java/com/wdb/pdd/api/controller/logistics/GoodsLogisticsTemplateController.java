package com.wdb.pdd.api.controller.logistics;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wdb.pdd.api.pojo.entity.GoodsLogisticsTemplateDO;
import com.wdb.pdd.api.service.logistics.ILogisticsTemplateBizService;
import com.wdb.pdd.api.service.logistics.ILogisticsTemplateService;
import com.wdb.pdd.api.service.logistics.LServiceTemplate;
import com.wdb.pdd.common.annotation.CheckToken;
import com.wdb.pdd.common.exception.handlers.EnumErrorCode;
import com.wdb.pdd.common.utils.Result;
import com.wdb.pdd.common.utils.UserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/6 0006
 * @描述 商品运费模板控制类
 */
@RestController
@RequestMapping("/api/goodsLogisticsTemplate")
public class GoodsLogisticsTemplateController {

    @Autowired
    private ILogisticsTemplateBizService logisticsTemplateBizService;
    @Autowired
    private ILogisticsTemplateService logisticsTemplateService;
    @Autowired
    private com.wdb.pdd.api.service.logistics.LServiceTemplate LServiceTemplate;

    /**
     * 商品运费模板同步
     *
     * @return
     */
    @CheckToken
    @GetMapping("/sync")
    public Result<?> syncLogisticsTemplate() {
        Integer reguserId = UserUtils.getReguserId();
        if (reguserId == null) {
            return Result.build(EnumErrorCode.paramsFail.getCode(), EnumErrorCode.paramsFail.getMsg());
        }
        logisticsTemplateBizService.syncLogisticsTemplate(reguserId);
        List<GoodsLogisticsTemplateDO> list = logisticsTemplateService.list(new LambdaQueryWrapper<GoodsLogisticsTemplateDO>()
                .eq(GoodsLogisticsTemplateDO::getReguserId, reguserId));
        return Result.ok(list);
    }

    @CheckToken
    @GetMapping("/list")
    public Result<?> list(){
        Integer reguserId = UserUtils.getReguserId();
        if (reguserId == null) {
            return Result.build(EnumErrorCode.paramsFail.getCode(), EnumErrorCode.paramsFail.getMsg());
        }
        List<GoodsLogisticsTemplateDO> list = logisticsTemplateService.list(new LambdaQueryWrapper<GoodsLogisticsTemplateDO>()
                .eq(GoodsLogisticsTemplateDO::getReguserId, reguserId));
        return Result.ok(list);
    }

    @CheckToken
    @GetMapping("/list2")
    public Result<?> list2(){
        Integer reguserId = UserUtils.getReguserId();
        if (reguserId == null) {
            return Result.build(EnumErrorCode.paramsFail.getCode(), EnumErrorCode.paramsFail.getMsg());
        }
        List<GoodsLogisticsTemplateDO> list = LServiceTemplate.Llist(reguserId);
        return Result.ok(list);
    }

}
