package com.example.gravityballgame

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout

/**
 * 自定义关卡活动
 * 提供关卡设计和游玩功能
 */
class CustomLevelActivity : LevelActivity() {
    // 设计模式元素
    private lateinit var designLayout: ConstraintLayout
    private lateinit var brushSelectionGroup: RadioGroup
    private lateinit var timeLimitInput: EditText
    private lateinit var startCustomGameButton: Button
    private lateinit var clearDesignButton: Button
    private lateinit var backToMenuButton: Button
    private lateinit var customGameViewContainer: FrameLayout
    
    // 游戏模式元素（从标准关卡布局中加载）
    private lateinit var gameModeLayout: ConstraintLayout
    private lateinit var gameModeContainer: FrameLayout
    private lateinit var resetButton: Button
    private lateinit var menuButton: Button
    override lateinit var levelTextView: TextView
    
    // 自定义GameView实例，不使用父类的gameView
    private lateinit var customGameView: GameView
    
    private var isInDesignMode = true
    private var gameLayoutInflated = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_level)
        
        // 初始化设计模式UI元素
        designLayout = findViewById(R.id.custom_design_layout)
        brushSelectionGroup = findViewById(R.id.brush_selection_group)
        timeLimitInput = findViewById(R.id.time_limit_input)
        startCustomGameButton = findViewById(R.id.start_custom_game_button)
        clearDesignButton = findViewById(R.id.clear_design_button)
        backToMenuButton = findViewById(R.id.back_to_menu_from_custom_button)
        customGameViewContainer = findViewById(R.id.custom_game_view_container)
        
        // 先使用设计模式的容器作为游戏容器
        gameContainer = customGameViewContainer
        
        // 初始化关卡数据
        initializeLevelData()
        
        // 创建游戏视图
        initializeCustomGameView()
        
        // 设置UI监听器
        setupUIListeners()
    }
    
    /**
     * 初始化自定义游戏视图
     * 完全自定义实现，不依赖父类的createGameView方法
     */
    private fun initializeCustomGameView() {
        // 创建自定义GameView实例
        customGameView = GameView(this)
        
        // 将GameView添加到自定义容器中
        customGameViewContainer.removeAllViews()
        customGameViewContainer.addView(customGameView)
        
        // 确保游戏视图容器可见
        customGameViewContainer.visibility = View.VISIBLE
        
        // 创建关卡元素
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()
        createLevelElements(screenWidth, screenHeight)
        
        // 设置GameView属性
        customGameView.setLevelElements(obstacles, goal, startX, startY, timeLimit)
        customGameView.setOnGameEventListener(object : GameView.GameEventListener {
            override fun onGameWon() {
                showWinDialog()
            }
            
            override fun onGameLost(message: String) {
                handleGameLost(message)
            }
        })
        
        // 设置初始为设计模式
        customGameView.setDesignMode(true)
        customGameView.setActive(true)
        
        // 默认显示设计界面
        showDesignMode()
    }
    
    /**
     * 重写父类的createGameView方法，但实际上不做任何事情
     * 因为我们使用自己的initializeCustomGameView方法
     */
    override fun createGameView() {
        // 不做任何事情，因为我们使用自己的初始化方法
    }
    
    override fun initializeLevelData() {
        // 设置关卡基本信息
        levelNumber = -1 // 自定义关卡使用-1作为标识
        difficulty = 0 // 自定义难度
        timeLimit = 60 // 默认时间限制
        levelTitle = "自定义关卡"
        
        // 设置默认起始位置
        startX = 0f
        startY = 0f
    }
    
    override fun createLevelElements(screenWidth: Float, screenHeight: Float) {
        // 自定义关卡不需要预设元素
        // 所有元素都由用户在设计模式中创建
        obstacles.clear()
        // 调整小球默认初始位置，使其更适应不同屏幕，并稍微偏离中心
        startX = screenWidth * 0.45f 
        startY = screenHeight * 0.1f
    }
    
    override fun onResume() {
        super.onResume()
        // 确保在活动恢复时设置了正确的模式和状态
        if (::customGameView.isInitialized) {
            customGameView.setActive(true)
            if (isInDesignMode) {
                customGameView.setDesignMode(true)
            }
        }
    }
    
    private fun setupUIListeners() {
        // 设置画笔选择监听器
        brushSelectionGroup.setOnCheckedChangeListener { _, checkedId ->
            if (::customGameView.isInitialized) {
                when (checkedId) {
                    R.id.brush_obstacle -> customGameView.setCurrentBrush(GameView.BrushType.OBSTACLE)
                    R.id.brush_trap -> customGameView.setCurrentBrush(GameView.BrushType.TRAP)
                    R.id.brush_goal -> customGameView.setCurrentBrush(GameView.BrushType.GOAL)
                }
            }
        }
        
        // 默认选中障碍物画笔
        brushSelectionGroup.check(R.id.brush_obstacle)
        
        // 设置清除设计按钮监听器
        clearDesignButton.setOnClickListener {
            if (::customGameView.isInitialized) {
                AlertDialog.Builder(this)
                    .setTitle("确认清除")
                    .setMessage("确定要清除当前设计吗？")
                    .setPositiveButton("确定") { _, _ -> customGameView.clearCustomDesign() }
                    .setNegativeButton("取消", null)
                    .show()
            }
        }
        
        // 设置开始游戏按钮监听器
        startCustomGameButton.setOnClickListener {
            // 验证时间输入
            val timeText = timeLimitInput.text.toString()
            if (timeText.isEmpty()) {
                Toast.makeText(this, "请输入时间限制", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            try {
                val customTimeLimit = timeText.toInt()
                if (customTimeLimit <= 0) {
                    Toast.makeText(this, "时间限制必须大于0", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                // 验证关卡设计是否有效
                if (!::customGameView.isInitialized || !customGameView.hasCustomElements()) {
                    Toast.makeText(this, "请先设计关卡", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                // 切换到游戏模式
                showGameMode(customTimeLimit)
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "请输入有效的时间限制", Toast.LENGTH_SHORT).show()
            }
        }
        
        // 设置返回菜单按钮监听器
        backToMenuButton.setOnClickListener {
            if (hasUnsavedChanges()) {
                AlertDialog.Builder(this)
                    .setTitle("确认退出")
                    .setMessage("退出将丢失当前设计，确定要退出吗？")
                    .setPositiveButton("确定") { _, _ -> returnToMainMenu() }
                    .setNegativeButton("取消", null)
                    .show()
            } else {
                returnToMainMenu()
            }
        }
    }
    
    // 检查是否有未保存的更改
    private fun hasUnsavedChanges(): Boolean {
        return ::customGameView.isInitialized && customGameView.hasCustomElements()
    }
    
    /**
     * 显示设计模式界面
     */
    private fun showDesignMode() {
        try {
            isInDesignMode = true
            
            // 如果游戏界面已经加载，隐藏它
            if (gameLayoutInflated && ::gameModeLayout.isInitialized) {
                gameModeLayout.visibility = View.GONE
            }
            
            // 设置设计界面可见性
            designLayout.visibility = View.VISIBLE
            customGameViewContainer.visibility = View.VISIBLE
            
            // 将游戏视图移回设计容器
            if (::customGameView.isInitialized) {
                if (customGameView.parent != null) {
                    try {
                        (customGameView.parent as ViewGroup).removeView(customGameView)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                // 确保容器已初始化并且不包含其他视图
                customGameViewContainer.removeAllViews()
                customGameViewContainer.addView(customGameView)
                
                // 设置为设计模式
                customGameView.setDesignMode(true)
                customGameView.setActive(true)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "切换到设计模式失败: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
    
    /**
     * 加载标准关卡游戏界面
     */
    private fun inflateGameLayout() {
        try {
            if (!gameLayoutInflated) {
                // 使用安全的方式获取父容器
                val contentView = findViewById<ViewGroup>(android.R.id.content)
                
                // 直接使用LayoutInflater加载布局，不附加到父视图
                val inflater = LayoutInflater.from(this)
                val gameLayout = inflater.inflate(R.layout.activity_level, null)
                
                // 类型安全检查
                if (gameLayout !is ConstraintLayout) {
                    Log.e("CustomLevelActivity", "Inflated layout is not a ConstraintLayout: ${gameLayout.javaClass.simpleName}")
                    Toast.makeText(this, "加载游戏界面失败: 布局类型错误", Toast.LENGTH_SHORT).show()
                    return
                }
                
                // 调整布局参数
                val params = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                gameLayout.layoutParams = params
                
                // 将游戏布局添加到内容视图中
                contentView.addView(gameLayout)
                
                // 初始化游戏模式界面元素
                gameModeLayout = gameLayout
                gameModeContainer = gameLayout.findViewById(R.id.game_container)
                resetButton = gameLayout.findViewById(R.id.reset_button)
                menuButton = gameLayout.findViewById(R.id.menu_button)
                levelTextView = gameLayout.findViewById(R.id.level_text)
                
                // 设置标题
                levelTextView.text = levelTitle
                
                // 设置按钮监听器
                resetButton.setOnClickListener {
                    if (::customGameView.isInitialized) {
                        customGameView.resetGame()
                    }
                }
                
                menuButton.setOnClickListener {
                    showDesignMode()
                }
                
                // 初始时隐藏游戏界面
                gameModeLayout.visibility = View.GONE
                
                gameLayoutInflated = true
            }
        } catch (e: Exception) {
            // 捕获并记录异常
            Toast.makeText(this, "加载游戏界面失败: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("CustomLevelActivity", "Error inflating game layout", e)
            e.printStackTrace()
        }
    }
    
    /**
     * 显示游戏模式界面
     */
    private fun showGameMode(customTimeLimit: Int) {
        try {
            // 先确保GameView已初始化
            if (!::customGameView.isInitialized) {
                Toast.makeText(this, "游戏初始化失败，请重试", Toast.LENGTH_SHORT).show()
                return
            }
            
            // 加载游戏界面布局（如果还没加载）
            inflateGameLayout()
            
            // 确保游戏界面已成功加载
            if (!gameLayoutInflated || !::gameModeLayout.isInitialized || !::gameModeContainer.isInitialized) {
                Toast.makeText(this, "游戏界面加载失败，请重试", Toast.LENGTH_SHORT).show()
                return
            }
            
            // 设置模式状态
            isInDesignMode = false
            
            // 设置界面可见性
            designLayout.visibility = View.GONE
            gameModeLayout.visibility = View.VISIBLE
            
            // 将游戏视图移动到游戏容器中
            if (customGameView.parent != null) {
                (customGameView.parent as ViewGroup).removeView(customGameView)
            }
            gameModeContainer.addView(customGameView)
            
            // 先确保游戏状态被重置
            customGameView.resetGame()
            
            // 设置为非设计模式
            customGameView.setDesignMode(false)
            
            // 启动自定义游戏
            customGameView.startCustomGame(customTimeLimit)
            
            // 激活游戏视图
            customGameView.setActive(true)
        } catch (e: Exception) {
            // 捕获并记录异常
            Toast.makeText(this, "切换到游戏模式失败: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            
            // 出错时回到设计模式
            try {
                designLayout.visibility = View.VISIBLE
                if (gameLayoutInflated && ::gameModeLayout.isInitialized) {
                    gameModeLayout.visibility = View.GONE
                }
                isInDesignMode = true
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }
    
    /**
     * 自定义关卡胜利对话框
     */
    override fun showWinDialog() {
        if (!isFinishing && !isDestroyed) {
            AlertDialog.Builder(this)
                .setTitle("自定义关卡完成!")
                .setMessage("恭喜你完成了自己设计的关卡!")
                .setPositiveButton("返回设计") { _, _ ->
                    // 重置游戏状态但保留设计
                    if (::customGameView.isInitialized) {
                        customGameView.resetGame()
                    }
                    // 切换回设计模式
                    showDesignMode()
                }
                .setNegativeButton("返回菜单") { _, _ -> returnToMainMenu() }
                .setCancelable(false)
                .show()
        }
    }
    
    /**
     * 处理游戏失败情况
     * 不重写showLoseDialog，而是使用自己的方法
     */
    private fun handleGameLost(message: String) {
        if (!isFinishing && !isDestroyed) {
            AlertDialog.Builder(this)
                .setTitle("游戏结束")
                .setMessage(message)
                .setPositiveButton("重试") { _, _ ->
                    if (::customGameView.isInitialized) {
                        customGameView.resetGame()
                        customGameView.setActive(true)
                    }
                }
                .setNegativeButton("返回设计") { _, _ ->
                    if (::customGameView.isInitialized) {
                        customGameView.resetGame()
                    }
                    showDesignMode()
                }
                .setCancelable(false)
                .show()
        }
    }
    
    override fun onBackPressed() {
        super.onBackPressed()
        if (!isInDesignMode) {
            // 如果在游戏模式，返回到设计模式
            if (::customGameView.isInitialized) {
                customGameView.resetGame()
            }
            showDesignMode()
        } else {
            // 如果在设计模式，询问是否返回主菜单
            if (hasUnsavedChanges()) {
                AlertDialog.Builder(this)
                    .setTitle("确认退出")
                    .setMessage("退出将丢失当前设计，确定要退出吗？")
                    .setPositiveButton("确定") { _, _ -> returnToMainMenu() }
                    .setNegativeButton("取消", null)
                    .show()
            } else {
                returnToMainMenu()
            }
        }
    }
}