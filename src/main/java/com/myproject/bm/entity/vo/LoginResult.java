package com.myproject.bm.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录返回: token、角色、姓名、读者编号(读者登录时返回)
 */
@Data
@AllArgsConstructor
public class LoginResult {

    private String token;

    private String username;

    private String role;

    private String name;

    private Integer readerId;
}
