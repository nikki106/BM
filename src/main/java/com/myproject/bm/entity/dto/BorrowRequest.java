package com.myproject.bm.entity.dto;

import lombok.Data;

/**
 * 办理借书请求体
 */
@Data
public class BorrowRequest {

    private Integer bookId;

    private Integer readerId;

    /** 借期天数, 默认 30 */
    private Integer days;
}
