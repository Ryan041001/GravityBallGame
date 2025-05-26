package com.example.gravityballgame

import android.os.Bundle

/**
 * 第一关（简单难度）Activity
 */
class Level1Activity : LevelActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    
    override fun initializeLevelData() {
        // 设置关卡基本信息
        levelNumber = 1
        difficulty = 1
        timeLimit = 45  // 简单难度给予足够的时间
        levelTitle = "简单模式"
        
        // 设置起始位置（将在createLevelElements中更新为实际坐标）
        startX = 0f
        startY = 0f
    }
    
    override fun createLevelElements(screenWidth: Float, screenHeight: Float) {
        // 清空现有元素
        obstacles.clear()
        
        // 计算基础尺寸（简化计算方式，避免状态栏问题）
        val baseX = screenWidth * 0.1f  // 左边距10%
        val baseWidth = screenWidth * 0.8f  // 宽度80%
        val baseY = screenHeight * 0.1f  // 上边距10%
        val baseHeight = screenHeight * 0.8f  // 高度80%
        
        val obstacleHeight = screenHeight * 0.03f  // 略微增加障碍物高度，使其更明显
        val borderThickness = screenWidth * 0.02f  // 根据屏幕宽度调整边框厚度
        
        // 添加边框
        obstacles.add(Obstacle(0f, 0f, screenWidth, borderThickness, isTrap = false))
        obstacles.add(Obstacle(0f, screenHeight - borderThickness, screenWidth, screenHeight, isTrap = false))
        obstacles.add(Obstacle(0f, 0f, borderThickness, screenHeight, isTrap = false))
        obstacles.add(Obstacle(screenWidth - borderThickness, 0f, screenWidth, screenHeight, isTrap = false))
        
        // 第一个障碍物（上方）
        obstacles.add(Obstacle(
            baseX + baseWidth * 0.2f,
            baseY + baseHeight * 0.3f,
            baseX + baseWidth * 0.8f,
            baseY + baseHeight * 0.3f + obstacleHeight,
            isTrap = false
        ))
        
        // 第二个障碍物（左下）
        obstacles.add(Obstacle(
            baseX,
            baseY + baseHeight * 0.6f,
            baseX + baseWidth * 0.3f,
            baseY + baseHeight * 0.6f + obstacleHeight,
            isTrap = false
        ))
        
        // 第三个障碍物（右下）
        obstacles.add(Obstacle(
            baseX + baseWidth * 0.7f,
            baseY + baseHeight * 0.6f,
            baseX + baseWidth,
            baseY + baseHeight * 0.6f + obstacleHeight,
            isTrap = false
        ))
        
//        // 添加一个小陷阱（增加一点小挑战）
//        obstacles.add(Obstacle(
//            baseX + baseWidth * 0.45f,
//            baseY + baseHeight * 0.7f,
//            baseX + baseWidth * 0.55f,
//            baseY + baseHeight * 0.7f + obstacleHeight,
//            isTrap = true
//        ))
        
        // 设置终点
        goal = Goal(
            baseX + baseWidth * 0.4f,
            baseY + baseHeight * 0.8f,
            baseX + baseWidth * 0.6f,
            baseY + baseHeight * 0.9f
        )
        
        // 设置起点位置
        startX = screenWidth * 0.5f  // 正中间
        startY = screenHeight * 0.15f  // 靠上位置，更容易的起始点
    }
}