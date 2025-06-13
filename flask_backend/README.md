# 重力球游戏 - Flask 后端服务

这是一个基于 Flask 的简单后端服务，用于支持重力球游戏的多设备排行榜联动功能。

## 功能特性

- 用户注册和登录
- 成绩上传和存储
- 多设备排行榜同步
- 个人成绩查询
- 游戏统计信息
- RESTful API 接口
- SQLite 数据库存储
- CORS 跨域支持

## 快速开始

### 方法一：使用启动脚本（推荐）

1. 确保已安装 Python 3.7+
2. 运行启动脚本：
   ```bash
   python start_server.py
   ```
   
   脚本会自动：
   - 检查 Python 版本
   - 安装所需依赖
   - 初始化数据库
   - 启动 Flask 服务

### 方法二：手动安装

1. 安装依赖：
   ```bash
   pip install -r requirements.txt
   ```

2. 初始化数据库：
   ```bash
   python init_db.py
   ```

3. 启动服务：
   ```bash
   python app.py
   ```

## API 接口文档

### 基础信息
- 服务地址：`http://localhost:5000`
- 数据格式：JSON
- 编码：UTF-8

### 接口列表

#### 1. 健康检查
```
GET /api/health
```
**响应：**
```json
{
  "status": "ok",
  "message": "Flask backend is running",
  "timestamp": "2024-01-01T12:00:00"
}
```

#### 2. 用户注册
```
POST /api/register
```
**请求体：**
```json
{
  "username": "player1",
  "password": "password123"
}
```
**响应：**
```json
{
  "success": true,
  "message": "用户注册成功",
  "user_id": 1
}
```

#### 3. 用户登录
```
POST /api/login
```
**请求体：**
```json
{
  "username": "player1",
  "password": "password123"
}
```
**响应：**
```json
{
  "success": true,
  "message": "登录成功",
  "user_id": 1,
  "username": "player1"
}
```

#### 4. 上传成绩
```
POST /api/upload_score
```
**请求体：**
```json
{
  "user_id": 1,
  "level_type": "challenge",
  "level_number": 1,
  "score": 1500,
  "time_seconds": 45.5
}
```
**响应：**
```json
{
  "success": true,
  "message": "成绩上传成功",
  "score_id": 1
}
```

#### 5. 获取排行榜
```
GET /api/leaderboard?level_type=challenge&level_number=1&limit=10
```
**参数：**
- `level_type`: 关卡类型（challenge, custom, standard）
- `level_number`: 关卡编号（可选）
- `limit`: 返回数量限制（默认10）

**响应：**
```json
{
  "success": true,
  "data": [
    {
      "rank": 1,
      "username": "player1",
      "score": 1500,
      "time_seconds": 45.5,
      "level_type": "challenge",
      "level_number": 1,
      "created_at": "2024-01-01T12:00:00"
    }
  ]
}
```

#### 6. 获取用户成绩
```
GET /api/user_scores?user_id=1&level_type=challenge&limit=20
```
**参数：**
- `user_id`: 用户ID
- `level_type`: 关卡类型（可选）
- `limit`: 返回数量限制（默认20）

**响应：**
```json
{
  "success": true,
  "data": [
    {
      "score": 1500,
      "time_seconds": 45.5,
      "level_type": "challenge",
      "level_number": 1,
      "created_at": "2024-01-01T12:00:00"
    }
  ]
}
```

#### 7. 获取统计信息
```
GET /api/stats
```
**响应：**
```json
{
  "success": true,
  "data": {
    "total_users": 10,
    "total_scores": 50,
    "avg_score": 1250.5,
    "best_score": 2000
  }
}
```

## 数据库结构

### 用户表 (users)
- `id`: 主键
- `username`: 用户名（唯一）
- `password`: 密码（哈希存储）
- `created_at`: 创建时间

### 成绩表 (scores)
- `id`: 主键
- `user_id`: 用户ID（外键）
- `level_type`: 关卡类型
- `level_number`: 关卡编号
- `score`: 分数
- `time_seconds`: 用时（秒）
- `created_at`: 创建时间

## 测试

运行测试脚本：
```bash
python test_api.py
```

测试脚本会验证所有 API 接口的功能。

## 配置说明

### 环境变量
- `FLASK_ENV`: 运行环境（development/production）
- `DATABASE_URL`: 数据库连接字符串（可选，默认使用 SQLite）

### 安全注意事项
- 生产环境中应使用更强的密码哈希算法
- 建议使用 JWT 或 Session 进行用户认证
- 应配置适当的 CORS 策略
- 建议使用 HTTPS 协议

## 部署

### 本地开发
服务默认运行在 `http://localhost:5000`

### 生产部署
1. 使用 Gunicorn 或 uWSGI 作为 WSGI 服务器
2. 配置 Nginx 作为反向代理
3. 使用 PostgreSQL 或 MySQL 替代 SQLite
4. 配置日志和监控

## 故障排除

### 常见问题

1. **端口被占用**
   - 修改 `app.py` 中的端口号
   - 或终止占用端口的进程

2. **依赖安装失败**
   - 确保 Python 版本 >= 3.7
   - 使用虚拟环境：`python -m venv venv`
   - 激活虚拟环境后再安装依赖

3. **数据库错误**
   - 删除 `game_scores.db` 文件
   - 重新运行 `python init_db.py`

4. **CORS 错误**
   - 检查 Android 应用的请求地址
   - 确保 Flask-CORS 正确配置

## 许可证

本项目仅用于学习和演示目的。