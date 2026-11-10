package com.myproject.bm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myproject.bm.auth.LoginUser;
import com.myproject.bm.auth.TokenStore;
import com.myproject.bm.common.ApiException;
import com.myproject.bm.entity.Admin;
import com.myproject.bm.entity.Reader;
import com.myproject.bm.entity.User;
import com.myproject.bm.entity.vo.LoginResult;
import com.myproject.bm.mapper.AdminMapper;
import com.myproject.bm.mapper.ReaderMapper;
import com.myproject.bm.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final AdminMapper adminMapper;
    private final ReaderMapper readerMapper;
    private final TokenStore tokenStore;

    public LoginResult login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new ApiException("请输入用户名和密码");
        }

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username.trim()));
        if (user == null || !md5(password).equals(user.getPassword())) {
            throw new ApiException("用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new ApiException("账号已被禁用，请联系管理员");
        }

        LoginUser loginUser = new LoginUser(user.getUserId(), user.getUsername(), user.getRole(),
                user.getUsername(), null);
        if ("ADMIN".equals(user.getRole())) {
            Admin admin = adminMapper.selectOne(
                    new LambdaQueryWrapper<Admin>().eq(Admin::getUserId, user.getUserId()));
            if (admin != null) {
                loginUser.setName(admin.getName());
            }
        } else {
            Reader reader = readerMapper.selectOne(
                    new LambdaQueryWrapper<Reader>().eq(Reader::getUserId, user.getUserId()));
            if (reader != null) {
                loginUser.setName(reader.getName());
                loginUser.setReaderId(reader.getReaderId());
            }
        }

        String token = tokenStore.issue(loginUser);
        return new LoginResult(token, user.getUsername(), user.getRole(),
                loginUser.getName(), loginUser.getReaderId());
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
