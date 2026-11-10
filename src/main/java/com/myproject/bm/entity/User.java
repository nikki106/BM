package com.myproject.bm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表 (管理员与读者的登录账号)
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Integer userId;

    private String username;

    /** MD5 加密存储 */
    private String password;

    /** ADMIN-管理员 READER-读者 */
    private String role;

    /** 1-正常 0-禁用 */
    private Integer status;

    private LocalDateTime createTime;
}
