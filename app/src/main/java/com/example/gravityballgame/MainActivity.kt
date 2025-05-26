package com.example.gravityballgame

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.example.gravityballgame.data.AppDatabase
import com.example.gravityballgame.data.User
import com.example.gravityballgame.data.UserSessionManager
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * 主Activity，作为游戏的入口点
 * 提供关卡选择和难度设置
 */
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var difficultyRadioGroup: RadioGroup
    private lateinit var startStandardGameButton: Button
    private lateinit var customModeButton: Button
    private lateinit var challengeModeButton: Button
    
    private lateinit var toolbar: Toolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var userSessionManager: UserSessionManager
    private lateinit var userDao: com.example.gravityballgame.data.UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // 初始化数据库和用户会话管理器
        userDao = AppDatabase.getDatabase(this).userDao()
        userSessionManager = UserSessionManager(this)
        
        // 初始化视图
        difficultyRadioGroup = findViewById(R.id.difficulty_radio_group)
        startStandardGameButton = findViewById(R.id.start_standard_game_button)
        customModeButton = findViewById(R.id.custom_mode_button)
        challengeModeButton = findViewById(R.id.challenge_mode_button)
        
        // 设置窗口插入监听器 (应用到根布局)
        val rootLayout = findViewById<ConstraintLayout>(R.id.main_layout_root)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // 设置开始标准游戏按钮的监听器
        startStandardGameButton.setOnClickListener {
            startSelectedLevel()
        }
        
        // 设置自定义模式按钮为不可点击
        customModeButton.isEnabled = true
        customModeButton.setOnClickListener {
            startCustomLevel()
        }
        
        // 设置挑战模式按钮的监听器
        challengeModeButton.setOnClickListener {
            // 检查用户是否已登录，未登录则提示登录
            if (userSessionManager.isLoggedIn()) {
                startChallengeLevel()
            } else {
                Toast.makeText(this, "请先登录后再挑战", Toast.LENGTH_SHORT).show()
                showLoginDialog()
            }
        }
        
        // 设置导航抽屉
        setupNavigationDrawer()
    }

    /**
     * 启动选中难度的关卡
     */
    private fun startSelectedLevel() {
        val intent = when (difficultyRadioGroup.checkedRadioButtonId) {
            R.id.radio_easy -> Intent(this, Level1Activity::class.java)
            R.id.radio_medium -> Intent(this, Level2Activity::class.java)
            R.id.radio_hard -> Intent(this, Level3Activity::class.java)
            else -> Intent(this, Level1Activity::class.java) // 默认简单难度
        }
        startActivity(intent)
    }

    private fun startCustomLevel() {
        val intent = Intent(this, CustomLevelActivity::class.java)
        startActivity(intent)
    }

    private fun startChallengeLevel() {
        val intent = Intent(this, LevelChallengeActivity::class.java)
        startActivity(intent)
    }
    
    // 设置导航抽屉
    private fun setupNavigationDrawer() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        
        // 设置ActionBarDrawerToggle
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        
        // 设置导航项选择监听器
        navigationView.setNavigationItemSelectedListener(this)
        
        // 更新导航头部信息
        updateNavigationHeader()
    }
    
    // 更新导航头部的用户信息
    private fun updateNavigationHeader() {
        val headerView = navigationView.getHeaderView(0)
        val usernameTextView = headerView.findViewById<TextView>(R.id.nav_header_username)
        val bestTimeTextView = headerView.findViewById<TextView>(R.id.nav_header_best_time)
        
        if (userSessionManager.isLoggedIn()) {
            // 用户已登录，显示用户信息
            val username = userSessionManager.getUsername()
            usernameTextView.text = username
            
            // 获取用户最佳成绩
            lifecycleScope.launch {
                val userId = userSessionManager.getUserId()
                val user = userDao.getUserById(userId)
                if (user != null && user.bestChallengeTime > 0) {
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(user.bestChallengeTime)
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(user.bestChallengeTime) % 60
                    val millis = user.bestChallengeTime % 1000
                    bestTimeTextView.text = String.format("最佳成绩: %02d:%02d.%03d", minutes, seconds, millis)
                } else {
                    bestTimeTextView.text = "最佳成绩: 暂无"
                }
            }
            
            // 显示登出选项，隐藏登录选项
            navigationView.menu.findItem(R.id.nav_login).isVisible = false
            navigationView.menu.findItem(R.id.nav_logout).isVisible = true
        } else {
            // 用户未登录，显示默认信息
            usernameTextView.text = "未登录"
            bestTimeTextView.text = "最佳成绩: 暂无"
            
            // 显示登录选项，隐藏登出选项
            navigationView.menu.findItem(R.id.nav_login).isVisible = true
            navigationView.menu.findItem(R.id.nav_logout).isVisible = false
        }
    }
    
    // 处理导航项选择事件
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_login -> {
                showLoginDialog()
            }
            R.id.nav_leaderboard -> {
                val intent = Intent(this, LeaderboardActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_logout -> {
                userSessionManager.logout()
                updateNavigationHeader()
                Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show()
            }
        }
        
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    
    // 显示登录对话框
    private fun showLoginDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_login, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        
        val usernameInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.username_input)
        val passwordInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.password_input)
        val loginButton = dialogView.findViewById<Button>(R.id.login_button)
        val registerButton = dialogView.findViewById<Button>(R.id.register_button)
        
        // 登录按钮点击事件
        loginButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "用户名和密码不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // 执行登录
            lifecycleScope.launch {
                val user = userDao.login(username, password)
                if (user != null) {
                    // 登录成功
                    userSessionManager.saveUserLoginSession(user.id, user.username)
                    updateNavigationHeader()
                    Toast.makeText(this@MainActivity, "登录成功", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    // 登录失败
                    Toast.makeText(this@MainActivity, "用户名或密码错误", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        // 注册按钮点击事件
        registerButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "用户名和密码不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // 执行注册
            lifecycleScope.launch {
                val existingUser = userDao.getUserByUsername(username)
                if (existingUser != null) {
                    // 用户名已存在
                    Toast.makeText(this@MainActivity, "用户名已存在", Toast.LENGTH_SHORT).show()
                } else {
                    // 创建新用户
                    val newUser = User(username = username, password = password)
                    val userId = userDao.insertUser(newUser)
                    
                    // 自动登录
                    userSessionManager.saveUserLoginSession(userId, username)
                    updateNavigationHeader()
                    Toast.makeText(this@MainActivity, "注册成功并已登录", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }
        
        dialog.show()
    }
    
    // 处理返回按钮事件
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}