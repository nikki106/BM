package com.myproject.bm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myproject.bm.common.ApiException;
import com.myproject.bm.common.PageResult;
import com.myproject.bm.entity.Book;
import com.myproject.bm.entity.Borrow;
import com.myproject.bm.entity.Category;
import com.myproject.bm.mapper.BookMapper;
import com.myproject.bm.mapper.BorrowMapper;
import com.myproject.bm.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookMapper bookMapper;
    private final CategoryMapper categoryMapper;
    private final BorrowMapper borrowMapper;

    public PageResult<Book> page(int pageNum, int pageSize, String keyword, Integer categoryId) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        String kw = keyword == null ? "" : keyword.trim();
        if (!kw.isEmpty()) {
            wrapper.and(w -> w.like(Book::getTitle, kw)
                    .or().like(Book::getAuthor, kw)
                    .or().like(Book::getIsbn, kw));
        }
        wrapper.eq(categoryId != null, Book::getCategoryId, categoryId);
        wrapper.orderByDesc(Book::getBookId);

        Page<Book> page = bookMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        fillCategoryName(page.getRecords());
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    public List<Book> all() {
        List<Book> books = bookMapper.selectList(
                new LambdaQueryWrapper<Book>().orderByDesc(Book::getBookId));
        fillCategoryName(books);
        return books;
    }

    public Book save(Book book) {
        if (book.getTitle() == null || book.getTitle().isBlank()) {
            throw new ApiException("书名不能为空");
        }
        if (book.getIsbn() == null || book.getIsbn().isBlank()) {
            throw new ApiException("ISBN 不能为空");
        }
        book.setTitle(book.getTitle().trim());
        book.setIsbn(book.getIsbn().trim());

        Long exists = bookMapper.selectCount(new LambdaQueryWrapper<Book>()
                .eq(Book::getIsbn, book.getIsbn())
                .ne(book.getBookId() != null, Book::getBookId, book.getBookId()));
        if (exists > 0) {
            throw new ApiException("该 ISBN 已存在");
        }

        if (book.getBookId() == null) {
            bookMapper.insert(book);
        } else if (bookMapper.updateById(book) == 0) {
            throw new ApiException("图书不存在");
        }
        // 重新查询, 返回带 createTime 的完整数据
        return bookMapper.selectById(book.getBookId());
    }

    public void delete(int bookId) {
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new ApiException("图书不存在");
        }
        Long active = borrowMapper.selectCount(new LambdaQueryWrapper<Borrow>()
                .eq(Borrow::getBookId, bookId)
                .isNull(Borrow::getReturnTime));
        if (active > 0) {
            throw new ApiException("该图书还有未归还的借阅记录，不能删除");
        }
        bookMapper.deleteById(bookId);
    }

    private void fillCategoryName(List<Book> books) {
        if (books == null || books.isEmpty()) {
            return;
        }
        List<Integer> categoryIds = books.stream()
                .map(Book::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (categoryIds.isEmpty()) {
            return;
        }
        Map<Integer, String> names = categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getCategoryId, Category::getCategoryName));
        books.forEach(b -> {
            if (b.getCategoryId() != null) {
                b.setCategoryName(names.getOrDefault(b.getCategoryId(), "未分类"));
            }
        });
    }
}
