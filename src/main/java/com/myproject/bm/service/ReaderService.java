package com.myproject.bm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myproject.bm.common.ApiException;
import com.myproject.bm.common.PageResult;
import com.myproject.bm.entity.Borrow;
import com.myproject.bm.entity.Reader;
import com.myproject.bm.mapper.BorrowMapper;
import com.myproject.bm.mapper.ReaderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReaderService {

    private final ReaderMapper readerMapper;
    private final BorrowMapper borrowMapper;

    public PageResult<Reader> page(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<Reader> wrapper = new LambdaQueryWrapper<>();
        String kw = keyword == null ? "" : keyword.trim();
        if (!kw.isEmpty()) {
            wrapper.and(w -> w.like(Reader::getName, kw)
                    .or().like(Reader::getCardNo, kw)
                    .or().like(Reader::getPhone, kw));
        }
        wrapper.orderByDesc(Reader::getReaderId);

        Page<Reader> page = readerMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        fillBorrowing(page.getRecords());
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    public List<Reader> all() {
        List<Reader> readers = readerMapper.selectList(
                new LambdaQueryWrapper<Reader>().orderByDesc(Reader::getReaderId));
        fillBorrowing(readers);
        return readers;
    }

    public Reader save(Reader reader) {
        if (reader.getCardNo() == null || reader.getCardNo().isBlank()) {
            throw new ApiException("读者证号不能为空");
        }
        reader.setCardNo(reader.getCardNo().trim());
        if (reader.getName() == null) {
            reader.setName("");
        }
        if (reader.getMaxBorrow() == null) {
            reader.setMaxBorrow(5);
        }

        Long exists = readerMapper.selectCount(new LambdaQueryWrapper<Reader>()
                .eq(Reader::getCardNo, reader.getCardNo())
                .ne(reader.getReaderId() != null, Reader::getReaderId, reader.getReaderId()));
        if (exists > 0) {
            throw new ApiException("该证号已存在");
        }

        if (reader.getReaderId() == null) {
            // 新增读者不创建登录账号(user 表), 账号开通需单独处理
            reader.setViolationCount(0);
            reader.setUserId(null);
            readerMapper.insert(reader);
        } else if (readerMapper.updateById(reader) == 0) {
            throw new ApiException("读者不存在");
        }
        // 重新查询, 返回带 createTime 的完整数据
        return readerMapper.selectById(reader.getReaderId());
    }

    public void delete(int readerId) {
        Reader reader = readerMapper.selectById(readerId);
        if (reader == null) {
            throw new ApiException("读者不存在");
        }
        Long active = borrowMapper.selectCount(new LambdaQueryWrapper<Borrow>()
                .eq(Borrow::getReaderId, readerId)
                .isNull(Borrow::getReturnTime));
        if (active > 0) {
            throw new ApiException("该读者还有未归还的图书，不能删除");
        }
        readerMapper.deleteById(readerId);
    }

    private void fillBorrowing(List<Reader> readers) {
        if (readers == null || readers.isEmpty()) {
            return;
        }
        List<Integer> ids = readers.stream().map(Reader::getReaderId).toList();
        Map<Integer, Long> counts = new HashMap<>();
        for (Map<String, Object> row : borrowMapper.countActiveByReaderIds(ids)) {
            Object id = row.get("readerId");
            Object cnt = row.get("cnt");
            if (id != null && cnt != null) {
                counts.put(((Number) id).intValue(), ((Number) cnt).longValue());
            }
        }
        readers.forEach(r -> r.setBorrowing(counts.getOrDefault(r.getReaderId(), 0L).intValue()));
    }
}
