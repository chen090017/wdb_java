package com.wdb.pdd.api.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @创建人 Yeohwah
 * @创建时间 2019/6/5 0005
 * @描述 用户注册信息 AccessToken
 */

@TableName("wdb_reguser")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReguserDO implements Serializable {

    private static final long serialVersionUID = 8331231340827340919L;
    @TableId
    private Integer id;
    private String ownerId;
    private String ownerName;
    private String mallName;
    private String logo;
    private String mallDesc;
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private Long addTime;
    private String sysUserToken;
    private Long sysTokenExpiresTime;
}

