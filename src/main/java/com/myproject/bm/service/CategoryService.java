package com.myproject.bm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myproject.bm.common.ApiException;
import com.myproject.bm.entity.Book;
import com.myproject.bm.entity.Category;
import com.myproject.bm.mapper.BookMapper;
import com.myproject.bm.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final BookMapper bookMapper;

    public List<Category> list() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getCategoryId));
    }

    public Category add(Category category) {
        String name = category.getCategoryName() == null ? "" : category.getCategoryName().trim();
        if (name.isEmpty()) {
            throw new ApiException("分类名称不能为空");
        }
        Long count = categoryMapper.selectCount(
                new LambdaQueryWrapper<Category>().eq(Category::getCategoryName, name));
        if (count > 0) {
            throw new ApiException("该分类已存在");
        }
        Category saved = new Category();
        saved.setCategoryName(name);
        categoryMapper.insert(saved);
        return saved;
    }

    public void delete(int categoryId) {
        Long count = bookMapper.selectCount(
                new LambdaQueryWrapper<Book>().eq(Book::getCategoryId, categoryId));
        if (count > 0) {
            throw new ApiException("该分类下还有图书，不能删除");
        }
        categoryMapper.deleteById(categoryId);
    }
}
