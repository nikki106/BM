package com.myproject.bm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myproject.bm.common.ApiException;
import com.myproject.bm.common.PageResult;
import com.myproject.bm.entity.Notice;
import com.myproject.bm.mapper.NoticeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeMapper noticeMapper;

    public PageResult<Notice> page(int pageNum, int pageSize) {
        Page<Notice> page = noticeMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Notice>()
                        .orderByDesc(Notice::getPublishTime)
                        .orderByDesc(Notice::getNoticeId));
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    public Notice save(Notice notice) {
        if (notice.getTitle() == null || notice.getTitle().isBlank()) {
            throw new ApiException("公告标题不能为空");
        }
        notice.setTitle(notice.getTitle().trim());
        if (notice.getContent() == null) {
            notice.setContent("");
        }
        if (notice.getNoticeId() == null) {
            noticeMapper.insert(notice);
        } else if (noticeMapper.updateById(notice) == 0) {
            throw new ApiException("公告不存在");
        }
        // 重新查询, 返回带 publishTime 的完整数据
        return noticeMapper.selectById(notice.getNoticeId());
    }

    public void delete(int noticeId) {
        noticeMapper.deleteById(noticeId);
    }
}
