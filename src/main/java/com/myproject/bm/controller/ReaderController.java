package com.myproject.bm.controller;

import com.myproject.bm.auth.AuthContext;
import com.myproject.bm.common.PageResult;
import com.myproject.bm.common.Result;
import com.myproject.bm.entity.Reader;
import com.myproject.bm.service.ReaderService;
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

import java.util.List;

@RestController
@RequestMapping("/api/reader")
@RequiredArgsConstructor
public class ReaderController {

    private final ReaderService readerService;

    @GetMapping("/page")
    public Result<PageResult<Reader>> page(@RequestParam(defaultValue = "1") int pageNum,
                                           @RequestParam(defaultValue = "10") int pageSize,
                                           @RequestParam(required = false) String keyword) {
        return Result.ok(readerService.page(pageNum, pageSize, keyword));
    }

    @GetMapping("/all")
    public Result<List<Reader>> all() {
        return Result.ok(readerService.all());
    }

    @PostMapping
    public Result<Reader> add(@RequestBody Reader reader) {
        AuthContext.requireAdmin();
        return Result.ok(readerService.save(reader));
    }

    @PutMapping
    public Result<Reader> update(@RequestBody Reader reader) {
        AuthContext.requireAdmin();
        return Result.ok(readerService.save(reader));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable int id) {
        AuthContext.requireAdmin();
        readerService.delete(id);
        return Result.ok(true);
    }
}
