package com.wdb.pdd.api.pojo.entity;


import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("wdb_activity")
public class Activity implements Serializable {
    private static final long serialVersionUID = 6004737720187436352L;

    @TableId(type = IdType.INPUT)
    private int id;
    private String name;
    private String description;
    private String startTime;
    private String endTime;
    private String thumb;
    private String banner;
    private String type;
    private String createTime;
    private String viewCount;

}
