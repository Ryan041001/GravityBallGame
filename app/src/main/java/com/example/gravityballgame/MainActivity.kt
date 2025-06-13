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
import com.example.gravityballgame.data.User
import com.example.gravityballgame.data.UserSessionManager
import com.example.gravityballgame.network.NetworkService
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
    private lateinit var networkService: NetworkService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // 初始化用户会话管理器和网络服务
        userSessionManager = UserSessionManager(this)
        networkService = NetworkService(this)
        
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
            
            // 从网络获取用户最佳成绩
            lifecycleScope.launch {
                val userId = userSessionManager.getUserId()
                try {
                    val result = networkService.getUserScores(userId.toInt(), "challenge")
                    when (result) {
                        is NetworkService.ApiResult.Success -> {
                            val scores = result.data.second
                            // 找出最佳完成时间
                            val bestScore = scores.minByOrNull { it.completionTime }
                            if (bestScore != null) {
                                val completionTime = (bestScore.completionTime * 1000).toLong() // 转换为毫秒
                                val minutes = TimeUnit.MILLISECONDS.toMinutes(completionTime)
                                val seconds = TimeUnit.MILLISECONDS.toSeconds(completionTime) % 60
                                val millis = completionTime % 1000
                                bestTimeTextView.text = String.format("最佳成绩: %02d:%02d.%03d", minutes, seconds, millis)
                            } else {
                                bestTimeTextView.text = "最佳成绩: 暂无"
                            }
                        }
                        is NetworkService.ApiResult.Error -> {
                            bestTimeTextView.text = "最佳成绩: 获取失败"
                            Toast.makeText(this@MainActivity, "获取成绩失败：${result.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    bestTimeTextView.text = "最佳成绩: 获取失败"
                    Toast.makeText(this@MainActivity, "获取成绩失败：${e.message}", Toast.LENGTH_SHORT).show()
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
            
            // 显示加载提示
            val loadingDialog = AlertDialog.Builder(this)
                .setMessage("正在登录...")
                .setCancelable(false)
                .create()
            loadingDialog.show()
            
            // 执行在线登录
            lifecycleScope.launch {
                try {
                    // 先尝试在线登录
                    val result = networkService.login(username, password)
                    loadingDialog.dismiss()
                    
                    when (result) {
                        is NetworkService.ApiResult.Success -> {
                            // 在线登录成功
                            val onlineUser = result.data
                            
                            // 保存登录会话 (userId 从在线登录结果获取)
                            userSessionManager.saveUserLoginSession(onlineUser.id.toLong(), username)
                            updateNavigationHeader()
                            Toast.makeText(this@MainActivity, "在线登录成功", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                        is NetworkService.ApiResult.Error -> {
                            // 在线登录失败
                            Toast.makeText(this@MainActivity, "登录失败：${result.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    loadingDialog.dismiss()
                    // 发生异常
                    Toast.makeText(this@MainActivity, "登录失败：${e.message}", Toast.LENGTH_SHORT).show()
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
            
            // 显示加载提示
            val loadingDialog = AlertDialog.Builder(this)
                .setMessage("正在注册...")
                .setCancelable(false)
                .create()
            loadingDialog.show()
            
            // 执行注册
            lifecycleScope.launch {
                try {
                    // 尝试在线注册
                    val result = networkService.register(username, password)
                    loadingDialog.dismiss()
                    
                    when (result) {
                        is NetworkService.ApiResult.Success -> {
                            // 在线注册成功，自动登录 (userId 从在线注册结果获取，如果API返回的话)
                            // 假设注册成功后，登录接口可以直接使用，或者注册接口返回用户ID
                            // 这里暂时不处理userId，因为UserSessionManager保存的是后台的userId
                            // 如果注册接口不直接返回userId，可能需要再次调用登录接口获取
                            // 或者修改UserSessionManager.saveUserLoginSession，如果 username 唯一且后端支持通过username获取id
                            // 简单处理：直接使用注册成功返回的用户名，ID由UserSessionManager内部处理或后续登录时获取
                            // 实际上，注册成功后，应该用返回的用户信息（包含ID）来保存会话
                            // 这里假设 networkService.register 返回的 User 对象包含 id
                            userSessionManager.saveUserLoginSession(result.data.id.toLong(), username) // 假设 result.data 是 User 类型且有 id
                            updateNavigationHeader()
                            Toast.makeText(this@MainActivity, "在线注册成功并已登录", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                        is NetworkService.ApiResult.Error -> {
                            // 在线注册失败
                            Toast.makeText(this@MainActivity, "注册失败：${result.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    loadingDialog.dismiss()
                    // 发生异常
                    Toast.makeText(this@MainActivity, "注册失败：${e.message}", Toast.LENGTH_SHORT).show()
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