# GravityBallGame - 重力球游戏

GravityBallGame 是一款基于 Android 平台的休闲益智游戏。玩家通过倾斜设备控制小球的滚动，躲避障碍物，最终到达终点。游戏包含多种模式，提供不同的挑战和乐趣。

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

*   **编程语言**：Kotlin
*   **核心框架**：Android SDK
*   **UI 设计**：XML 布局, Material Components for Android
*   **数据存储**：Room Persistence Library (SQLite)
*   **异步处理**：Kotlin Coroutines
*   **依赖管理**：Gradle

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

## 项目结构 (主要目录)

```
app
├── src
│   ├── main
│   │   ├── java/com/example/gravityballgame  # Kotlin 源代码
│   │   │   ├── data                          # 数据层 (Room, DAO, Entity)
│   │   │   ├── ui                            # UI 相关 (Activity, Adapter)
│   │   │   └── util                          # 工具类
│   │   ├── res                             # 资源文件
│   │   │   ├── drawable                      # 图片、图标、形状等
│   │   │   ├── layout                        # 布局文件 (XML)
│   │   │   ├── menu                          # 菜单文件
│   │   │   ├── mipmap                        # 应用图标
│   │   │   ├── navigation                    # 导航图 (如果使用 Navigation Component)
│   │   │   └── values                        # 颜色、字符串、样式、主题等
│   │   └── AndroidManifest.xml             # 应用清单文件
│   └── test                                # 单元测试
│   └── androidTest                         # 仪器测试
└── build.gradle.kts                        # app 模块的 Gradle 构建脚本

build.gradle.kts                            # 项目级的 Gradle 构建脚本
settings.gradle.kts                         # Gradle 设置文件
gradle.properties                           # Gradle 属性配置
```

## 未来改进方向

*   **完善自定义模式**：开放并优化自定义关卡编辑器，允许玩家分享和挑战他人设计的关卡。
*   **增加更多关卡元素**：例如移动障碍物、传送门、加速带等，增加游戏趣味性。
*   **优化性能**：针对低端设备进行性能优化，确保流畅的游戏体验。
*   **音效和背景音乐**：添加合适的音效和背景音乐，提升游戏沉浸感。
*   **社交功能**：例如好友排行榜、挑战邀请等。
*   **多语言支持**：适配不同国家和地区的语言。

## 贡献

欢迎对本项目进行贡献！如果您有任何建议或发现任何问题，请随时提交 Issue 或 Pull Request。
