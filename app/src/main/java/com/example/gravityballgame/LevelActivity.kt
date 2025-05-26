package com.example.gravityballgame

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


/**
 * 传感器事件处理接口
 */
interface SensorEventHandler {
    fun onSensorChanged(x: Float, y: Float)
}

/**
 * 基础关卡Activity类
 * 所有具体关卡Activity都继承自这个类
 */
abstract class LevelActivity : AppCompatActivity(), SensorEventHandler {
    protected open val gameView: GameView by lazy { createGameViewInstance() }
    protected lateinit var gameLayout: ConstraintLayout
    protected open lateinit var levelTextView: TextView
    protected lateinit var gameContainer: FrameLayout
    
    // 关卡相关属性
    protected var levelNumber: Int = 0
    protected var difficulty: Int = 1
    protected var timeLimit: Int = 60
    protected var levelTitle: String = ""
    
    // 关卡元素
    protected val obstacles = mutableListOf<Obstacle>()
    protected var goal: Goal? = null
    protected var startX: Float = 100f
    protected var startY: Float = 100f
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level)
        
        // 初始化视图
        gameLayout = findViewById(R.id.game_layout)
        levelTextView = findViewById(R.id.level_text)
        gameContainer = findViewById(R.id.game_container)
        
        // 设置窗口插入监听器
        ViewCompat.setOnApplyWindowInsetsListener(gameLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // 初始化关卡数据
        initializeLevelData()
        
        // 创建游戏视图
        createGameView()
        
        // 设置重置按钮
        findViewById<Button>(R.id.reset_button).setOnClickListener {
            resetGame()
        }
        
        // 设置返回菜单按钮
        findViewById<Button>(R.id.menu_button).setOnClickListener {
            returnToMainMenu()
        }
        
        // 更新关卡标题
        updateLevelTitle()
    }
    
    /**
     * 初始化关卡数据，子类必须实现此方法来设置关卡特定的属性
     */
    protected abstract fun initializeLevelData()
    
    /**
     * 创建关卡特定的障碍物、陷阱和终点，子类必须实现此方法
     */
    protected abstract fun createLevelElements(screenWidth: Float, screenHeight: Float)
    
    /**
     * 创建GameView实例
     */
    protected open fun createGameViewInstance(): GameView {
        return GameView(this)
    }
    
    /**
     * 初始化GameView并添加到布局中
     */
    open fun createGameView() {
        // 将GameView添加到布局中
        gameContainer.removeAllViews()
        gameContainer.addView(gameView, 0)
        
        // 创建关卡元素
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()
        createLevelElements(screenWidth, screenHeight)
        
        // 设置GameView属性
        gameView.setLevelElements(obstacles, goal, startX, startY, timeLimit)
        gameView.setOnGameEventListener(object : GameView.GameEventListener {
            override fun onGameWon() {
                showWinDialog()
            }
            
            override fun onGameLost(message: String) {
                showLoseDialog(message)
            }
        })
        
        // 设置游戏为活动状态
        gameView.setActive(true)
    }
    
    /**
     * 更新关卡标题
     */
    protected fun updateLevelTitle() {
        levelTextView.text = levelTitle
    }
    
    /**
     * 重置游戏
     */
    protected fun resetGame() {
        gameView.resetGame()
    }
    
    /**
     * 返回主菜单
     */
    protected fun returnToMainMenu() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    /**
     * 显示胜利对话框
     */
    protected open fun showWinDialog() {
        if (!isFinishing && !isDestroyed) {
            val nextLevel = levelNumber + 1
            val message = if (nextLevel <= 3) {
                "恭喜完成第${levelNumber}关！是否尝试下一关？"
            } else {
                "恭喜完成最后一关！是否再次挑战？"
            }
            
            AlertDialog.Builder(this)
                .setTitle("关卡完成")
                .setMessage(message)
                .setPositiveButton(if (nextLevel <= 3) "下一关" else "再次挑战") { _, _ ->
                    if (nextLevel <= 3) {
                        // 启动下一关的Activity
                        val intent = when (nextLevel) {
                            1 -> Intent(this, Level1Activity::class.java)
                            2 -> Intent(this, Level2Activity::class.java)
                            3 -> Intent(this, Level3Activity::class.java)
                            else -> Intent(this, Level1Activity::class.java)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        // 重新开始当前关卡
                        resetGame()
                    }
                }
                .setNegativeButton("重玩本关") { _, _ -> resetGame() }
                .setNeutralButton("返回菜单") { _, _ -> returnToMainMenu() }
                .setCancelable(false)
                .show()
        }
    }
    
    /**
     * 显示失败对话框
     */
    protected fun showLoseDialog(loseMessage: String) {
        if (!isFinishing && !isDestroyed) {
            AlertDialog.Builder(this)
                .setTitle("游戏结束")
                .setMessage(loseMessage)
                .setPositiveButton("重新开始") { _, _ -> resetGame() }
                .setNegativeButton("返回菜单") { _, _ -> returnToMainMenu() }
                .setCancelable(false)
                .show()
        }
    }
    
    override fun onResume() {
        super.onResume()
        gameView.setActive(true)
    }
    
    override fun onPause() {
        super.onPause()
        gameView.setActive(false)
    }
    
    /**
     * 传感器事件回调实现
     */
    override fun onSensorChanged(x: Float, y: Float) {
        gameView.setAcceleration(x, y) // 移除x的取反操作
    }
}