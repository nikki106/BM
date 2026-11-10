package com.myproject.bm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 图书表
 */
@Data
@TableName("book")
public class Book {

    @TableId(type = IdType.AUTO)
    private Integer bookId;

    private String title;

    private String author;

    private String publisher;

    private String isbn;

    private Integer categoryId;

    private Integer stock;

    private BigDecimal price;

    private LocalDateTime createTime;

    /** 联表字段: 分类名称(列表展示用) */
    @TableField(exist = false)
    private String categoryName;
}
