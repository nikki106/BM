package com.myproject.bm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.myproject.bm.common.ApiException;
import com.myproject.bm.common.PageResult;
import com.myproject.bm.entity.Book;
import com.myproject.bm.entity.Borrow;
import com.myproject.bm.entity.Reader;
import com.myproject.bm.entity.dto.BorrowRequest;
import com.myproject.bm.mapper.BookMapper;
import com.myproject.bm.mapper.BorrowMapper;
import com.myproject.bm.mapper.ReaderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowService {

    private final BorrowMapper borrowMapper;
    private final BookMapper bookMapper;
    private final ReaderMapper readerMapper;

    public PageResult<Borrow> page(int pageNum, int pageSize, Integer status, Integer readerId, String keyword) {
        int num = Math.max(1, pageNum);
        int size = Math.max(1, pageSize);
        String kw = keyword == null ? "" : keyword.trim();
        String kwParam = kw.isEmpty() ? null : kw;

        long total = borrowMapper.countBorrow(status, readerId, kwParam);
        List<Borrow> rows = borrowMapper.selectBorrowPage((num - 1) * size, size, status, readerId, kwParam);
        // 借阅中但已超过应还时间的记录, 状态动态算成 2-已逾期
        LocalDateTime now = LocalDateTime.now();
        rows.forEach(row -> {
            if (row.getStatus() != null && row.getStatus() == 0
                    && row.getDueTime() != null && row.getDueTime().isBefore(now)) {
                row.setStatus(2);
            }
        });
        return new PageResult<>(rows, total);
    }

    @Transactional
    public void borrow(BorrowRequest request) {
        Integer bookId = request.getBookId();
        Integer readerId = request.getReaderId();
        if (bookId == null || readerId == null) {
            throw new ApiException("图书或读者不存在");
        }

        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new ApiException("图书不存在");
        }
        Reader reader = readerMapper.selectById(readerId);
        if (reader == null) {
            throw new ApiException("读者不存在");
        }
        if (reader.getViolationCount() != null && reader.getViolationCount() >= 3) {
            throw new ApiException("该读者违约次数过多，暂停借书资格");
        }
        Long active = borrowMapper.selectCount(new LambdaQueryWrapper<Borrow>()
                .eq(Borrow::getReaderId, readerId)
                .isNull(Borrow::getReturnTime));
        int maxBorrow = reader.getMaxBorrow() == null ? 5 : reader.getMaxBorrow();
        if (active >= maxBorrow) {
            throw new ApiException("该读者已借满 " + maxBorrow + " 本，不能再借了");
        }

        // 原子扣库存, 库存不足时更新不到行
        int updated = bookMapper.update(null, new LambdaUpdateWrapper<Book>()
                .setSql("stock = stock - 1")
                .eq(Book::getBookId, bookId)
                .gt(Book::getStock, 0));
        if (updated == 0) {
            throw new ApiException("该图书暂时没有库存");
        }

        int days = request.getDays() == null || request.getDays() <= 0 ? 30 : request.getDays();
        Borrow borrow = new Borrow();
        borrow.setBookId(bookId);
        borrow.setReaderId(readerId);
        borrow.setBorrowTime(LocalDateTime.now());
        borrow.setDueTime(LocalDateTime.now().plusDays(days));
        borrow.setStatus(0);
        borrowMapper.insert(borrow);
    }

    @Transactional
    public void returnBook(int borrowId) {
        Borrow borrow = borrowMapper.selectById(borrowId);
        if (borrow == null) {
            throw new ApiException("记录不存在");
        }
        if (borrow.getStatus() != null && borrow.getStatus() == 1) {
            throw new ApiException("该记录已经归还过了");
        }

        borrowMapper.update(null, new LambdaUpdateWrapper<Borrow>()
                .set(Borrow::getReturnTime, LocalDateTime.now())
                .set(Borrow::getStatus, 1)
                .eq(Borrow::getBorrowId, borrowId));
        bookMapper.update(null, new LambdaUpdateWrapper<Book>()
                .setSql("stock = stock + 1")
                .eq(Book::getBookId, borrow.getBookId()));

        // 逾期归还记一次违约
        if (borrow.getDueTime() != null && borrow.getDueTime().isBefore(LocalDateTime.now())) {
            readerMapper.update(null, new LambdaUpdateWrapper<Reader>()
                    .setSql("violation_count = violation_count + 1")
                    .eq(Reader::getReaderId, borrow.getReaderId()));
        }
    }

    @Transactional
    public void renew(int borrowId) {
        Borrow borrow = borrowMapper.selectById(borrowId);
        if (borrow == null) {
            throw new ApiException("记录不存在");
        }
        if (borrow.getStatus() == null || borrow.getStatus() != 0) {
            throw new ApiException("只有借阅中的图书才能续借");
        }
        if (borrow.getDueTime() == null || borrow.getDueTime().isBefore(LocalDateTime.now())) {
            throw new ApiException("图书已逾期，请先归还再借");
        }
        borrowMapper.update(null, new LambdaUpdateWrapper<Borrow>()
                .setSql("due_time = DATE_ADD(due_time, INTERVAL 30 DAY)")
                .eq(Borrow::getBorrowId, borrowId));
    }
}
