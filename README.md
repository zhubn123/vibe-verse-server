# AetherFlow Server

AetherFlow Server 是 AetherFlow 平台的后端仓库，当前以 Spring Boot 单体应用承载平台基础能力和 WMS 业务接口。

AetherFlow 不是单一 WMS 项目。当前代码中最完整的业务主线是 `system` 和 `wms`，后续可以继续演进 ERP / WCS / TMS 等模块，但是否落地以当前代码事实为准。

## 当前能力

- 认证与权限：登录、刷新 token、用户管理、角色管理、权限校验。
- WMS 基础资料：仓库、区域、库位、物料。
- WMS 业务单据：入库单、出库单、库存调整单。
- 库存能力：单据确认驱动库存增减、出库可用库存校验、库存流水。
- 工作台数据：仓库筛选、库存预警、近期出入库趋势和作业动态。
- 接口文档：通过 SpringDoc OpenAPI 暴露 Swagger UI。

## 技术栈

- Java 21
- Spring Boot 3.5
- MyBatis / MyBatis-Plus
- MySQL
- Redis
- Liquibase
- Sa-Token
- SpringDoc OpenAPI
- Lombok
- MapStruct Plus
- Caffeine

## 目录结构

```text
src/main/java/com/berlin/aetherflow
├─ common/       # 通用工具、基础对象、分页等
├─ config/       # Spring MVC、鉴权、基础设施配置
├─ exception/    # 统一返回与异常处理
├─ system/       # 认证、用户、角色、权限等平台能力
└─ wms/          # WMS 业务模块
```

当前 WMS 仍采用模块级技术分层：

```text
wms
├─ constant
├─ controller
├─ domain
│  ├─ bo
│  ├─ entity
│  ├─ query
│  └─ vo
├─ mapper
├─ service
│  └─ impl
└─ support
```

新增 WMS 能力应继续落在这套结构中，除非任务明确要求目录重构。

## 本地启动

### 1. 准备环境

- JDK 21
- MySQL 8.x 或兼容版本
- Redis 6.x 或兼容版本

### 2. 配置本地参数

复制本地配置模板：

```powershell
Copy-Item src/main/resources/application-local.example.yml src/main/resources/application-local.yml
```

按本机环境修改：

```yaml
local:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/aether_flow?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
    username: root
    password: 123456
  redis:
    host: 127.0.0.1
    port: 6379
    password:
    database: 0
```

本地私有配置不要提交。

### 3. 创建数据库

先创建空库：

```sql
create database if not exists aether_flow
  default character set utf8mb4
  collate utf8mb4_unicode_ci;
```

应用启动时会通过 Liquibase 执行 `src/main/resources/db/changelog/db.changelog-master.yaml`。

### 4. 启动应用

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

macOS / Linux:

```bash
./mvnw spring-boot:run
```

默认服务地址：

```text
http://localhost:8080
```

Swagger UI：

```text
http://localhost:8080/swagger-ui/index.html
```

## 常用命令

```powershell
# 运行测试
.\mvnw.cmd test

# 编译打包
.\mvnw.cmd clean package

# 跳过测试打包
.\mvnw.cmd clean package -DskipTests
```

## 数据库变更约定

- 新数据库变更使用 Liquibase 增量文件。
- 已纳入 `db.changelog-master.yaml` 的历史 changeset 默认不回写修改。
- 日常模块变更优先追加到 `src/main/resources/db/changelog/modules/*.yaml` 或模块对应 SQL。
- 不要直接修改 `sql/aether-flow.sql` 来替代 Liquibase 增量变更。

## 接口约定

- 后端统一返回 `Result<?>` 或 `Result<T>`。
- WMS 接口统一放在 `/api/wms/**`。
- 列表查询优先使用 `GET` + query 参数；历史接口例外需尊重现状。
- 新增资源使用 `POST`，修改资源使用 `PUT`，删除资源使用 `DELETE`。
- 单据确认等动作型接口优先使用 `POST /{id}/actions`。
- Controller 只接收参数、触发校验、调用 service、返回结果；业务规则放在 service。

## 库存规则

当前库存采用“单据驱动库存”：

- 创建草稿：只保存单据和明细，不改库存。
- 编辑草稿：只改单据和明细，不改库存。
- 删除草稿：允许，不改库存。
- 确认单据：统一改库存并写库存流水。
- 已确认单据：不允许编辑，不允许删除。

库存增减必须统一经过：

```java
inventoryService.applyStockChanges(List<StockChangeBo> changes)
```

不要在 controller 或具体单据 service 中绕过库存服务直接改库存。

## 与前端联调

前端仓库是同级独立仓库 `AetherFlow-web`。本地前端默认通过 Vite 代理把 `/api` 转发到 `http://localhost:8080`。

启动顺序通常为：

1. 启动 MySQL 和 Redis。
2. 启动 `AetherFlow-server`。
3. 启动 `AetherFlow-web`。

## 相关文档

- 根目录平台规则：`../AGENTS.md`
- Codex 协作规则：`../CODEX.md`
- 平台定位：`../docs/codex/platform.md`
- WMS 开发规则：`../docs/codex/wms-dev.md`
- 库存规则：`../docs/codex/inventory-rule.md`
