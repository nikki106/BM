package com.myproject.bm.controller;

import com.myproject.bm.auth.AuthContext;
import com.myproject.bm.common.PageResult;
import com.myproject.bm.common.Result;
import com.myproject.bm.entity.Book;
import com.myproject.bm.service.BookService;
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
@RequestMapping("/api/book")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping("/page")
    public Result<PageResult<Book>> page(@RequestParam(defaultValue = "1") int pageNum,
                                         @RequestParam(defaultValue = "10") int pageSize,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Integer categoryId) {
        return Result.ok(bookService.page(pageNum, pageSize, keyword, categoryId));
    }

    @GetMapping("/all")
    public Result<List<Book>> all() {
        return Result.ok(bookService.all());
    }

    @PostMapping
    public Result<Book> add(@RequestBody Book book) {
        AuthContext.requireAdmin();
        return Result.ok(bookService.save(book));
    }

    @PutMapping
    public Result<Book> update(@RequestBody Book book) {
        AuthContext.requireAdmin();
        return Result.ok(bookService.save(book));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable int id) {
        AuthContext.requireAdmin();
        bookService.delete(id);
        return Result.ok(true);
    }
}
