# vibe-verse Server

`vibe-verse-server` 是 vibe-verse 的 Spring Boot 后端仓库。

当前只保留系统底座能力：

- 登录、注册、刷新 token、退出登录
- 用户管理、角色管理、权限目录
- 字典管理
- 安全审计日志
- 统一返回、统一异常、分页、基础配置
- Liquibase 数据库初始化
- SpringDoc OpenAPI / Swagger UI

## 技术栈

- Java 21
- Spring Boot 3.5
- MyBatis / MyBatis-Plus
- MySQL
- Liquibase
- Sa-Token
- SpringDoc OpenAPI
- Lombok
- Caffeine

## 目录结构

```text
src/main/java/com/fz/vibeverse
├─ common/       # 通用对象、分页、Mapper 基类、工具、Web 辅助
├─ config/       # Spring MVC、OpenAPI、MyBatis-Plus、缓存等配置
├─ exception/    # 统一返回与异常处理
├─ startup/      # 启动横幅、启动检查
└─ system/       # 认证、用户、角色、权限、字典
```

## 本地启动

准备环境：

- JDK 21
- MySQL 8.x 或兼容版本

本地私有配置放在 `src/main/resources/application-local.yml`，不要提交。

默认本地数据库为 `vibe_verse`：

```sql
create database if not exists vibe_verse
  default character set utf8mb4
  collate utf8mb4_unicode_ci;
```

启动：

```powershell
.\mvnw.cmd spring-boot:run
```

Swagger UI：

```text
http://localhost:8080/swagger-ui/index.html
```

## 常用命令

```powershell
.\mvnw.cmd test
.\mvnw.cmd clean package
.\mvnw.cmd clean package -DskipTests
```
