package com.wdb.pdd.api.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@TableName("wdb_admin_action")
@Data
@AllArgsConstructor
@NoArgsConstructor
 public class AdminAction  {


    @TableId(type = IdType.INPUT)
    private int id;
    private Integer reguserId;
    private Integer uid; //'角色id
    private Integer accountLevelId;//'账户等级'
    private String username;//用户名(邮箱)
    private String password;//'密码'
    private Integer isEnable;//1:启用0：禁用
    private String salt;
}
