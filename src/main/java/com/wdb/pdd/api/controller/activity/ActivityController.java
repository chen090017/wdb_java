package com.wdb.pdd.api.controller.activity;
import com.wdb.pdd.api.pojo.entity.Activity;
import com.wdb.pdd.api.pojo.entity.ActivityLog;
import com.wdb.pdd.api.pojo.entity.Product;
import com.wdb.pdd.api.service.activity.ActivityService;
import com.wdb.pdd.api.service.activity.IActivityService;
import com.wdb.pdd.api.service.product.ProductService;
import com.wdb.pdd.api.service.sys.IReguserService;
import com.wdb.pdd.common.annotation.CheckToken;
import com.wdb.pdd.common.utils.Result;
import com.wdb.pdd.common.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    @Autowired
    private ActivityService ActivityService;
    @Autowired
    private  IReguserService iReguserService;
    @Autowired
    private IActivityService iActivityService;
    @Autowired
    private ProductService productService;



    /**
     *
     *
     * @return
     */
    @CheckToken
    @PostMapping("/activityList")
    public Result<?> getActivityList(@RequestBody Map<String, Object> req) {
        req.put("reguserId", UserUtils.getReguserId());
        Map<String, Object> activityListMap = ActivityService.getList(req);
        return Result.ok(activityListMap);
    }

    @CheckToken
    @PostMapping("/addActivity")
    public Result<?> addActivity(@RequestBody Map<String, Object> req) {
        int uid = Integer.parseInt(req.get("uid").toString());
        int helper = Integer.parseInt(req.get("helper").toString());
        HashMap<String, Object> data = new HashMap<>();
        int activityId = Integer.parseInt(req.get("activityId").toString());
//        int   singleCount=ActivityService.singleTotal(activityId, reguserId, reguserId);
        Activity activity=iActivityService.getById(activityId);
        ActivityLog  myactivityLog=ActivityService.myactivity(activityId,uid,uid);
        Date now=new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime = new Date();
        Date endTime = new Date();
        Date myactivityDate = new Date();
        Calendar cal = Calendar.getInstance();
        try {
            startTime = formatter.parse(activity.getStartTime());
            endTime = formatter.parse(activity.getEndTime());
            if(myactivityLog==null){
                myactivityDate =new Date();
            }else{
                myactivityDate = formatter.parse(myactivityLog.getCreateTime());
            }


        } catch (ParseException e) {
            e.printStackTrace();
        }
        cal.setTime(myactivityDate);//设置起时间
        cal.add(Calendar.DATE, 1);//增加一天

        Date myendTime=new Date();// 我的 结束时间
        if(cal.getTime().before(endTime)){
            myendTime=cal.getTime();

        }else{
            myendTime=endTime;
        }


        if(now.getTime()>startTime.getTime()&&now.getTime()<myendTime.getTime()){
            ActivityLog activityLog = new ActivityLog();

            activityLog.setActivityId(activityId);
            activityLog.setUid(uid);
            activityLog.setHelper(helper);

           int   singleCount=ActivityService.singleTotal(activityId, uid, helper);
            if (singleCount >= 1) {
                return Result.fail("每个用户只能砍1次..");
            }

            int singleTotal = ActivityService.singleCount(activityId,helper);
            if (singleTotal >= 5) {
                return Result.fail("每个用户总共只能砍5次..");
            }
            Random random = new Random();
            String sum = ActivityService.sum(activityId, uid);//已经砍的价格
            if (sum == null) {
                sum = "0.00";
            }
            BigDecimal result = new BigDecimal(sum);
         Product product=productService.getById(6);

//        BigDecimal result=bgsum.add(firstNReduceTotal);// 已经砍的价格  和 新砍的价格 生成的 结果
//            BigDecimal totalReduce = new BigDecimal(req.get("totalprice").toString());
            BigDecimal totalReduce = new BigDecimal(product.getPrice());
            BigDecimal leftReduceTotal = totalReduce.subtract(result).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            double max = 0.00;
            if (leftReduceTotal.doubleValue() > 60.00) {
                max = 60.00;
            } else {
                max = leftReduceTotal.doubleValue();
            }
            double min = 0.00;
            double randNumber = random.nextDouble() * (max - min) + min;
            BigDecimal firstNReduceTotal = BigDecimal.valueOf(randNumber).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            activityLog.setResult(firstNReduceTotal + "");
            int count = 0;
            Boolean isok = false;
            if (leftReduceTotal.doubleValue() > 0.00) {
                count = ActivityService.add(activityLog);
                isok = true;
            }
            data.put("isok", isok);
            data.put("max", max);
            data.put("sum", sum);
            data.put("sum2", result.setScale(2, BigDecimal.ROUND_HALF_EVEN));
            data.put("left", leftReduceTotal);
            data.put("result", firstNReduceTotal);// 单次砍掉金额
            data.put("count", singleTotal);// 已砍次数
            data.put("title", count);
            data.put("activity",activity);
            data.put("lasttime",myendTime.getTime()-startTime.getTime());
             data.put("status","活动还没结束");

        }else if(now.getTime()<startTime.getTime()){

            return Result.fail("砍价活动还没开始咯~");
        }else if(now.getTime()>myendTime.getTime()){
            return Result.fail("砍价活动已经结束咯~");
        }

        return Result.ok(data);
    }

    /**
     * 获取最新的前10天数据
     *
     * @return
     */
    @CheckToken
    @PostMapping("/getTop10List")
    public Result<?> getTop10List(@RequestBody Map<String, Object> req) {
        Map<String, Object> activityListMap = ActivityService.getTop10List(req);
        return Result.ok(activityListMap);
    }



    @CheckToken
    @PostMapping("/AgainKj")
    public Result<?> AgainKj(@RequestBody Map<String, Object> req) {
        int uid = Integer.parseInt(req.get("uid").toString());
        int helper = Integer.parseInt(req.get("helper").toString());
        HashMap<String, Object> data = new HashMap<>();
        int activityId = Integer.parseInt(req.get("activityId").toString());
       int result=ActivityService.deleteActivityLog(activityId,uid);

        Activity activity=iActivityService.getById(activityId);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime = new Date();
        Date endTime = new Date();
        try {
            startTime = formatter.parse(activity.getStartTime());
            endTime = formatter.parse(activity.getEndTime());
         } catch (ParseException e) {
            e.printStackTrace();
        }

        if(new Date().getTime()>startTime.getTime()&&new Date().getTime()<endTime.getTime()){

            ActivityLog activityLog = new ActivityLog();

            activityLog.setActivityId(activityId);
            activityLog.setUid(uid);
            activityLog.setHelper(helper);
            Product product=productService.getById(6);
//            BigDecimal totalReduce = new BigDecimal(req.get("totalprice").toString());
            BigDecimal totalReduce = new BigDecimal(product.getPrice());
            double max = 60.00;
            double min = 0.00;
            Random random = new Random();
            double randNumber = random.nextDouble() * (max - min) + min;
            BigDecimal firstNReduceTotal = BigDecimal.valueOf(randNumber).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            activityLog.setResult(firstNReduceTotal + "");

            Boolean isok = true;
            data.put("isok", isok);
            data.put("status","活动还没结束");
            data.put("result", firstNReduceTotal);// 单次砍掉金额

            ActivityService.add(activityLog);


        }else if(new Date().getTime()<startTime.getTime()){

            return Result.fail("砍价活动还没开始咯~");
        }else if(new Date().getTime()>endTime.getTime()){
            return Result.fail("砍价活动已经结束咯~");
        }

        return Result.ok(data);

    }






}
