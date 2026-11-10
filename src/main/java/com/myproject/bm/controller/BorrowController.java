package com.myproject.bm.controller;

import com.myproject.bm.auth.AuthContext;
import com.myproject.bm.auth.LoginUser;
import com.myproject.bm.common.PageResult;
import com.myproject.bm.common.Result;
import com.myproject.bm.entity.Borrow;
import com.myproject.bm.entity.dto.BorrowRequest;
import com.myproject.bm.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/borrow")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    @GetMapping("/page")
    public Result<PageResult<Borrow>> page(@RequestParam(defaultValue = "1") int pageNum,
                                           @RequestParam(defaultValue = "10") int pageSize,
                                           @RequestParam(required = false) String status,
                                           @RequestParam(required = false) Integer readerId,
                                           @RequestParam(required = false) String keyword) {
        Integer statusNum = (status == null || status.isBlank()) ? null : Integer.valueOf(status);

        // 读者只能查看自己的借阅记录
        LoginUser user = AuthContext.current();
        if (!user.isAdmin()) {
            if (user.getReaderId() == null) {
                return Result.ok(new PageResult<>(List.of(), 0L));
            }
            readerId = user.getReaderId();
        }
        return Result.ok(borrowService.page(pageNum, pageSize, statusNum, readerId, keyword));
    }

    @PostMapping
    public Result<Boolean> add(@RequestBody BorrowRequest request) {
        AuthContext.requireAdmin();
        borrowService.borrow(request);
        return Result.ok(true);
    }

    @PutMapping("/return/{id}")
    public Result<Boolean> returnBook(@PathVariable int id) {
        AuthContext.requireAdmin();
        borrowService.returnBook(id);
        return Result.ok(true);
    }

    @PutMapping("/renew/{id}")
    public Result<Boolean> renew(@PathVariable int id) {
        AuthContext.requireAdmin();
        borrowService.renew(id);
        return Result.ok(true);
    }
}
