package com.wdb.pdd.api.service.activity.impl;

import com.wdb.pdd.api.dao.activity.ActivityDao;
import com.wdb.pdd.api.dao.activity.ActivityLogDao;
import com.wdb.pdd.api.dao.product.ProductDao;
import com.wdb.pdd.api.dao.sys.ReguserDao;
import com.wdb.pdd.api.pojo.entity.Activity;
import com.wdb.pdd.api.pojo.entity.ActivityLog;
import com.wdb.pdd.api.pojo.entity.Product;
import com.wdb.pdd.api.pojo.entity.ReguserDO;
import com.wdb.pdd.api.service.activity.ActivityService;
import com.wdb.pdd.common.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Service
public class ActivityServiceImpl implements ActivityService {
    @Autowired
    ActivityLogDao ActivityLogDao;
    @Autowired
    ReguserDao reguserDao;
    @Autowired
    ActivityDao activityDao;
    @Autowired
    ProductDao productDao;

    @Override
    public List<ActivityLog> getList() {

        return null;
    }

    @Override
    public int add(ActivityLog activityLog) {
//      int count=ActivityLogDao.add(activityId,uid,helper,result);
        int count = ActivityLogDao.add(activityLog);
        return count;
    }

    @Override
    public String sum(int activityId, int uid) {
        String sum = ActivityLogDao.sum(activityId, uid);
        return sum;
    }

    @Override
    public int singleTotal(int activityId, int uid, int helper) {
        int count = ActivityLogDao.singleTotal(activityId, uid, helper);
        return count;
    }

    @Override
    public Map<String, Object> getList(Map<String, Object> params) {

        HashMap<String, Object> data = new HashMap<>();

        int activityId = Integer.parseInt(params.get("activityId").toString());
        int uid = Integer.parseInt(params.get("uid").toString());
        Activity activity=activityDao.selectById(activityId);
        ActivityLog  myactivityLog=ActivityLogDao.myactivity(activityId,uid,uid);

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat myformatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date myactivityDate = new Date();
        Date activityData=new Date();
        try {
            if(myactivityLog!=null){
                myactivityDate = myformatter.parse(myactivityLog.getCreateTime());
            }else{
                myactivityDate=null;
            }

            activityData=myformatter.parse(activity.getEndTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        cal.setTime(myactivityDate);//设置起时间
        cal.add(Calendar.DATE, 1);//增加一天
        String myendTime="";
   if(cal.getTime().before(activityData)){
        myendTime=myformatter.format(cal.getTime());

   }else{
        myendTime=activity.getEndTime();
   }

        // 判断是否可以重新发起砍价
        Boolean isAgainKanjia=false;
        if(myactivityLog!=null&&cal.getTime().before(new Date()) ){
            isAgainKanjia=true;
        }




        System.out.println("结束时间"+myendTime);

        List<ActivityLog> activityLoglist = ActivityLogDao.getList(activityId, uid);
        System.out.println(params.get("goodsCommitId"));

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = null;
        ReguserDO u_reguser = reguserDao.selectById(uid);
        for (ActivityLog activityLog : activityLoglist) {
            map = new HashMap<>();
            ReguserDO h_reguser = reguserDao.selectById(activityLog.getHelper());
            map.put("id", h_reguser.getId());
            map.put("name", h_reguser.getMallName());
            map.put("log", h_reguser.getLogo());
            map.put("mes", activityLog.getUid() == activityLog.getHelper() ? "自己砍掉" : "帮好友砍掉");
            map.put("result", activityLog.getResult());
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Date createTime = new Date();
            try {
                createTime = formatter.parse(activityLog.getCreateTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            map.put("time3", activityLog.getCreateTime());
            map.put("time2", convertTimeToFormat(createTime.getTime()/1000));
             list.add(map);
        }


        String sum = ActivityLogDao.sum(activityId, uid);//已经砍的价格
        if (sum == null) {
            sum = "0.00";
        }

        BigDecimal result = new BigDecimal(sum);
        Product  product=productDao.selectById(6);
        BigDecimal totalprice = new BigDecimal(product.getPrice());
        BigDecimal leftReduceTotal = totalprice.subtract(result).setScale(2, BigDecimal.ROUND_HALF_EVEN);
        data.put("price", leftReduceTotal);
        data.put("totalprice", totalprice);
        data.put("name",UserUtils.getReguserId()==uid?"我的":u_reguser.getMallName());
        data.put("result", result.setScale(2, BigDecimal.ROUND_HALF_EVEN));
        data.put("list", list);
        data.put("activity",activity);
        data.put("myendTime",myendTime);
        data.put("isAgainKanjia",isAgainKanjia);

        return data;
    }

    @Override
    public Map<String,Object> getTop10List(Map<String, Object> params) {
      HashMap<String, Object> data = new HashMap<>();

        int activityId = Integer.parseInt(params.get("activityId").toString());
        Integer reguserId= UserUtils.getReguserId();
        int   singleCount=ActivityLogDao.singleTotal(activityId, reguserId, reguserId);


        Activity activity=activityDao.selectById(activityId);
        List<ActivityLog> activityLoglist = ActivityLogDao.getTop10List(activityId);
        List<ActivityLog> activityLoglist2 = ActivityLogDao.getTop10List2(activityId);
        System.out.println(params.get("goodsCommitId"));
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> list2 = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = null;
        for (ActivityLog activityLog : activityLoglist) {
            map = new HashMap<>();
            ReguserDO h_reguser = reguserDao.selectById(activityLog.getHelper());
            map.put("id", h_reguser.getId());
            map.put("name", h_reguser.getMallName());
            map.put("log", h_reguser.getLogo());
            map.put("mes", activityLog.getUid() == activityLog.getHelper() ? "自己砍掉" : "帮好友砍掉");
            map.put("result", activityLog.getResult());
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date createTime = new Date();
            try {
                createTime = formatter.parse(activityLog.getCreateTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
             map.put("time", convertTimeToFormat(createTime.getTime()/1000));
            list.add(map);
        }

        for (ActivityLog activityLog : activityLoglist2) {
            map = new HashMap<>();
            ReguserDO h_reguser = reguserDao.selectById(activityLog.getHelper());
             map.put("id", h_reguser.getId());
            map.put("name", h_reguser.getMallName());
            map.put("log", h_reguser.getLogo());
            map.put("mes", activityLog.getUid() == activityLog.getHelper() ? "自己砍掉" : "帮好友砍掉");
            map.put("result", activityLog.getResult());
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date createTime = new Date();
            try {
                createTime = formatter.parse(activityLog.getCreateTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
             map.put("time", convertTimeToFormat(createTime.getTime()/1000));
            list2.add(map);
        }

        data.put("listB", list2);
       data.put("listA", list);
        data.put("activity",activity);
        data.put("iskan",singleCount>0);



        return data;
    }

    @Override
    public int singleCount(int activityId,int helper) {
        int count = ActivityLogDao.singleCount(activityId,helper);
        return count;
    }

    @Override
    public ActivityLog myactivity(int activityId, int uid, int helper) {
        ActivityLog  myactivityLog=ActivityLogDao.myactivity(activityId,uid,uid);
        return myactivityLog;
    }

    @Override
    public int deleteActivityLog(int activityId, int uid) {
        return ActivityLogDao.deleteActivityLog(activityId,uid);
    }


    /**
     * 将一个时间戳转换成提示性时间字符串
     *
     * @param timeStamp
     * @return
     */
    public static String convertTimeToFormat(long timeStamp) {
        long curTime =System.currentTimeMillis() / (long) 1000 ;
        long time =curTime - timeStamp;

        if (time < 60 && time >= 0) {
            return time+"秒前";
        } else if (time >= 60 && time < 3600) {
            return time / 60 + "分钟前";
        } else if (time >= 3600 && time < 3600 * 24) {
            return time / 3600 + "小时前";
        } else if (time >= 3600 * 24 && time < 3600 * 24 * 30) {
            return time / 3600 / 24 + "天前";
        } else if (time >= 3600 * 24 * 30 && time < 3600 * 24 * 30 * 12) {
            return time / 3600 / 24 / 30 + "个月前";
        } else if (time >= 3600 * 24 * 30 * 12) {
            return time / 3600 / 24 / 30 / 12 + "年前";
        } else {
            return "刚刚";
        }
    }





}
