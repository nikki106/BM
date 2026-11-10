-- ============================================================
-- 图书管理系统 (BM) 数据库建表脚本
-- 数据库: BM   MySQL 8.x   字符集: utf8mb4
-- 说明: 重新执行本脚本会删除并重建所有表(含初始数据重置)
-- ============================================================

-- 创建数据库(与 application.properties 中的库名一致)
CREATE DATABASE IF NOT EXISTS `BM` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `BM`;

SET NAMES utf8mb4;

-- 关闭外键检查后按依赖顺序删表
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `borrow`;
DROP TABLE IF EXISTS `reader`;
DROP TABLE IF EXISTS `admin`;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `book`;
DROP TABLE IF EXISTS `category`;
DROP TABLE IF EXISTS `notice`;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 1. 图书分类表 category
-- ============================================================
CREATE TABLE `category` (
    `category_id`   INT         NOT NULL AUTO_INCREMENT COMMENT '分类编号',
    `category_name` VARCHAR(50) NOT NULL                COMMENT '分类名称',
    PRIMARY KEY (`category_id`),
    UNIQUE KEY `uk_category_name` (`category_name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '图书分类表';

-- ============================================================
-- 2. 图书表 book
-- ============================================================
CREATE TABLE `book` (
    `book_id`     INT           NOT NULL AUTO_INCREMENT COMMENT '图书编号',
    `title`       VARCHAR(100)  NOT NULL                COMMENT '书名',
    `author`      VARCHAR(50)   DEFAULT NULL            COMMENT '作者',
    `publisher`   VARCHAR(100)  DEFAULT NULL            COMMENT '出版社',
    `isbn`        VARCHAR(20)   DEFAULT NULL            COMMENT 'ISBN号',
    `category_id` INT           DEFAULT NULL            COMMENT '分类编号(关联category表)',
    `stock`       INT           NOT NULL DEFAULT 0      COMMENT '库存数量',
    `price`       DECIMAL(10,2) NOT NULL DEFAULT 0.00   COMMENT '价格(元)',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
    PRIMARY KEY (`book_id`),
    UNIQUE KEY `uk_isbn` (`isbn`),
    KEY `idx_book_category` (`category_id`),
    CONSTRAINT `fk_book_category` FOREIGN KEY (`category_id`)
        REFERENCES `category` (`category_id`) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '图书表';

-- ============================================================
-- 3. 用户表 user (管理员与读者的登录账号)
-- ============================================================
CREATE TABLE `user` (
    `user_id`     INT          NOT NULL AUTO_INCREMENT COMMENT '用户编号',
    `username`    VARCHAR(50)  NOT NULL                COMMENT '账号',
    `password`    VARCHAR(100) NOT NULL                COMMENT '密码(建议MD5加密存储)',
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'READER' COMMENT '角色: ADMIN-管理员 READER-读者',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态: 1-正常 0-禁用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- ============================================================
-- 4. 管理员表 admin
-- ============================================================
CREATE TABLE `admin` (
    `admin_id`    INT          NOT NULL AUTO_INCREMENT COMMENT '管理员编号',
    `user_id`     INT          NOT NULL                COMMENT '用户编号(关联user表)',
    `name`        VARCHAR(50)  NOT NULL                COMMENT '姓名',
    `phone`       VARCHAR(20)  DEFAULT NULL            COMMENT '联系电话',
    `email`       VARCHAR(50)  DEFAULT NULL            COMMENT '邮箱',
    `permissions` VARCHAR(200) DEFAULT NULL            COMMENT '权限信息(逗号分隔,如 book:add,book:del)',
    PRIMARY KEY (`admin_id`),
    KEY `idx_admin_user` (`user_id`),
    CONSTRAINT `fk_admin_user` FOREIGN KEY (`user_id`)
        REFERENCES `user` (`user_id`) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '管理员表';

-- ============================================================
-- 5. 读者表 reader
-- ============================================================
CREATE TABLE `reader` (
    `reader_id`       INT         NOT NULL AUTO_INCREMENT COMMENT '读者编号',
    `card_no`         VARCHAR(20) NOT NULL                COMMENT '读者证号',
    `name`            VARCHAR(50) NOT NULL                COMMENT '姓名',
    `phone`           VARCHAR(20) DEFAULT NULL            COMMENT '联系方式',
    `max_borrow`      INT         NOT NULL DEFAULT 5      COMMENT '可借数量(最多可同时借阅的图书数)',
    `violation_count` INT         NOT NULL DEFAULT 0      COMMENT '违约记录(违约次数,如超期未还)',
    `user_id`         INT         DEFAULT NULL            COMMENT '用户编号(关联user表,读者登录账号)',
    `create_time`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    PRIMARY KEY (`reader_id`),
    UNIQUE KEY `uk_card_no` (`card_no`),
    KEY `idx_reader_user` (`user_id`),
    CONSTRAINT `fk_reader_user` FOREIGN KEY (`user_id`)
        REFERENCES `user` (`user_id`) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '读者表';

-- ============================================================
-- 6. 借阅记录表 borrow
-- ============================================================
CREATE TABLE `borrow` (
    `borrow_id`   INT      NOT NULL AUTO_INCREMENT COMMENT '借阅编号',
    `book_id`     INT      NOT NULL                COMMENT '图书编号(关联book表)',
    `reader_id`   INT      NOT NULL                COMMENT '读者编号(关联reader表)',
    `borrow_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '借阅时间',
    `due_time`    DATETIME NOT NULL                COMMENT '应还时间',
    `return_time` DATETIME DEFAULT NULL            COMMENT '归还时间(未归还为NULL)',
    `status`      TINYINT  NOT NULL DEFAULT 0      COMMENT '借阅状态: 0-借阅中 1-已归还 2-已逾期',
    PRIMARY KEY (`borrow_id`),
    KEY `idx_borrow_book` (`book_id`),
    KEY `idx_borrow_reader` (`reader_id`),
    CONSTRAINT `fk_borrow_book` FOREIGN KEY (`book_id`)
        REFERENCES `book` (`book_id`) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT `fk_borrow_reader` FOREIGN KEY (`reader_id`)
        REFERENCES `reader` (`reader_id`) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '借阅记录表';

-- ============================================================
-- 7. 公告表 notice
-- ============================================================
CREATE TABLE `notice` (
    `notice_id`    INT          NOT NULL AUTO_INCREMENT COMMENT '公告编号',
    `title`        VARCHAR(100) NOT NULL                COMMENT '公告标题',
    `content`      TEXT         DEFAULT NULL            COMMENT '公告内容',
    `publish_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    PRIMARY KEY (`notice_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '公告表';

-- ============================================================
-- 初始数据
-- ============================================================

-- 图书分类
INSERT INTO `category` (`category_name`) VALUES
('文学'), ('计算机'), ('历史'), ('科学'), ('艺术'), ('教育'), ('经济');

-- 用户账号(密码均为 123456 的 MD5 加密值: e10adc3949ba59abbe56e057f20f883e)
INSERT INTO `user` (`username`, `password`, `role`, `status`) VALUES
('admin',    'e10adc3949ba59abbe56e057f20f883e', 'ADMIN',  1),
('reader01', 'e10adc3949ba59abbe56e057f20f883e', 'READER', 1);

-- 管理员
INSERT INTO `admin` (`user_id`, `name`, `phone`, `email`, `permissions`) VALUES
(1, '系统管理员', '13800000000', 'admin@bm.com',
 'book:add,book:update,book:del,reader:manage,borrow:manage,notice:manage');

-- 读者
INSERT INTO `reader` (`card_no`, `name`, `phone`, `max_borrow`, `violation_count`, `user_id`) VALUES
('R20260001', '张三', '13900000001', 5, 0, 2);

-- 图书
INSERT INTO `book` (`title`, `author`, `publisher`, `isbn`, `category_id`, `stock`, `price`) VALUES
('Java编程思想',      'Bruce Eckel',       '机械工业出版社',     '9787111213826', 2, 10, 108.00),
('三体',              '刘慈欣',            '重庆出版社',         '9787536692930', 1, 15,  23.00),
('明朝那些事儿',      '当年明月',          '中国海关出版社',     '9787801656179', 3,  8, 358.20),
('时间简史',          '史蒂芬·霍金',       '湖南科学技术出版社', '9787535732309', 4,  5,  45.00),
('百年孤独',          '加西亚·马尔克斯',   '南海出版公司',       '9787544253994', 1, 12,  39.50);

-- 借阅记录(示例: 张三借了1本书, 借期30天, 借阅中)
INSERT INTO `borrow` (`book_id`, `reader_id`, `borrow_time`, `due_time`, `status`) VALUES
(1, 1, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 0);

-- 公告
INSERT INTO `notice` (`title`, `content`) VALUES
('开馆通知',     '图书馆将于2026年11月10日正式开放，欢迎广大读者前来借阅。'),
('图书借阅规则', '每位读者最多可同时借阅5本图书，借阅期限为30天，逾期未还将产生违约记录。');
