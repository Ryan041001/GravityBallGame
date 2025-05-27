package com.example.gravityballgame

import android.os.Bundle

/**
 * 第三关（困难难度）Activity
 */
class Level3Activity : LevelActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    
    override fun initializeLevelData() {
        // 设置关卡基本信息
        levelNumber = 3
        difficulty = 3
        timeLimit = 25
        levelTitle = "困难模式"
        
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
        val trapSize = screenWidth * 0.05f  // 减小陷阱大小
        
        // 上横向墙 - 分成两段
        obstacles.add(Obstacle(
            baseX + baseWidth * 0.1f,
            baseY + baseHeight * 0.2f,
            baseX + baseWidth * 0.41f,
            baseY + baseHeight * 0.2f + wallThickness,
            isTrap = false
        ))
        
        obstacles.add(Obstacle(
            baseX + baseWidth * 0.6f,
            baseY + baseHeight * 0.2f,
            baseX + baseWidth * 0.9f,
            baseY + baseHeight * 0.2f + wallThickness,
            isTrap = false
        ))
        
        // 中间斜向墙
        obstacles.add(Obstacle(
            baseX + baseWidth * 0.4f,
            baseY + baseHeight * 0.2f,
            baseX + baseWidth * 0.4f + wallThickness,
            baseY + baseHeight * 0.5f,
            isTrap = false
        ))
        
        // 下横向墙 - 也分成两段
        obstacles.add(Obstacle(
            baseX + baseWidth * 0.1f,
            baseY + baseHeight * 0.5f,
            baseX + baseWidth * 0.3f,
            baseY + baseHeight * 0.5f + wallThickness,
            isTrap = false
        ))
        
        obstacles.add(Obstacle(
            baseX + baseWidth * 0.5f,
            baseY + baseHeight * 0.5f,
            baseX + baseWidth * 0.9f,
            baseY + baseHeight * 0.5f + wallThickness,
            isTrap = false
        ))

        // 添加陷阱1 - 通道下方
        obstacles.add(Obstacle(
            baseX + baseWidth * 0.45f,
            baseY + baseHeight * 0.65f,
            baseX + baseWidth * 0.55f + trapSize,
            baseY + baseHeight * 0.65f + trapSize,
            isTrap = true
        ))
        
        // 添加陷阱2 - 下方区域
        obstacles.add(Obstacle(
            baseX + baseWidth * 0.5f,
            baseY + baseHeight * 0.8f - trapSize * 2,
            baseX + baseWidth * 0.65f + trapSize,
            baseY + baseHeight * 0.8f + trapSize * 2,
            isTrap = true
        ))
        
        // 添加陷阱3 - 右侧区域
        obstacles.add(Obstacle(
            baseX + baseWidth * 0.8f - trapSize,
            baseY + baseHeight * 0.25f,
            baseX + baseWidth * 0.8f + trapSize,
            baseY + baseHeight * 0.45f,
            isTrap = true
        ))
        
        // 设置终点
        goal = Goal(
            baseX + baseWidth * 0.8f,
            baseY + baseHeight * 0.7f,
            baseX + baseWidth * 0.9f,
            baseY + baseHeight * 0.8f
        )
        
        // 设置起点位置
        startX = screenWidth * 0.2f  // 屏幕左上区域，比中等模式稍靠上
        startY = screenHeight * 0.15f  // 靠上位置
    }
}