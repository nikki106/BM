package com.myproject.bm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 借阅记录表
 * 状态: 0-借阅中 1-已归还 2-已逾期(借阅中且超过应还时间, 查询时动态计算)
 */
@Data
@TableName("borrow")
public class Borrow {

    @TableId(type = IdType.AUTO)
    private Integer borrowId;

    private Integer bookId;

    private Integer readerId;

    private LocalDateTime borrowTime;

    private LocalDateTime dueTime;

    private LocalDateTime returnTime;

    /** 0-借阅中 1-已归还 */
    private Integer status;

    /** 联表字段 */
    @TableField(exist = false)
    private String bookTitle;

    @TableField(exist = false)
    private String readerName;

    @TableField(exist = false)
    private String cardNo;
}
