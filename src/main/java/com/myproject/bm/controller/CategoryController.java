package com.myproject.bm.controller;

import com.myproject.bm.auth.AuthContext;
import com.myproject.bm.common.Result;
import com.myproject.bm.entity.Category;
import com.myproject.bm.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/list")
    public Result<List<Category>> list() {
        return Result.ok(categoryService.list());
    }

    @PostMapping
    public Result<Category> add(@RequestBody Category category) {
        AuthContext.requireAdmin();
        return Result.ok(categoryService.add(category));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable int id) {
        AuthContext.requireAdmin();
        categoryService.delete(id);
        return Result.ok(true);
    }
}
