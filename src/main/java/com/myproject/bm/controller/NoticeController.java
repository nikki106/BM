package com.myproject.bm.controller;

import com.myproject.bm.auth.AuthContext;
import com.myproject.bm.common.PageResult;
import com.myproject.bm.common.Result;
import com.myproject.bm.entity.Notice;
import com.myproject.bm.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/page")
    public Result<PageResult<Notice>> page(@RequestParam(defaultValue = "1") int pageNum,
                                           @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(noticeService.page(pageNum, pageSize));
    }

    @PostMapping
    public Result<Notice> add(@RequestBody Notice notice) {
        AuthContext.requireAdmin();
        return Result.ok(noticeService.save(notice));
    }

    @PutMapping
    public Result<Notice> update(@RequestBody Notice notice) {
        AuthContext.requireAdmin();
        return Result.ok(noticeService.save(notice));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable int id) {
        AuthContext.requireAdmin();
        noticeService.delete(id);
        return Result.ok(true);
    }
}
