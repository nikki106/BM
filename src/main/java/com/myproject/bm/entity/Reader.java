package com.myproject.bm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 读者表
 */
@Data
@TableName("reader")
public class Reader {

    @TableId(type = IdType.AUTO)
    private Integer readerId;

    private String cardNo;

    private String name;

    private String phone;

    /** 可借数量上限 */
    private Integer maxBorrow;

    /** 违约次数, 达到 3 次暂停借书 */
    private Integer violationCount;

    /** 关联的登录账号, 新增读者时不创建账号 */
    private Integer userId;

    private LocalDateTime createTime;

    /** 联表字段: 当前借阅数量(下拉框/列表展示用) */
    @TableField(exist = false)
    private Integer borrowing;
}
