package com.myproject.bm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myproject.bm.entity.Borrow;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface BorrowMapper extends BaseMapper<Borrow> {

    /**
     * 借阅分页查询, 联表带出书名/读者姓名/证号。
     * status 筛选: 0-借阅中(未归还且未逾期) 1-已归还 2-已逾期(未归还且超过应还时间)
     */
    @Select("""
            <script>
            SELECT b.borrow_id, b.book_id, b.reader_id, b.borrow_time, b.due_time, b.return_time, b.status,
                   bk.title AS book_title, r.name AS reader_name, r.card_no AS card_no
            FROM borrow b
            LEFT JOIN book bk ON b.book_id = bk.book_id
            LEFT JOIN reader r ON b.reader_id = r.reader_id
            <where>
                <if test="status != null">
                    <choose>
                        <when test="status == 0">AND b.return_time IS NULL AND b.due_time &gt;= NOW()</when>
                        <when test="status == 1">AND b.status = 1</when>
                        <when test="status == 2">AND b.return_time IS NULL AND b.due_time &lt; NOW()</when>
                    </choose>
                </if>
                <if test="readerId != null">AND b.reader_id = #{readerId}</if>
                <if test="keyword != null and keyword != ''">
                    AND (bk.title LIKE CONCAT('%', #{keyword}, '%') OR r.name LIKE CONCAT('%', #{keyword}, '%'))
                </if>
            </where>
            ORDER BY b.borrow_time DESC, b.borrow_id DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Borrow> selectBorrowPage(@Param("offset") int offset,
                                  @Param("size") int size,
                                  @Param("status") Integer status,
                                  @Param("readerId") Integer readerId,
                                  @Param("keyword") String keyword);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM borrow b
            LEFT JOIN book bk ON b.book_id = bk.book_id
            LEFT JOIN reader r ON b.reader_id = r.reader_id
            <where>
                <if test="status != null">
                    <choose>
                        <when test="status == 0">AND b.return_time IS NULL AND b.due_time &gt;= NOW()</when>
                        <when test="status == 1">AND b.status = 1</when>
                        <when test="status == 2">AND b.return_time IS NULL AND b.due_time &lt; NOW()</when>
                    </choose>
                </if>
                <if test="readerId != null">AND b.reader_id = #{readerId}</if>
                <if test="keyword != null and keyword != ''">
                    AND (bk.title LIKE CONCAT('%', #{keyword}, '%') OR r.name LIKE CONCAT('%', #{keyword}, '%'))
                </if>
            </where>
            </script>
            """)
    long countBorrow(@Param("status") Integer status,
                     @Param("readerId") Integer readerId,
                     @Param("keyword") String keyword);

    /**
     * 批量统计读者的当前借阅数量(未归还)
     */
    @Select("""
            <script>
            SELECT reader_id AS readerId, COUNT(*) AS cnt
            FROM borrow
            WHERE return_time IS NULL
              AND reader_id IN
              <foreach collection="readerIds" item="id" open="(" separator="," close=")">#{id}</foreach>
            GROUP BY reader_id
            </script>
            """)
    List<Map<String, Object>> countActiveByReaderIds(@Param("readerIds") List<Integer> readerIds);
}
