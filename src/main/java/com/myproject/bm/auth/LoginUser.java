package com.myproject.bm.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录用户信息(放在内存 token 中)
 */
@Data
@AllArgsConstructor
public class LoginUser {

    private Integer userId;

    private String username;

    /** ADMIN-管理员 READER-读者 */
    private String role;

    /** 姓名 */
    private String name;

    /** 读者编号(读者角色时有值) */
    private Integer readerId;

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
