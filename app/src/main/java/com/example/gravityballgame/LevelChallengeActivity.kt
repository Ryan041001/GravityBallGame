package com.example.gravityballgame

import android.content.Intent
import android.graphics.RectF
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
// kotlin.math.abs 已内置在 Kotlin 标准库中，无需显式导入
// 如有特殊需求可添加 import kotlin.math.abs
import com.example.gravityballgame.data.AppDatabase
import com.example.gravityballgame.data.User
import com.example.gravityballgame.data.UserSessionManager

class LevelChallengeActivity:
LevelActivity() {
    
    // 存储迷宫路径段落的集合
    private val pathSegments = mutableListOf<Obstacle>()
    
    // 添加数据库和用户会话管理器
    private lateinit var userDao: com.example.gravityballgame.data.UserDao
    private lateinit var userSessionManager: UserSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化数据库和用户会话管理器
        userDao = AppDatabase.getDatabase(this).userDao()
        userSessionManager = UserSessionManager(this)
    }

    override fun initializeLevelData() {
        levelNumber = 5 // 挑战模式专用标识符
        difficulty = 5  // 最高难度等级
        timeLimit = 180 // 设置较长时间限制，适应复杂迷宫路径
        levelTitle = "极限迷宫挑战"

        // 起点坐标初始化为0，将在createLevelElements方法中动态设置
        startX = 0f
        startY = 0f
    }

    override fun createLevelElements(screenWidth: Float, screenHeight: Float) {
        obstacles.clear() // 清除已有障碍物
        pathSegments.clear() // 清除已有路径段落

        val pathThickness = screenWidth * 0.15f // 通道宽度，较大以提升可玩性
        val trapCellSize = screenWidth * 0.06f  // 陷阱单元格尺寸
        val pathCornerRadius = 45f // 路径拐角半径，提升视觉美感

        // 1. 定义相对路径点坐标 (基于屏幕百分比位置，增强适配性)
        val waypointsRelative = listOf(
            Pair(0.1f, 0.15f),  // 起点，位于左上方区域
            Pair(0.85f, 0.15f), // 第一段：水平向右延伸
            Pair(0.85f, 0.45f), // 第二段：垂直向下延伸
            Pair(0.15f, 0.45f), // 第三段：水平向左延伸
            Pair(0.15f, 0.75f), // 第四段：垂直向下延伸
            Pair(0.8f, 0.75f),  // 第五段：水平向右延伸
            Pair(0.8f, 0.85f),  // 第六段：垂直向下延伸
            Pair(0.9f, 0.85f)   // 终点，位于右下方区域
        )

        // 将相对坐标转换为实际屏幕坐标
        val waypoints = waypointsRelative.map {
            Pair(it.first * screenWidth, it.second * screenHeight)
        }

        // 设置小球起点坐标
        startX = screenWidth * 0.1f  // 屏幕左上角
        startY = screenHeight * 0.15f

        // 设置终点区域 (略大于路径宽度，便于小球进入)
        val goalPoint = waypoints.last()
        val goalSize = pathThickness * 1.2f // 终点区域尺寸
        goal = Goal(
            goalPoint.first - goalSize / 2, goalPoint.second - goalSize / 2,
            goalPoint.first + goalSize / 2, goalPoint.second + goalSize / 2
        )

        // 2. 创建迷宫安全路径
        for (i in 0 until waypoints.size - 1) {
            val p1 = waypoints[i]
            val p2 = waypoints[i+1]

            // 计算路径段边界，考虑路径厚度
            val segmentLeft = min(p1.first, p2.first) - pathThickness / 2
            val segmentTop = min(p1.second, p2.second) - pathThickness / 2
            val segmentRight = max(p1.first, p2.first) + pathThickness / 2
            val segmentBottom = max(p1.second, p2.second) + pathThickness / 2

            val pathSegment = Obstacle(segmentLeft, segmentTop, segmentRight, segmentBottom, false)
            pathSegment.setCornerRadius(pathCornerRadius) // 应用圆角效果
            pathSegments.add(pathSegment)
        }
        // 路径段为安全区域，不属于障碍物，但需要单独绘制
        // 使用专用集合管理路径段，与障碍物分开处理

        // 3. 使用陷阱填充非路径区域
        val numCols = (screenWidth / trapCellSize).toInt() + 1
        val numRows = (screenHeight / trapCellSize).toInt() + 1

        for (r in 0 until numRows) {
            for (c in 0 until numCols) {
                val cellLeft = c * trapCellSize
                val cellTop = r * trapCellSize
                val cellRight = cellLeft + trapCellSize
                val cellBottom = cellTop + trapCellSize
                val cellRect = RectF(cellLeft, cellTop, cellRight, cellBottom)
                // 获取单元格中心点坐标，用于安全区判定
                val cellCenterX = cellRect.centerX()
                val cellCenterY = cellRect.centerY()

                var isSafeZone = false // 标记单元格是否位于安全区内

                // 检测单元格中心是否在任意路径段内
                for (pathSegment in pathSegments) {
                    if (pathSegment.getBounds().contains(cellCenterX, cellCenterY)) {
                        isSafeZone = true
                        break
                    }
                }

                // 检测单元格中心是否在终点区域内
                if (!isSafeZone && goal != null && goal!!.getBounds().contains(cellCenterX, cellCenterY)) {
                    isSafeZone = true
                }

                // 检测单元格中心是否在起点安全缓冲区内
                val startBuffer = pathThickness * 1.5f // 起点周围额外安全区域，避免开局困难
                val startZone = RectF(startX - startBuffer, startY - startBuffer, startX + startBuffer, startY + startBuffer)
                if (!isSafeZone && startZone.contains(cellCenterX, cellCenterY)) {
                    isSafeZone = true
                }

                // 根据安全区判定创建对应类型的单元格
                if (!isSafeZone) {
                    val trap = Obstacle(cellLeft, cellTop, cellRight, cellBottom, true)
                    trap.setCornerRadius(0f) // 陷阱使用直角设计
                    obstacles.add(trap)
                } else {
                    // 安全区域单元格（当前未启用）
                    val safeCell = Obstacle(cellLeft, cellTop, cellRight, cellBottom, false)
                    safeCell.setCornerRadius(45f) // 安全区使用圆角设计
                    // 已注释掉，未添加到路径集合
                    // pathSegments.add(safeCell)
                }
            }
        }
    }
    
    // 创建并初始化游戏视图
    override fun createGameView() {
        // 调用父类方法进行基础初始化
        super.createGameView()
        
        // 路径段渲染功能（当前已注释）
        // gameView.addPathsForDrawing(pathSegments)
    }
    
    /**
     * 重写胜利对话框，添加成绩记录功能
     */
    override fun showWinDialog() {
        if (!isFinishing && !isDestroyed) {
            // 计算完成时间
            val completionTime = System.currentTimeMillis() - gameView.gameStartTime
            
            // 保存成绩
            saveCompletionTime(completionTime)
            
            // 格式化时间显示
            val minutes = completionTime / 60000
            val seconds = (completionTime % 60000) / 1000
            val millis = completionTime % 1000
            val timeText = String.format("%02d:%02d.%03d", minutes, seconds, millis)
            
            AlertDialog.Builder(this)
                .setTitle("挑战完成！")
                .setMessage("恭喜完成极限迷宫挑战！\n\n完成时间：$timeText\n\n是否查看排行榜？")
                .setPositiveButton("查看排行榜") { _, _ ->
                    val intent = Intent(this, LeaderboardActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("再次挑战") { _, _ -> resetGame() }
                .setNeutralButton("返回菜单") { _, _ -> returnToMainMenu() }
                .setCancelable(false)
                .show()
        }
    }
    
    /**
     * 保存完成时间到数据库
     */
    private fun saveCompletionTime(completionTime: Long) {
        // 检查用户是否已登录
        if (userSessionManager.isLoggedIn()) {
            val userId = userSessionManager.getUserId()
            val username = userSessionManager.getUsername() ?: return
            
            // 使用协程在后台线程更新数据库
            lifecycleScope.launch {
                try {
                    // 获取当前用户
                    val currentUser = userDao.getUserById(userId)
                    if (currentUser != null) {
                        // 检查是否是新的最佳成绩
                        val shouldUpdate = currentUser.bestChallengeTime == 0L || 
                                         completionTime < currentUser.bestChallengeTime
                        
                        if (shouldUpdate) {
                            val updatedUser = currentUser.copy(bestChallengeTime = completionTime)
                            userDao.updateUser(updatedUser)
                            
                            // 更新会话中的用户信息
                            userSessionManager.saveUserLoginSession(userId, username)
                            
                            runOnUiThread {
                                Toast.makeText(this@LevelChallengeActivity, 
                                    "新的最佳成绩已保存！", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@LevelChallengeActivity, 
                            "保存成绩时出错：${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "请先登录以保存成绩", Toast.LENGTH_SHORT).show()
        }
    }
}