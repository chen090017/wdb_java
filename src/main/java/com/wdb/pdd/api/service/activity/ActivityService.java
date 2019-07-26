package com.wdb.pdd.api.service.activity;

import com.wdb.pdd.api.pojo.entity.ActivityLog;

import java.util.List;
import java.util.Map;

public interface ActivityService {

     List<ActivityLog> getList();
//     int add(  int activityId , int uid,  int helper,  String result);
     int add( ActivityLog activityLog);

     String sum(int activityId,int uid);// 已经砍的价格的总额

     int singleTotal( int activityId, int uid,int helper);//单个人砍价的次数

     Map<String,Object> getList(Map<String,Object>  params);
     Map<String,Object> getTop10List(Map<String,Object>  params);

     int singleCount(int activityId,int helper);//同一个人砍价的次数

     //我自己的砍价活动
     ActivityLog myactivity( int activityId,int uid,int helper);

     int deleteActivityLog( int activityId,int uid);
}
