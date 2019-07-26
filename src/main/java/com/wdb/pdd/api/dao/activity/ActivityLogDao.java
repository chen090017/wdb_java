package com.wdb.pdd.api.dao.activity;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wdb.pdd.api.pojo.entity.ActivityLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ActivityLogDao  {
    List<ActivityLog> getList(@Param("activityId") int activityId,@Param("uid") int uid);
//    int add(@Param("activityId") int activityId,@Param("uid") int uid,@Param("helper") int helper,@Param("result") String result);

    int add(@Param("activityLog") ActivityLog activityLog);

    String sum(@Param("activityId")int activityId,@Param("uid")int uid);// 已经砍的价格的总额

    int singleTotal(@Param("activityId")int activityId,@Param("uid")int uid,@Param("helper")int helper);//单个人砍价的次数

   //取最新的前10条数据
   List<ActivityLog> getTop10List(@Param("activityId") int activityId);

    //取砍价最高的的前10条数据
    List<ActivityLog> getTop10List2(@Param("activityId") int activityId);

    int singleCount(@Param("activityId")int activityId,@Param("helper")int helper);//同一个人砍价的次数
   //我自己的砍价活动
   ActivityLog myactivity(@Param("activityId")int activityId,@Param("uid")int uid,@Param("helper")int helper);

   int deleteActivityLog(@Param("activityId")int activityId,@Param("uid")int uid);


}



