-- ============================================================
-- 湖北省博物馆参观预约系统 初始化数据
-- 请先执行 schema.sql，再执行本脚本
-- 默认密码：Admin@123 / Staff@123 / Visitor@123
-- ============================================================

USE museum_booking;
SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 用户（密码为 BCrypt 密文，明文见上方说明）
-- ------------------------------------------------------------
INSERT INTO sys_user (username, password, real_name, id_card, phone, email, role, status)
VALUES ('admin', '$2a$10$MDSVVeMMnRjR46gtDJAyqOb.FcYEI9/i6CQ5/55ZTCnyt.S5RvpWS', '系统管理员', '420106199001011234', '13800000001', 'admin@hbmuseum.cn', 'ADMIN', 1),
       ('staff', '$2a$10$THrF0WdsSxM06vXW/cAF2u2bKEy1Pk2shKqAHZ5erMU/r12aMFPrm', '检票员小周', '420106199505051234', '13800000002', 'staff@hbmuseum.cn', 'STAFF', 1),
       ('visitor', '$2a$10$44woE6.yFH1uAW/3ICeN/O4/WUnfW5F.znz8ZKEXMs95JBV/Ie2s2', '李明', '420106199808081234', '13800000003', 'liming@example.com', 'VISITOR', 1),
       ('xiaoyu', '$2a$10$44woE6.yFH1uAW/3ICeN/O4/WUnfW5F.znz8ZKEXMs95JBV/Ie2s2', '张小雨', '420106200003031234', '13800000004', 'xiaoyu@example.com', 'VISITOR', 1),
       ('wangwei', '$2a$10$44woE6.yFH1uAW/3ICeN/O4/WUnfW5F.znz8ZKEXMs95JBV/Ie2s2', '王伟', '420106198712121234', '13800000005', 'wangwei@example.com', 'VISITOR', 1);

-- ------------------------------------------------------------
-- 展览
-- ------------------------------------------------------------
INSERT INTO exhibition (id, title, subtitle, category, cover_url, description, venue, start_date, end_date,
                        open_time, close_time, ticket_price, tips, status, sort_order, view_count)
VALUES (1, '曾侯乙编钟', '聆听两千四百年前的华夏正音', '常设展览', '/images/exhibitions/bells.svg',
        '曾侯乙编钟出土于湖北随州曾侯乙墓，是战国早期的大型礼乐重器。全套编钟共六十五件，分三层八组悬挂于曲尺形钟架之上，总重量超过五吨。钟体上铸有三千七百余字铭文，记述了先秦时期的乐学理论，被誉为“古代世界第八大奇迹”。\n\n本展以编钟为核心，结合出土乐器、礼器与复原演奏场景，系统呈现先秦礼乐文明的辉煌成就。观众可近距离观察钟体纹饰与铭文，并通过多媒体装置聆听穿越千年的钟磬之音。',
        '综合馆一楼 · 曾侯乙展厅', '2024-01-01', '2027-12-31', '09:00:00', '17:00:00', 0.00,
        '展厅内请勿触摸展品；编钟演奏展示每日 10:30、14:30 各一场。', 'PUBLISHED', 100, 2860),
       (2, '越王勾践剑', '天下第一剑的千年锋芒', '常设展览', '/images/exhibitions/sword.svg',
        '越王勾践剑于1965年出土于湖北江陵望山一号楚墓，通长55.7厘米，剑身布满黑色菱形暗格花纹，剑格镶嵌蓝色琉璃与绿松石。剑身近格处铸有“越王鸠浅自作用剑”八字鸟篆铭文，历经两千余年仍寒光凛冽、锋利如新。\n\n展览以剑为中心，串联起春秋战国时期的吴越争霸、楚越交流与青铜铸造工艺，配合显微观察与合金成分解析，揭示“千年不锈”的科学奥秘。',
        '综合馆二楼 · 越王勾践剑展厅', '2024-01-01', '2027-12-31', '09:00:00', '17:00:00', 0.00,
        '该展厅人流较多，建议错峰参观；拍照请关闭闪光灯。', 'PUBLISHED', 95, 3210),
       (3, '楚国八百年', '荆楚大地的浪漫与雄奇', '常设展览', '/images/exhibitions/chu.svg',
        '楚国是先秦时期延续时间最长、疆域最广的诸侯国之一，创造了独具特色的楚文化。本展以“筚路蓝缕”“问鼎中原”“楚辞华章”三个单元，展出青铜礼器、漆木器、丝织品、简牍等珍贵文物三百余件。\n\n从虎座鸟架鼓到人物龙凤帛画，从楚式升鼎到错金银铜樽，展览勾勒出楚人尚赤、崇凤、好巫的文化性格，也呈现了长江中游文明多元一体的发展脉络。',
        '楚文化馆一楼', '2024-03-01', '2027-12-31', '09:00:00', '17:00:00', 0.00,
        '展线较长，建议预留 60 分钟参观时间。', 'PUBLISHED', 90, 1976),
       (4, '梁庄王珍藏', '明代藩王的地下宝库', '常设展览', '/images/exhibitions/jewel.svg',
        '梁庄王朱瞻垍是明仁宗第九子，其墓出土金、银、玉、瓷器等文物五千余件，其中金器总重量超过十公斤。展览分为“藩王礼制”“金玉满堂”“海上丝路”三个部分，展出金镶宝帽顶、金累丝镶宝石带、青花瓷等精品。\n\n精美的金银器与异域宝石，既反映了明代藩王的奢华生活，也见证了郑和下西洋背景下的中外物质文化交流。',
        '综合馆三楼 · 梁庄王展厅', '2024-01-01', '2027-12-31', '09:00:00', '17:00:00', 0.00,
        '金器展柜反光较强，建议调整拍摄角度。', 'PUBLISHED', 80, 1542),
       (5, '荆楚文明之光', '长江中游的文明进程', '专题特展', '/images/exhibitions/yangtze.svg',
        '本特展汇聚湖北省内十余家文博单位的精品文物一百八十余件，以时间为主线，讲述从旧石器时代到明清时期荆楚大地上的文明演进。\n\n展览运用场景复原、数字投影与互动触摸屏，将考古发现转化为可感可知的历史叙事，适合亲子家庭与青少年观众参观。',
        '临时展厅一', '2026-09-20', '2027-03-20', '09:00:00', '17:00:00', 30.00,
        '特展需单独预约；1.2 米以下儿童免票，需由成人陪同。', 'PUBLISHED', 70, 986),
       (6, '长江文明特展', '一条大河的万年记忆', '临时展览', '/images/exhibitions/river.svg',
        '长江孕育了灿烂的稻作文明、青铜文明与近代工业文明。本次展览联合长江沿线博物馆，以“源·流·汇”为叙事结构，展出陶器、青铜器、织绣与近现代影像资料二百余件。\n\n展览特别设置“长江生物多样性”单元，呼吁公众共同守护母亲河。',
        '临时展厅二', '2026-10-01', '2027-01-10', '09:00:00', '17:00:00', 50.00,
        '展览最后一单元设有互动盖章区，可自带集章本。', 'PUBLISHED', 60, 734);

-- ------------------------------------------------------------
-- 参观时段：今天前 3 天至未来 13 天，每天三个时段，周一闭馆
-- ------------------------------------------------------------
INSERT INTO visit_slot (exhibition_id, visit_date, start_time, end_time, capacity, status, remark)
SELECT e.id,
       DATE_ADD(CURDATE(), INTERVAL d.n DAY),
       s.start_time,
       s.end_time,
       s.capacity,
       'OPEN',
       '系统初始化时段'
FROM exhibition e
         CROSS JOIN (SELECT 0 AS n
                     UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3
                     UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6
                     UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
                     UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
                     UNION ALL SELECT 13 UNION ALL SELECT -1 UNION ALL SELECT -2
                     UNION ALL SELECT -3) d
         CROSS JOIN (SELECT '09:00:00' AS start_time, '11:00:00' AS end_time, 200 AS capacity
                     UNION ALL
                     SELECT '11:00:00', '13:00:00', 300
                     UNION ALL
                     SELECT '13:00:00', '16:00:00', 300) s
WHERE e.status = 'PUBLISHED'
  AND DAYOFWEEK(DATE_ADD(CURDATE(), INTERVAL d.n DAY)) <> 2;

-- ------------------------------------------------------------
-- 预约订单（依赖上面生成的时段；找不到对应时段时自动跳过）
-- ------------------------------------------------------------
INSERT INTO reservation (order_no, user_id, slot_id, exhibition_id, visit_date, start_time, end_time,
                         visitor_name, visitor_id_card, visitor_phone, ticket_count, status, ticket_code,
                         remark, checkin_at, cancel_reason, cancelled_at, source, created_at)
SELECT 'HB202610220001', u.id, s.id, s.exhibition_id, s.visit_date, s.start_time, s.end_time,
       '李明', '420106199808081234', '13800000003', 1, 'CHECKED_IN', 'TC202610220001',
       '希望聆听编钟演奏', TIMESTAMP(CURDATE(), '10:05:00'), NULL, NULL, 'WEB', TIMESTAMP(CURDATE(), '08:30:00')
FROM visit_slot s
         JOIN sys_user u ON u.username = 'visitor'
WHERE s.exhibition_id = 1 AND s.visit_date = CURDATE() AND s.start_time = '09:00:00'
ORDER BY s.id LIMIT 1;

INSERT INTO reservation (order_no, user_id, slot_id, exhibition_id, visit_date, start_time, end_time,
                         visitor_name, visitor_id_card, visitor_phone, ticket_count, status, ticket_code,
                         remark, source, created_at)
SELECT 'HB202610220002', u.id, s.id, s.exhibition_id, s.visit_date, s.start_time, s.end_time,
       '李明', '420106199808081234', '13800000003', 2, 'CONFIRMED', 'TC202610220002',
       '与家人同行', 'WEB', TIMESTAMP(CURDATE(), '09:10:00')
FROM visit_slot s
         JOIN sys_user u ON u.username = 'visitor'
WHERE s.exhibition_id = 2 AND s.visit_date = DATE_ADD(CURDATE(), INTERVAL 1 DAY) AND s.start_time = '09:00:00'
ORDER BY s.id LIMIT 1;

INSERT INTO reservation (order_no, user_id, slot_id, exhibition_id, visit_date, start_time, end_time,
                         visitor_name, visitor_id_card, visitor_phone, ticket_count, status, ticket_code,
                         remark, source, created_at)
SELECT 'HB202610220003', u.id, s.id, s.exhibition_id, s.visit_date, s.start_time, s.end_time,
       '张小雨', '420106200003031234', '13800000004', 3, 'CONFIRMED', 'TC202610220003',
       '研学团队预约', 'WEB', TIMESTAMP(CURDATE(), '10:20:00')
FROM visit_slot s
         JOIN sys_user u ON u.username = 'xiaoyu'
WHERE s.exhibition_id = 3 AND s.visit_date = DATE_ADD(CURDATE(), INTERVAL 2 DAY) AND s.start_time = '11:00:00'
ORDER BY s.id LIMIT 1;

INSERT INTO reservation (order_no, user_id, slot_id, exhibition_id, visit_date, start_time, end_time,
                         visitor_name, visitor_id_card, visitor_phone, ticket_count, status, ticket_code,
                         remark, source, created_at)
SELECT 'HB202610220004', u.id, s.id, s.exhibition_id, s.visit_date, s.start_time, s.end_time,
       '王伟', '420106198712121234', '13800000005', 1, 'CANCELLED', 'TC202610220004',
       '行程有变', 'WEB', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '15:20:00')
FROM visit_slot s
         JOIN sys_user u ON u.username = 'wangwei'
WHERE s.exhibition_id = 5 AND s.visit_date = DATE_SUB(CURDATE(), INTERVAL 1 DAY) AND s.start_time = '11:00:00'
ORDER BY s.id LIMIT 1;

INSERT INTO reservation (order_no, user_id, slot_id, exhibition_id, visit_date, start_time, end_time,
                         visitor_name, visitor_id_card, visitor_phone, ticket_count, status, ticket_code,
                         remark, source, created_at)
SELECT 'HB202610220005', u.id, s.id, s.exhibition_id, s.visit_date, s.start_time, s.end_time,
       '李明', '420106199808081234', '13800000003', 1, 'EXPIRED', 'TC202610220005',
       NULL, 'WEB', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '11:00:00')
FROM visit_slot s
         JOIN sys_user u ON u.username = 'visitor'
WHERE s.exhibition_id = 1 AND s.visit_date = DATE_SUB(CURDATE(), INTERVAL 3 DAY) AND s.start_time = '09:00:00'
ORDER BY s.id LIMIT 1;

INSERT INTO reservation (order_no, user_id, slot_id, exhibition_id, visit_date, start_time, end_time,
                         visitor_name, visitor_id_card, visitor_phone, ticket_count, status, ticket_code,
                         remark, source, created_at)
SELECT 'HB202610220006', u.id, s.id, s.exhibition_id, s.visit_date, s.start_time, s.end_time,
       '张小雨', '420106200003031234', '13800000004', 3, 'CONFIRMED', 'TC202610220006',
       '亲子参观', 'WEB', TIMESTAMP(CURDATE(), '11:30:00')
FROM visit_slot s
         JOIN sys_user u ON u.username = 'xiaoyu'
WHERE s.exhibition_id = 6 AND s.visit_date = DATE_ADD(CURDATE(), INTERVAL 1 DAY) AND s.start_time = '13:00:00'
ORDER BY s.id LIMIT 1;

INSERT INTO reservation (order_no, user_id, slot_id, exhibition_id, visit_date, start_time, end_time,
                         visitor_name, visitor_id_card, visitor_phone, ticket_count, status, ticket_code,
                         remark, checkin_at, source, created_at)
SELECT 'HB202610220007', u.id, s.id, s.exhibition_id, s.visit_date, s.start_time, s.end_time,
       '王伟', '420106198712121234', '13800000005', 2, 'CHECKED_IN', 'TC202610220007',
       '团队参观', TIMESTAMP(CURDATE(), '11:20:00'), 'WEB', TIMESTAMP(CURDATE(), '09:00:00')
FROM visit_slot s
         JOIN sys_user u ON u.username = 'wangwei'
WHERE s.exhibition_id = 4 AND s.visit_date = CURDATE() AND s.start_time = '11:00:00'
ORDER BY s.id LIMIT 1;

UPDATE reservation
SET cancelled_at = TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '16:00:00')
WHERE order_no = 'HB202610220004';

-- 根据有效订单回填时段已预约人数
UPDATE visit_slot s
SET s.booked_count = (SELECT COALESCE(SUM(r.ticket_count), 0)
                      FROM reservation r
                      WHERE r.slot_id = s.id
                        AND r.status IN ('CONFIRMED', 'CHECKED_IN'));

-- ------------------------------------------------------------
-- 公告
-- ------------------------------------------------------------
INSERT INTO announcement (title, summary, content, category, is_top, status, publish_time, view_count)
VALUES ('2026年国庆假期开放公告',
        '国庆假期（10月1日至10月8日）正常开放，实行实名预约参观。',
        '尊敬的观众朋友：\n\n为满足广大观众假期参观需求，湖北省博物馆2026年国庆假期（10月1日至10月8日）正常开放，开放时间为9:00—17:00（16:00停止入馆）。\n\n一、预约方式\n观众可通过“湖北省博物馆参观预约系统”提前7天实名预约，每个账号最多可预约5人。\n\n二、入馆须知\n请携带预约时使用的有效身份证件，按预约时段到馆，经核验后入馆。\n\n三、温馨提示\n假期客流较大，建议绿色出行，错峰参观。展厅内请勿饮食、追逐喧哗。\n\n感谢您的理解与配合！',
        'NOTICE', 1, 1, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '09:00:00'), 1286),
       ('“荆楚文明之光”特展开展',
        '汇聚全省十余家文博单位精品文物，展期至2027年3月20日。',
        '9月20日，“荆楚文明之光——长江中游的文明进程”特展在湖北省博物馆临时展厅一开展。\n\n本次展览汇聚湖北省内十余家文博单位的精品文物一百八十余件，以时间为主线，讲述从旧石器时代到明清时期荆楚大地上的文明演进。展览运用场景复原、数字投影与互动触摸屏，将考古发现转化为可感可知的历史叙事。\n\n特展需单独预约，票价30元。1.2米以下儿童免票，需由成人陪同。欢迎广大观众前来参观。',
        'NEWS', 1, 1, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 12 DAY), '10:30:00'), 968),
       ('编钟演奏恢复每日两场',
        '即日起，曾侯乙编钟演奏展示恢复每日10:30、14:30各一场。',
        '各位观众：\n\n曾侯乙编钟演奏展示自即日起恢复每日两场，时间为10:30、14:30，地点为综合馆一楼编钟演奏厅。\n\n每场演奏约15分钟，请提前10分钟入场。演奏期间请保持安静，将手机调至静音模式。\n\n如遇临时调整，以馆内公告为准。',
        'NOTICE', 0, 1, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '14:00:00'), 632),
       ('“博物馆里的二十四节气”社教活动招募',
        '面向8—12岁青少年，每场20组家庭，免费参与。',
        '“博物馆里的二十四节气”系列社教活动开始招募啦！\n\n活动将结合馆藏文物，带领孩子们认识节气物候、体验传统手作。本期主题为“霜降·柿柿如意”，包括展厅探秘、文物解读与手工体验三个环节。\n\n活动时间：本周六 14:00—16:00\n活动地点：综合馆三楼社教活动室\n招募对象：8—12岁青少年，每场20组家庭\n报名方式：关注官方公众号，点击“活动预约”报名。\n\n名额有限，先到先得。',
        'ACTIVITY', 0, 1, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '11:00:00'), 415),
       ('参观须知与交通指南',
        '开放时间、预约规则、入馆流程与公共交通信息。',
        '一、开放时间\n周二至周日 9:00—17:00（16:00停止入馆），周一闭馆（法定节假日除外）。\n\n二、预约规则\n观众需提前实名预约，可预约未来7天内时段。每个账号最多预约5人。预约成功后请按时间段到馆。\n\n三、入馆流程\n出示预约码/身份证 → 安检 → 核验入馆 → 参观。\n\n四、交通指南\n地址：湖北省武汉市武昌区东湖路160号。\n地铁：8号线省博湖北日报站A出口，步行约500米。\n公交：14路、402路、411路、552路、578路、701路、709路、810路至省博物馆站。\n\n五、咨询电话\n027-86794127（服务时间 9:00—17:00）。',
        'NOTICE', 0, 1, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 30 DAY), '09:30:00'), 2035);

-- ------------------------------------------------------------
-- 意见反馈
-- ------------------------------------------------------------
INSERT INTO feedback (user_id, type, content, contact, reply, status, replied_at, created_at)
SELECT u.id, 'PRAISE', '编钟演奏非常震撼，讲解员也很专业，孩子听得特别认真。感谢博物馆的用心！', '13800000003',
       '感谢您的认可！我们会继续提升讲解服务质量，欢迎再次参观。', 'REPLIED',
       TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '10:00:00'),
       TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '16:20:00')
FROM sys_user u WHERE u.username = 'visitor';

INSERT INTO feedback (user_id, type, content, contact, status, created_at)
SELECT u.id, 'SUGGESTION', '建议在预约页面增加“同行人”信息填写，方便一家人一起入馆。', '13800000004', 'PENDING',
       TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '09:15:00')
FROM sys_user u WHERE u.username = 'xiaoyu';

INSERT INTO feedback (user_id, type, content, contact, reply, status, replied_at, created_at)
SELECT u.id, 'COMPLAINT', '周末排队入馆时间较长，建议增加安检通道。', '13800000005',
       '您好，感谢您的建议。我们已在客流高峰时段增开安检通道，并优化预约时段分流，祝您参观愉快。', 'REPLIED',
       TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '15:00:00'),
       TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 7 DAY), '11:40:00')
FROM sys_user u WHERE u.username = 'wangwei';

INSERT INTO feedback (user_id, type, content, contact, status, created_at)
SELECT u.id, 'OTHER', '请问馆内可以寄存行李吗？', '13800000005', 'PENDING',
       TIMESTAMP(CURDATE(), '08:50:00')
FROM sys_user u WHERE u.username = 'wangwei';