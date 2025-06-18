# GravityBallGame - 重力球游戏

GravityBallGame 是一款基于 Android 平台的休闲益智游戏，采用前后端分离架构。玩家通过倾斜设备控制小球的滚动，躲避障碍物，最终到达终点。游戏包含多种模式，提供不同的挑战和乐趣，并支持多设备排行榜联动功能。

## 主要功能

*   **多种游戏模式**：
    *   **标准模式**：包含简单、中等、困难三个等级的预设关卡。
    *   **自定义模式**：玩家可以发挥创意，设计自己的游戏关卡。
    *   **极限迷宫挑战**：挑战精心设计的复杂迷宫，考验玩家的技巧和耐心。
*   **重力感应控制**：利用设备的重力传感器控制小球的移动，带来真实的物理体验。
*   **用户系统**：支持用户注册和登录，记录玩家的最佳成绩。
*   **排行榜**：展示极限迷宫挑战模式下的最佳成绩排名。
*   **现代化界面**：采用 Material Design 风格，界面美观、操作流畅。

## 技术栈

### Android 客户端
*   **编程语言**：Kotlin
*   **核心框架**：Android SDK
*   **UI 设计**：XML 布局, Material Components for Android
*   **数据存储**：Room Persistence Library (SQLite) + SharedPreferences
*   **异步处理**：Kotlin Coroutines
*   **依赖管理**：Gradle
*   **传感器**：重力传感器 (Accelerometer)
*   **网络通信**：原生 HttpURLConnection

### Flask 后端
*   **编程语言**：Python 3.7+
*   **Web 框架**：Flask 2.2.5
*   **数据库**：SQLite (Flask-SQLAlchemy)
*   **跨域支持**：Flask-CORS
*   **密码加密**：Werkzeug Security
*   **API 风格**：RESTful API

## 如何运行

1.  **环境准备**：
    *   安装 Android Studio (推荐最新版本)。
    *   配置 Android SDK (确保已安装相应的 API Level)。
2.  **克隆项目**：
    ```bash
    git clone <项目仓库地址>
    ```
3.  **导入项目**：
    *   打开 Android Studio。
    *   选择 "Open an existing Android Studio project"。
    *   选择克隆到本地的项目文件夹。
4.  **构建项目**：
    *   等待 Gradle 同步完成。
    *   点击菜单栏的 "Build" -> "Make Project"。
5.  **运行项目**：
    *   连接 Android 设备或启动 Android 模拟器。
    *   点击 Android Studio 工具栏的 "Run 'app'" 按钮 (绿色三角形图标)。

## 项目架构

### 整体架构
本项目采用 **前后端分离架构**，包含以下两个主要部分：

1. **Android 客户端** - 游戏主体，提供完整的游戏体验
2. **Flask 后端服务** - 提供用户管理和排行榜功能

### Android 客户端架构

#### 核心组件
- **MainActivity** - 主入口，提供游戏模式选择和用户管理
- **LevelActivity** - 抽象基类，定义关卡通用逻辑
- **GameView** - 游戏核心视图，负责渲染、物理计算和交互
- **具体关卡类** - Level1Activity, Level2Activity, Level3Activity 等

#### 数据层
- **User Entity** - 用户数据模型 (Room)
- **UserSessionManager** - 用户会话管理 (SharedPreferences)
- **NetworkService** - 网络通信服务

#### 游戏核心
- **重力感应控制** - 基于 SensorEventListener
- **物理引擎** - 自定义实现的简单物理系统
- **关卡系统** - 支持标准关卡、自定义关卡、挑战模式

### Flask 后端架构

#### API 接口
- **用户管理** - 注册、登录、用户信息
- **成绩管理** - 成绩上传、查询、排行榜
- **健康检查** - 服务状态监控

#### 数据模型
- **User Model** - 用户信息存储
- **Score Model** - 成绩记录存储

### 项目目录结构

```
GravityBallGame/
├── app/                                    # Android 应用
│   ├── src/main/
│   │   ├── java/com/example/gravityballgame/
│   │   │   ├── MainActivity.kt             # 主Activity
│   │   │   ├── LevelActivity.kt            # 关卡基类
│   │   │   ├── GameView.kt                 # 游戏视图
│   │   │   ├── Level*Activity.kt           # 具体关卡
│   │   │   ├── CustomLevelActivity.kt      # 自定义关卡
│   │   │   ├── LevelChallengeActivity.kt   # 挑战模式
│   │   │   ├── LeaderboardActivity.kt      # 排行榜
│   │   │   ├── Ball.kt                     # 球体模型
│   │   │   ├── data/                       # 数据层
│   │   │   │   ├── User.kt                 # 用户实体
│   │   │   │   └── UserSessionManager.kt   # 会话管理
│   │   │   └── network/                    # 网络层
│   │   │       └── NetworkService.kt       # 网络服务
│   │   ├── res/                            # 资源文件
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── flask_backend/                          # Flask 后端
│   ├── app.py                              # 主应用文件
│   ├── init_db.py                          # 数据库初始化
│   ├── start_server.py                     # 服务启动脚本
│   ├── requirements.txt                    # Python 依赖
│   ├── game_data.db                        # SQLite 数据库
│   └── README.md                           # 后端文档
└── README.md                               # 项目总文档
```

## 系统特性

### 游戏功能
- **多种游戏模式**：标准模式（3个难度等级）、自定义关卡设计、极限迷宫挑战
- **重力感应控制**：真实的物理体验，通过倾斜设备控制小球
- **关卡编辑器**：支持自定义关卡设计，包括障碍物、陷阱、目标点设置
- **计时系统**：精确的游戏计时和成绩记录

### 用户系统
- **本地用户管理**：基于 Room 数据库的本地用户存储
- **远程同步**：与 Flask 后端的用户数据同步
- **会话管理**：持久化的用户登录状态

### 网络功能
- **排行榜系统**：多设备成绩同步和排名
- **成绩上传**：自动上传最佳成绩到服务器
- **跨平台支持**：支持多设备数据共享

### 技术亮点
- **前后端分离**：Android 客户端 + Flask 后端的现代化架构
- **RESTful API**：标准化的 API 接口设计
- **协程异步处理**：流畅的用户体验，无阻塞操作
- **传感器集成**：充分利用 Android 设备的硬件特性

## 部署说明

### Android 客户端部署
1. 使用 Android Studio 打开项目
2. 配置 SDK 和依赖
3. 修改 NetworkService 中的服务器地址
4. 编译并安装到设备

### Flask 后端部署
1. 安装 Python 3.7+ 环境
2. 进入 `flask_backend` 目录
3. 运行启动脚本：`python start_server.py`
4. 或手动安装依赖并启动：
   ```bash
   pip install -r requirements.txt
   python init_db.py
   python app.py
   ```

## 未来改进方向

*   **完善自定义模式**：开放并优化自定义关卡编辑器，允许玩家分享和挑战他人设计的关卡
*   **增加更多关卡元素**：例如移动障碍物、传送门、加速带等，增加游戏趣味性
*   **优化性能**：针对低端设备进行性能优化，确保流畅的游戏体验
*   **音效和背景音乐**：添加合适的音效和背景音乐，提升游戏沉浸感
*   **社交功能**：例如好友排行榜、挑战邀请等
*   **多语言支持**：适配不同国家和地区的语言
*   **云端关卡分享**：建立关卡分享平台，支持玩家上传和下载自定义关卡

## 贡献

欢迎对本项目进行贡献！如果您有任何建议或发现任何问题，请随时提交 Issue 或 Pull Request。
