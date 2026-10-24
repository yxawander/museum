# 数据库说明

## 文件

| 文件 | 说明 |
| --- | --- |
| `schema.sql` | 建库、创建应用账号、建 6 张表、索引与外键 |
| `data.sql` | 初始化用户、展览、时段、预约、公告、反馈 |

## 快速导入（MySQL 3306）

确保 MySQL 3306 已启动，在项目根目录执行：

```bash
mysql -uroot -p < database/schema.sql
mysql -uroot -p < database/data.sql
```

如果 MySQL 客户端当前目录已经是 `database`：

```bash
mysql -uroot -p < schema.sql
mysql -uroot -p < data.sql
```

> Windows PowerShell 不建议直接使用 `<` 重定向，可使用：
> `cmd /c "mysql.exe -uroot -p --default-character-set=utf8mb4 < schema.sql"`

## 默认账号

数据库应用账号：`museum` / `museum123`（仅本地开发）。

系统账号：

| 角色 | 账号 | 密码 |
| --- | --- | --- |
| 管理员 | `admin` | `Admin@123` |
| 检票员 | `staff` | `Staff@123` |
| 观众 | `visitor` | `Visitor@123` |
| 观众 | `xiaoyu` | `Visitor@123` |
| 观众 | `wangwei` | `Visitor@123` |

密码在 `data.sql` 中以 BCrypt 密文保存。

## 数据量

| 表 | 初始化 |
| --- | ---: |
| `sys_user` | 5 |
| `exhibition` | 6 |
| `visit_slot` | 252（今天前 3 天至未来 13 天，每天 3 时段，跳过周一） |
| `reservation` | 6（待参观/已入馆/已取消/已过期） |
| `announcement` | 5 |
| `feedback` | 4 |

## 重置

```bash
mysql -uroot -p < schema.sql
mysql -uroot -p < data.sql
```

`schema.sql` 会先 `DROP TABLE`，请勿在生产环境直接执行。

## 数据一致性

`visit_slot.booked_count` 是预约人数的冗余字段，正常业务通过原子 SQL 维护：

```sql
-- 预约扣减
UPDATE visit_slot
SET booked_count = booked_count + ?
WHERE id = ? AND status = 'OPEN' AND booked_count + ? <= capacity;

-- 取消归还
UPDATE visit_slot
SET booked_count = GREATEST(booked_count - ?, 0)
WHERE id = ?;
```

如需对账：

```sql
UPDATE visit_slot s
SET s.booked_count = (
  SELECT COALESCE(SUM(r.ticket_count), 0)
  FROM reservation r
  WHERE r.slot_id = s.id AND r.status IN ('CONFIRMED', 'CHECKED_IN')
);
```

## 注意

- 字符集必须为 `utf8mb4`，否则中文/emoji 可能乱码。
- `schema.sql` 中的 `CREATE USER` 需要 root 权限；无权限时删除该段，改用已有账号。
- 生产环境请修改 `museum123`、开启定期备份、限制数据库端口访问。