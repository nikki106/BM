package com.myproject.bm.auth;

import com.myproject.bm.common.ApiException;

/**
 * 当前请求的登录用户上下文(由 AuthInterceptor 填充)
 */
public class AuthContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static void clear() {
        HOLDER.remove();
    }

    /** 获取当前登录用户, 未登录抛 401 */
    public static LoginUser current() {
        LoginUser user = HOLDER.get();
        if (user == null) {
            throw new ApiException(401, "登录已过期，请重新登录");
        }
        return user;
    }

    /** 校验当前用户是管理员, 否则抛 403 */
    public static void requireAdmin() {
        LoginUser user = current();
        if (!user.isAdmin()) {
            throw new ApiException(403, "没有权限执行此操作");
        }
    }
}
