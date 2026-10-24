-- ============================================================
-- 湖北省博物馆参观预约系统 数据库结构脚本
-- 数据库：MySQL 8.0+ / 9.x
-- 字符集：utf8mb4
-- 执行方式（root 账号）：
--   mysql -uroot -p < database/schema.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS museum_booking
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE museum_booking;

-- 应用数据库账号（可选；如已存在会忽略）
CREATE USER IF NOT EXISTS 'museum'@'localhost' IDENTIFIED BY 'museum123';
GRANT ALL PRIVILEGES ON museum_booking.* TO 'museum'@'localhost';
FLUSH PRIVILEGES;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS visit_slot;
DROP TABLE IF EXISTS feedback;
DROP TABLE IF EXISTS announcement;
DROP TABLE IF EXISTS exhibition;
DROP TABLE IF EXISTS sys_user;

SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
-- 1. 用户表
-- ------------------------------------------------------------
CREATE TABLE sys_user (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username      VARCHAR(32)  NOT NULL COMMENT '登录账号',
    password      VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码密文',
    real_name     VARCHAR(50)  NOT NULL COMMENT '真实姓名',
    id_card       VARCHAR(18)  DEFAULT NULL COMMENT '身份证号',
    phone         VARCHAR(20)  NOT NULL COMMENT '手机号',
    email         VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    avatar        VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    role          VARCHAR(20)  NOT NULL DEFAULT 'VISITOR' COMMENT '角色：VISITOR/STAFF/ADMIN',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    last_login_at DATETIME     DEFAULT NULL COMMENT '最近登录时间',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_username (username),
    KEY idx_user_role (role),
    KEY idx_user_phone (phone)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '系统用户表';

-- ------------------------------------------------------------
-- 2. 展览表
-- ------------------------------------------------------------
CREATE TABLE exhibition (
    id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    title        VARCHAR(100)  NOT NULL COMMENT '展览名称',
    subtitle     VARCHAR(200)  DEFAULT NULL COMMENT '副标题/一句话简介',
    category     VARCHAR(30)   NOT NULL DEFAULT '常设展览' COMMENT '展览分类',
    cover_url    VARCHAR(255)  DEFAULT NULL COMMENT '封面图地址',
    description  TEXT          COMMENT '展览详细介绍',
    venue        VARCHAR(100)  NOT NULL COMMENT '展出地点',
    start_date   DATE          NOT NULL COMMENT '开始日期',
    end_date     DATE          NOT NULL COMMENT '结束日期',
    open_time    TIME          NOT NULL DEFAULT '09:00:00' COMMENT '开放开始时间',
    close_time   TIME          NOT NULL DEFAULT '17:00:00' COMMENT '开放结束时间',
    ticket_price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '票价（元），0 表示免费',
    tips         VARCHAR(500)  DEFAULT NULL COMMENT '参观提示',
    status       VARCHAR(20)   NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/PUBLISHED/CLOSED',
    sort_order   INT           NOT NULL DEFAULT 0 COMMENT '首页排序值，越大越靠前',
    view_count   INT           NOT NULL DEFAULT 0 COMMENT '浏览次数',
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_exhibition_status (status),
    KEY idx_exhibition_category (category),
    KEY idx_exhibition_date (start_date, end_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '展览信息表';

-- ------------------------------------------------------------
-- 3. 参观时段表
-- ------------------------------------------------------------
CREATE TABLE visit_slot (
    id            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    exhibition_id BIGINT      NOT NULL COMMENT '所属展览',
    visit_date    DATE        NOT NULL COMMENT '参观日期',
    start_time    TIME        NOT NULL COMMENT '时段开始时间',
    end_time      TIME        NOT NULL COMMENT '时段结束时间',
    capacity      INT         NOT NULL DEFAULT 200 COMMENT '可预约总人数',
    booked_count  INT         NOT NULL DEFAULT 0 COMMENT '已预约人数',
    status        VARCHAR(20) NOT NULL DEFAULT 'OPEN' COMMENT '状态：OPEN/CLOSED',
    remark        VARCHAR(255) DEFAULT NULL COMMENT '备注',
    created_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_slot_exhibition_date_start (exhibition_id, visit_date, start_time),
    KEY idx_slot_date (visit_date),
    KEY idx_slot_status (status),
    CONSTRAINT fk_slot_exhibition FOREIGN KEY (exhibition_id) REFERENCES exhibition (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '参观时段/库存表';

-- ------------------------------------------------------------
-- 4. 预约订单表
-- ------------------------------------------------------------
CREATE TABLE reservation (
    id              BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_no        VARCHAR(32) NOT NULL COMMENT '预约单号',
    user_id         BIGINT      NOT NULL COMMENT '预约用户',
    slot_id         BIGINT      NOT NULL COMMENT '参观时段',
    exhibition_id   BIGINT      NOT NULL COMMENT '展览',
    visit_date      DATE        NOT NULL COMMENT '参观日期（冗余，便于查询）',
    start_time      TIME        NOT NULL COMMENT '时段开始（冗余）',
    end_time        TIME        NOT NULL COMMENT '时段结束（冗余）',
    visitor_name    VARCHAR(50) NOT NULL COMMENT '参观人姓名',
    visitor_id_card VARCHAR(18) NOT NULL COMMENT '参观人身份证号',
    visitor_phone   VARCHAR(20) NOT NULL COMMENT '参观人手机号',
    ticket_count    INT         NOT NULL DEFAULT 1 COMMENT '预约人数',
    status          VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED' COMMENT '状态：CONFIRMED/CHECKED_IN/CANCELLED/EXPIRED',
    ticket_code     VARCHAR(32) NOT NULL COMMENT '核销码/二维码内容',
    remark          VARCHAR(255) DEFAULT NULL COMMENT '备注',
    cancel_reason   VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
    cancelled_at    DATETIME    DEFAULT NULL COMMENT '取消时间',
    checkin_at      DATETIME    DEFAULT NULL COMMENT '核销入馆时间',
    source          VARCHAR(20) NOT NULL DEFAULT 'WEB' COMMENT '来源：WEB/ADMIN',
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_reservation_order_no (order_no),
    UNIQUE KEY uk_reservation_ticket_code (ticket_code),
    KEY idx_reservation_user (user_id, status),
    KEY idx_reservation_slot (slot_id),
    KEY idx_reservation_date (visit_date),
    KEY idx_reservation_status (status),
    CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_reservation_slot FOREIGN KEY (slot_id) REFERENCES visit_slot (id),
    CONSTRAINT fk_reservation_exhibition FOREIGN KEY (exhibition_id) REFERENCES exhibition (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '预约订单表';

-- ------------------------------------------------------------
-- 5. 公告表
-- ------------------------------------------------------------
CREATE TABLE announcement (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    title        VARCHAR(120) NOT NULL COMMENT '公告标题',
    summary      VARCHAR(255) DEFAULT NULL COMMENT '摘要',
    content      MEDIUMTEXT   NOT NULL COMMENT '正文',
    category     VARCHAR(30)  NOT NULL DEFAULT 'NOTICE' COMMENT '分类：NOTICE/NEWS/ACTIVITY',
    cover_url    VARCHAR(255) DEFAULT NULL COMMENT '封面图',
    is_top       TINYINT      NOT NULL DEFAULT 0 COMMENT '是否置顶：1是 0否',
    status       TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：1已发布 0草稿',
    publish_time DATETIME     DEFAULT NULL COMMENT '发布时间',
    view_count   INT          NOT NULL DEFAULT 0 COMMENT '阅读量',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_announcement_status (status),
    KEY idx_announcement_publish (publish_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '博物馆公告表';

-- ------------------------------------------------------------
-- 6. 意见反馈表
-- ------------------------------------------------------------
CREATE TABLE feedback (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id    BIGINT       NOT NULL COMMENT '提交用户',
    type       VARCHAR(30)  NOT NULL DEFAULT 'SUGGESTION' COMMENT '类型：SUGGESTION/COMPLAINT/PRAISE/OTHER',
    content    VARCHAR(1000) NOT NULL COMMENT '反馈内容',
    contact    VARCHAR(100) DEFAULT NULL COMMENT '联系方式',
    reply      VARCHAR(1000) DEFAULT NULL COMMENT '管理员回复',
    status     VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/REPLIED',
    replied_at DATETIME     DEFAULT NULL COMMENT '回复时间',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_feedback_user (user_id),
    KEY idx_feedback_status (status),
    CONSTRAINT fk_feedback_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '意见反馈表';