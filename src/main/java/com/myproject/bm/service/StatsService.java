package com.myproject.bm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myproject.bm.entity.Borrow;
import com.myproject.bm.mapper.BookMapper;
import com.myproject.bm.mapper.BorrowMapper;
import com.myproject.bm.mapper.ReaderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final BookMapper bookMapper;
    private final ReaderMapper readerMapper;
    private final BorrowMapper borrowMapper;

    /**
     * 首页统计: 馆藏图书总数 / 读者总数 / 借出中(未逾期) / 逾期未还
     */
    public Map<String, Object> stats() {
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("bookTotal", bookMapper.selectCount(null));
        stats.put("readerTotal", readerMapper.selectCount(null));
        stats.put("borrowing", borrowMapper.selectCount(new LambdaQueryWrapper<Borrow>()
                .isNull(Borrow::getReturnTime)
                .ge(Borrow::getDueTime, now)));
        stats.put("overdue", borrowMapper.selectCount(new LambdaQueryWrapper<Borrow>()
                .isNull(Borrow::getReturnTime)
                .lt(Borrow::getDueTime, now)));
        return stats;
    }
}
