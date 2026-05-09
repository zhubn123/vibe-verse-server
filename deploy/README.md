# Docker 一键部署

部署入口在本仓库 GitHub Actions：

```text
Actions -> Deploy Docker -> Run workflow
```

该 workflow 会自动完成：

1. 构建 `vibe-verse-server` Docker 镜像。
2. 拉取 `zhubn123/vibe-verse-web` 并构建前端 Docker 镜像。
3. 上传镜像和 `docker-compose.yml` 到服务器 `/srv/vibe-verse/docker`。
4. 使用 Docker Compose 启动 MySQL、后端和前端。

## GitHub Secrets

复用服务器 SSH 配置：

```text
SERVER_HOST
SERVER_USER
SERVER_PORT
SERVER_SSH_KEY
```

新增项目专用配置：

```text
VIBE_VERSE_DB_PASSWORD
VIBE_VERSE_MYSQL_ROOT_PASSWORD
VIBE_VERSE_AUTH_REFRESH_TOKEN_SECRET
```

可选：

```text
VIBE_VERSE_DB_USERNAME      # 默认 vibe_verse
VIBE_VERSE_WEB_PORT         # 默认 18080
VIBE_VERSE_SERVER_PORT      # 默认 18081
```

## 端口与容器

默认宿主机端口：

```text
18080 -> 前端
18081 -> 后端
```

容器、网络、卷都使用 `vibe-verse-*` 命名，避免和旧项目冲突。

服务器只需要提前装好 Docker 和 Docker Compose。
