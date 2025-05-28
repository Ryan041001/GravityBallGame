package com.example.gravityballgame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.util.Collections
import java.util.ArrayList

/**
 * 游戏主视图类
 * 负责游戏核心渲染、物理计算及用户交互处理
 * 支持常规游戏模式和自定义关卡设计模式
 */
class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback, SensorEventListener, Runnable {
    private var thread: Thread = Thread(this)
    private var isRunning = false
    private var gameIsActive = false
    private val paint = Paint()
    private val ball = Ball(100f, 100f, 30f)
    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var isGameWon = false
    private var isGameLost = false
    var gameStartTime: Long = 0
    private val textPaint = Paint().apply {
        color = Color.parseColor("#212121") // 深灰色文本颜色
        textSize = 48f // 较大字号提升可读性
        textAlign = Paint.Align.LEFT
        isAntiAlias = true // 启用抗锯齿
    }

    // 游戏关卡元素
    private val obstacles = mutableListOf<Obstacle>()
    private val traps = mutableListOf<Obstacle>() // 陷阱类型的障碍物，触碰即失败
    private val paths = mutableListOf<Obstacle>() // 安全路径元素，仅用于视觉显示
    private var goal: Goal? = null
    private var startX: Float = 100f
    private var startY: Float = 100f
    private var timeLimit: Int = 60
    private var hasInitialized = false // 初始化状态标志

    // 自定义关卡设计模式相关变量
    private var isDesigningMode = false
    private var currentBrushType: BrushType = BrushType.OBSTACLE
    private val customObstacles = Collections.synchronizedList(mutableListOf<Obstacle>())
    private val customTraps = Collections.synchronizedList(mutableListOf<Obstacle>())
    private var customGoal: Goal? = null
    private var customTimeLimitSec: Int = 60

    // 绘制路径时的临时坐标存储
    private var currentPathX: Float? = null
    private var currentPathY: Float? = null
    
    // 自定义元素访问同步锁
    private val customElementsLock = Any()

    // 屏幕尺寸数据处理
    private val screenWidth = resources.displayMetrics.widthPixels.toFloat()
    private val screenHeight = resources.displayMetrics.heightPixels.toFloat()

    // 游戏事件回调监听器
    private var gameEventListener: GameEventListener? = null

    // 画笔类型枚举，用于自定义关卡设计
    enum class BrushType {
        OBSTACLE, TRAP, GOAL
    }

    init {
        holder.addCallback(this)
        paint.apply {
            color = Color.RED
            style = Paint.Style.FILL
            strokeWidth = 36f
        }
    }

    /**
     * 设置关卡元素配置
     * 
     * @param obstacles 障碍物和陷阱列表
     * @param goal 终点区域对象
     * @param startX 小球起始X坐标
     * @param startY 小球起始Y坐标
     * @param timeLimit 关卡时间限制(秒)
     */
    fun setLevelElements(
        obstacles: List<Obstacle>,
        goal: Goal?,
        startX: Float,
        startY: Float,
        timeLimit: Int
    ) {
        this.obstacles.clear()
        this.traps.clear()
        this.paths.clear()
        
        // 根据isTrap属性将障碍物分类存储
        for (obstacle in obstacles) {
            if (obstacle.isTrap) {
                this.traps.add(obstacle)
            } else {
                this.obstacles.add(obstacle)
            }
        }
        
        this.goal = goal
        this.startX = startX
        this.startY = startY
        this.timeLimit = timeLimit
        
        // 仅在视图初始化完成后重置小球位置
        if (hasInitialized) {
            ball.reset(startX, startY)
        }
    }
    
    /**
     * 添加纯视觉效果路径元素
     * 这些路径不参与碰撞检测，仅用于视觉展示
     * 
     * @param paths 路径元素列表
     */
    fun addPathsForDrawing(paths: List<Obstacle>) {
        this.paths.clear()
        this.paths.addAll(paths)
    }

    /**
     * 设置游戏事件监听器
     * 
     * @param listener 游戏事件回调接口实现
     */
    fun setOnGameEventListener(listener: GameEventListener) {
        this.gameEventListener = listener
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (!thread.isAlive) {
            thread = Thread(this)
            thread.start()
        }
        isRunning = true
        hasInitialized = true
        
        // Surface创建完成后初始化小球位置
        ball.reset(startX, startY)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        updateScreenBounds(width, height)
        // 屏幕尺寸变更时重置小球位置
        if (hasInitialized) {
            ball.reset(startX, startY)
        }
    }

    /**
     * 更新游戏物理边界尺寸
     * 
     * @param width 屏幕宽度
     * @param height 屏幕高度
     */
    fun updateScreenBounds(width: Int, height: Int) {
        ball.updateScreenBounds(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isRunning = false
        var retry = true
        while (retry) {
            try {
                thread.join()
                retry = false
            } catch (e: InterruptedException) {
                // 线程关闭失败，重试
            }
        }
        sensorManager.unregisterListener(this)
    }

    override fun run() {
        while (isRunning) {
            if (!gameIsActive) {
                try {
                    Thread.sleep(50)
                } catch (e: InterruptedException) {
                    // 线程休眠被中断
                }
                continue
            }

            val canvas = holder.lockCanvas() ?: continue
            try {
                synchronized(holder) {
                    if (isDesigningMode) {
                        updateDesign()
                        drawDesign(canvas)
                    } else {
                        updateGame()
                        drawGame(canvas)
                    }
                }
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }

            try {
                Thread.sleep(8) // 约120FPS的更新频率
            } catch (e: InterruptedException) {
                // 线程休眠被中断
            }
        }
    }

    // --- 游戏模式核心逻辑 ---
    /**
     * 更新游戏状态
     * 处理小球物理更新、碰撞检测和游戏规则判定
     */
    private fun updateGame() {
        if (isGameWon || isGameLost) return

        ball.update()

        // 时间限制检查
        val elapsedSeconds = (System.currentTimeMillis() - gameStartTime) / 1000
        val timeLimit = if (isDesigningMode) customTimeLimitSec.toLong() else this.timeLimit.toLong()
        if (elapsedSeconds >= timeLimit) {
            isGameLost = true
            post { gameEventListener?.onGameLost("时间用完了！") }
            return
        }

        // 屏幕边界越界检查
        val ballX = ball.getX()
        val ballY = ball.getY()
        val ballRadius = ball.getRadius()

        if (ballX - ballRadius <= 0 || ballX + ballRadius >= width ||
            ballY - ballRadius <= 0 || ballY + ballRadius >= height) {
            isGameLost = true
            post { gameEventListener?.onGameLost("球出界了！") }
            return
        }

        // 碰撞检测处理
        if (isDesigningMode) {
            synchronized(customElementsLock) {
                // 自定义模式下的障碍物碰撞处理
                for (obstacle in customObstacles) {
                    if (ball.checkCollision(obstacle)) {
                        ball.handleCollision(obstacle)
                    }
                }

                // 自定义模式下的陷阱碰撞检查
                for (trap in customTraps) {
                    if (ball.checkCollision(trap)) {
                        isGameLost = true
                        post { gameEventListener?.onGameLost("碰到陷阱了！") }
                        return
                    }
                }

                // 自定义模式下的终点检测
                if (customGoal != null && ball.checkGoal(customGoal!!)) {
                    isGameWon = true
                    post { gameEventListener?.onGameWon() }
                    return
                }
            }
        } else {
            // 标准模式下的碰撞处理
            // 障碍物碰撞处理
            for (obstacle in obstacles) {
                if (ball.checkCollision(obstacle)) {
                    ball.handleCollision(obstacle)
                }
            }

            // 陷阱碰撞检查
            for (trap in traps) {
                if (ball.checkCollision(trap)) {
                    isGameLost = true
                    post { gameEventListener?.onGameLost("碰到陷阱了！") }
                    return
                }
            }

            // 终点检测
            if (goal != null && ball.checkGoal(goal!!)) {
                isGameWon = true
                post { gameEventListener?.onGameWon() }
                return
            }
        }
    }

    /**
     * 绘制游戏画面
     * 按照背景、路径、障碍物、陷阱、终点、小球的顺序进行渲染
     * 
     * @param canvas 绘图画布
     */
    private fun drawGame(canvas: Canvas) {
        // 绘制背景
        canvas.drawColor(Color.parseColor("#ECEFF1")) // 浅灰色背景色

        // 绘制安全路径
        val pathPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#FFFFFF") // 白色路径
        }
        for (path in paths) {
            path.draw(canvas, pathPaint)
        }
        
        // 绘制障碍物
        for (obstacle in obstacles) {
            obstacle.draw(canvas, paint)
        }
        
        // 绘制陷阱
        for (trap in traps) {
            trap.draw(canvas, paint)
        }
        
        // 绘制终点
        goal?.draw(canvas, paint)
        
        // 绘制小球
        ball.draw(canvas, paint)
        
        // 绘制剩余时间信息
        val remainingTime = if (timeLimit > 0) {
            val elapsedTime = (System.currentTimeMillis() - gameStartTime) / 1000
            maxOf(0, timeLimit - elapsedTime.toInt())
        } else {
            0
        }
        
        val timeText = "Time: $remainingTime s"
        // 根据剩余时间设置文字颜色
        textPaint.color = if (remainingTime > 5) Color.parseColor("#212121") else Color.RED
        canvas.drawText(timeText, 50f, 100f, textPaint)
        // 恢复默认颜色
        textPaint.color = Color.parseColor("#212121")
        
        // 绘制游戏状态信息
        if (isGameWon) {
            val timeTaken = (System.currentTimeMillis() - gameStartTime) / 1000
            textPaint.color = Color.rgb(0, 100, 0)
            canvas.drawText("通关! 用时: ${timeTaken}秒", 100f, 200f, textPaint)
        } else if (isGameLost) {
            textPaint.color = Color.RED
            canvas.drawText("游戏结束!", 100f, 200f, textPaint)
        }
    }

    // --- 设计模式核心逻辑 ---
    /**
     * 更新设计模式状态
     */
    private fun updateDesign() {
        // 设计模式下无需特殊的实时更新逻辑
    }

    /**
     * 绘制设计模式界面
     * 
     * @param canvas 绘图画布
     */
    private fun drawDesign(canvas: Canvas) {
        // 先绘制背景
        canvas.drawColor(Color.parseColor("#CFD8DC")) // 设计模式专用背景色

        // 设置画笔属性
        paint.strokeWidth = 36f
        paint.style = Paint.Style.FILL

        // 使用线程安全方式访问自定义元素
        synchronized(customElementsLock) {
            // 绘制自定义障碍物
            for (obstacle in ArrayList(customObstacles)) {
                obstacle.draw(canvas, paint)
            }

            // 绘制自定义陷阱
            for (trap in ArrayList(customTraps)) {
                trap.draw(canvas, paint)
            }

            // 绘制自定义终点
            customGoal?.draw(canvas, paint)
            
            // 绘制小球（设计模式下作为参考点）
            ball.draw(canvas, paint)
        }

        // 显示当前选择的画笔类型
        textPaint.color = Color.DKGRAY
        val brushTypeText = when(currentBrushType) {
            BrushType.OBSTACLE -> "障碍物"
            BrushType.TRAP -> "陷阱"
            BrushType.GOAL -> "终点"
        }
        canvas.drawText("当前画笔: $brushTypeText", 20f, 60f, textPaint)
        
        // 在设计模式下显示提示文本
        textPaint.textSize = 36f
        canvas.drawText("拖动手指绘制关卡", 20f, 110f, textPaint)
    }

    /**
     * 处理触摸事件
     * 在设计模式下用于创建和编辑自定义关卡元素
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isDesigningMode || !gameIsActive || event == null) return super.onTouchEvent(event)

        try {
            val x = event.x
            val y = event.y

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    currentPathX = x
                    currentPathY = y
                    if (currentBrushType == BrushType.GOAL) {
                        synchronized(customElementsLock) {
                            customGoal = Goal(x - 50f, y - 50f, x + 50f, y + 50f)
                        }
                        invalidate()
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    val prevX = currentPathX
                    val prevY = currentPathY

                    if (prevX != null && prevY != null) {
                        synchronized(customElementsLock) {
                            if (currentBrushType != BrushType.GOAL) {
                                val minX = minOf(prevX, x)
                                val minY = minOf(prevY, y)
                                val maxX = maxOf(prevX, x)
                                val maxY = maxOf(prevY, y)

                                val width = maxOf(maxX - minX, 36f)
                                val height = maxOf(maxY - minY, 36f)

                                val segment = Obstacle(
                                    minX, minY,
                                    minX + width, minY + height,
                                    isTrap = (currentBrushType == BrushType.TRAP)
                                )

                                if (currentBrushType == BrushType.OBSTACLE) {
                                    customObstacles.add(segment)
                                } else if (currentBrushType == BrushType.TRAP) {
                                    customTraps.add(segment)
                                }
                                else{}
                            } else if (customGoal != null) {
                                customGoal?.adjustBounds(x - 50f, y - 50f, x + 50f, y + 50f)
                            }else{}
                        }
                    }
                    
                    currentPathX = x
                    currentPathY = y
                    invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    synchronized(customElementsLock) {
                        if (currentBrushType == BrushType.GOAL && customGoal == null) {
                            customGoal = Goal(x - 50f, y - 50f, x + 50f, y + 50f)
                        }
                    }
                    
                    currentPathX = null
                    currentPathY = null
                    invalidate()
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return true
        }
    }

    /**
     * 重置游戏状态，回到初始设置
     */
    fun resetGame() {
        ball.reset(startX, startY) // 将小球位置重置为起始位置
        isGameWon = false
        isGameLost = false
        // 在重置位置后清除小球的速度和加速度
        ball.setAcceleration(0f, 0f)
        // 重新激活游戏，这会自动重置计时器
        setActive(true)
    }

    /**
     * 检查是否存在自定义元素
     * 用于确定设计模式中是否有未保存的更改
     * 
     * @return 是否存在自定义元素
     */
    fun hasCustomElements(): Boolean {
        synchronized(customElementsLock) {
            return customObstacles.isNotEmpty() || customTraps.isNotEmpty() || customGoal != null
        }
    }

    /**
     * 开始自定义关卡游戏
     * 验证关卡有效性并切换至游戏模式
     * 
     * @param timeLimit 时间限制(秒)
     */
    fun startCustomGame(timeLimit: Int) {
        // 验证关卡有效性
        if (customGoal == null) {
            post { gameEventListener?.onGameLost("请先放置终点！") }
            return
        }
        
        if (customObstacles.isEmpty() && customTraps.isEmpty()) {
            post { gameEventListener?.onGameLost("请至少添加一些障碍物或陷阱！") }
            return
        }
        
        // 将自定义元素复制到游戏中使用的元素集合
        synchronized(customElementsLock) {
            // 清除现有元素
            obstacles.clear()
            traps.clear()
            
            // 复制自定义障碍物
            obstacles.addAll(customObstacles)
            
            // 复制自定义陷阱
            traps.addAll(customTraps)
            
            // 复制自定义终点
            goal = customGoal
            
            // 设置时间限制
            this.timeLimit = timeLimit
            this.customTimeLimitSec = timeLimit
        }
        
        // 设置游戏模式
        setDesignMode(false)
        
        // 重置游戏状态
        isGameWon = false
        isGameLost = false
        gameStartTime = System.currentTimeMillis()
        
        // 设置小球初始位置
        ball.reset(screenWidth * 0.45f, screenHeight * 0.1f)
        
        // 启动游戏
        setActive(true)
        invalidate()
    }

    /**
     * 清除自定义关卡设计
     * 移除所有自定义元素，恢复初始状态
     */
    fun clearCustomDesign() {
        synchronized(customElementsLock) {
            customObstacles.clear()
            customTraps.clear()
            customGoal = null

            ball.reset(screenWidth * 0.45f, screenHeight * 0.1f)
            
            isGameWon = false
            isGameLost = false
            gameStartTime = 0L
        }
        invalidate()
    }

    /**
     * 设置当前模式
     * 在设计模式和游戏模式之间切换
     * 
     * @param isDesigning true表示设计模式，false表示游戏模式
     */
    fun setDesignMode(isDesigning: Boolean) {
        // 若模式已经是当前所需模式，无需切换
        if (this.isDesigningMode == isDesigning) return

        this.isDesigningMode = isDesigning
        
        // 如果切换到设计模式，将小球固定在起始位置
        ball.reset(screenWidth * 0.45f, screenHeight * 0.1f)
        
        // 如果切换到游戏模式，需要暂时停用以便等待startCustomGame调用
        if (!isDesigning) {
            setActive(false)
        }
        
        // 请求重绘界面
        invalidate()
    }

    /**
     * 获取当前是否为设计模式
     * 
     * @return 是否处于设计模式
     */
    fun isCurrentModeDesign(): Boolean {
        return isDesigningMode
    }

    /**
     * 设置当前画笔类型
     * 
     * @param brushType 画笔类型枚举值
     */
    fun setCurrentBrush(brushType: BrushType) {
        this.currentBrushType = brushType
    }

    /**
     * 设置游戏活动状态
     * 控制传感器注册和游戏时钟
     * 
     * @param active 是否激活游戏
     */
    fun setActive(active: Boolean) {
        this.gameIsActive = active
        if (active) {
            if (!isDesigningMode) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
                gameStartTime = System.currentTimeMillis()
            }
        } else {
            sensorManager.unregisterListener(this)
        }
    }

    /**
     * 获取游戏当前活动状态
     * 
     * @return 游戏是否处于活动状态
     */
    fun isGameActive(): Boolean {
        return gameIsActive
    }

    /**
     * 手动设置小球加速度
     * 用于外部控制或测试
     * 
     * @param x X轴加速度
     * @param y Y轴加速度
     */
    fun setAcceleration(x: Float, y: Float) {
        ball.setAcceleration(x, y)
    }

    /**
     * 更新游戏模式的时间限制
     * 用于在游戏过程中动态修改时间限制
     */
    fun updateGameTimeLimit(timeLimit: Int) {
        this.timeLimit = timeLimit
    }

    /**
     * 加速度传感器数据处理
     * 将物理加速度转换为游戏内小球控制力
     */
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = -event.values[0] * 0.1f // 横向加速度（左右），反向以匹配直觉操作
            val y = event.values[1] * 0.1f // 纵向加速度（上下）
            
            // 将处理后的传感器数据传递给Activity
            (context as? SensorEventHandler)?.onSensorChanged(x, y)
            
            // 应用加速度到小球
            ball.setAcceleration(x, y)
        }
    }

    /**
     * 传感器精度变化回调
     * 当前版本未使用此功能
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 传感器精度变化处理（当前未实现）
    }

    /**
     * 游戏事件回调接口
     * 用于向Activity传递游戏状态变化事件
     */
    interface GameEventListener {
        /**
         * 游戏胜利回调
         */
        fun onGameWon()
        
        /**
         * 游戏失败回调
         * 
         * @param message 失败原因描述
         */
        fun onGameLost(message: String)
    }
}

