# 后端 · 湖北省博物馆参观预约系统

Spring Boot 3.5 + MyBatis-Plus 3.5 + MySQL + JWT + BCrypt。

## 启动

```bash
# 确保 MySQL 3306 已导入 museum_booking
mvn spring-boot:run

# 打包
mvn clean package -DskipTests
java -jar target/booking-1.0.0.jar
```

## 配置

- `src/main/resources/application.yml`：端口、3306 数据源、Jackson、MyBatis-Plus、JWT。
- 生产环境用 `SPRING_DATASOURCE_*`、`JWT_SECRET` 环境变量覆盖。

## 测试

```bash
mvn test
```

8 个测试：JWT 2 个、预约服务 5 个、上下文 1 个。

## 包结构

```text
com.museum.booking
├─ common/        Result、PageResult、BusinessException、GlobalExceptionHandler
├─ config/        Jackson、MyBatis-Plus、Web/CORS、JWT、密码
├─ controller/    游客端接口
│  └─ admin/      管理端接口
├─ dto/           请求体与校验
├─ entity/        6 张表实体
├─ job/           预约过期定时任务
├─ mapper/        MyBatis-Plus Mapper
├─ security/      JWT、UserContext、AuthInterceptor、@RequireRole
├─ service/       业务服务
└─ vo/            视图对象
```

## 核心设计

- 预约库存使用原子 SQL：`UPDATE ... WHERE booked_count + n <= capacity`。
- 预约与库存同事务；取消归还库存；核销/取消使用条件更新。
- JWT + 拦截器 + 方法级 `@RequireRole`。
- 统一 `Result<T>` 与全局异常处理，业务错误码 400/401/403/404/409/500。

完整系统说明见 `../../docs/系统说明.md`。