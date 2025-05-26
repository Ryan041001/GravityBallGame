package com.example.gravityballgame

import android.os.Bundle

/**
 * 第二关（中等难度）Activity
 */
class Level2Activity : LevelActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    
    override fun initializeLevelData() {
        // 设置关卡基本信息
        levelNumber = 2
        difficulty = 2
        timeLimit = 35
        levelTitle = "中等模式"
        
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
        
        val wallThickness = screenWidth * 0.05f  // 减小墙体厚度
        val trapSize = screenWidth * 0.06f  // 减小陷阱大小
        val borderThickness = screenWidth * 0.02f  // 根据屏幕宽度调整边框厚度
        
        // 添加边框
        obstacles.add(Obstacle(0f, 0f, screenWidth, borderThickness, isTrap = false))
        obstacles.add(Obstacle(0f, screenHeight - borderThickness, screenWidth, screenHeight, isTrap = false))
        obstacles.add(Obstacle(0f, 0f, borderThickness, screenHeight, isTrap = false))
        obstacles.add(Obstacle(screenWidth - borderThickness, 0f, screenWidth, screenHeight, isTrap = false))
        
        // 上方横向墙
        obstacles.add(Obstacle(
            baseX + baseWidth * 0.2f,
            baseY + baseHeight * 0.25f,
            baseX + baseWidth * 0.8f,
            baseY + baseHeight * 0.25f + wallThickness,
            isTrap = false
        ))
        
        // 左侧斜向墙
        obstacles.add(Obstacle(
            baseX + baseWidth * 0.2f,
            baseY + baseHeight * 0.25f,
            baseX + baseWidth * 0.2f + wallThickness,
            baseY + baseHeight * 0.61f,
            isTrap = false
        ))
        
        // 中间横向墙 - 分成两段，中间留空
        obstacles.add(Obstacle(
            baseX + baseWidth * 0.2f,
            baseY + baseHeight * 0.6f,
            baseX + baseWidth * 0.4f,
            baseY + baseHeight * 0.6f + wallThickness,
            isTrap = false
        ))
        
        obstacles.add(Obstacle(
            baseX + baseWidth * 0.6f,
            baseY + baseHeight * 0.6f,
            baseX + baseWidth * 0.8f,
            baseY + baseHeight * 0.6f + wallThickness,
            isTrap = false
        ))

        // 添加陷阱1 (中间通道)
        obstacles.add(Obstacle(
            baseX + baseWidth * 0.45f,
            baseY + baseHeight * 0.8f - trapSize * 2,
            baseX + baseWidth * 0.55f,
            baseY + baseHeight * 0.8f + trapSize * 2,
            isTrap = true
        ))
        
        // 添加陷阱2 (右上角)
        obstacles.add(Obstacle(
            baseX + baseWidth * 0.7f,
            baseY + baseHeight * 0.35f,
            baseX + baseWidth * 0.7f + trapSize,
            baseY + baseHeight * 0.35f + trapSize,
            isTrap = true
        ))
        
        // 设置终点
        goal = Goal(
            baseX + baseWidth * 0.85f,
            baseY + baseHeight * 0.75f,
            baseX + baseWidth * 0.95f,
            baseY + baseHeight * 0.85f
        )
        
        // 设置起点位置
        startX = screenWidth * 0.2f  // 左侧
        startY = screenHeight * 0.2f  // 靠上位置
    }
}