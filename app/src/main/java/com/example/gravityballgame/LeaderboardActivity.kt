package com.example.gravityballgame

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.gravityballgame.data.UserSessionManager
import com.example.gravityballgame.network.NetworkService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecyclerView.Adapter<*>
    private lateinit var buttonRefresh: Button
    private lateinit var buttonMyScores: Button
    private lateinit var textViewStatus: TextView
    private lateinit var progressBar: ProgressBar
    
    private lateinit var networkService: NetworkService
    private lateinit var userSessionManager: UserSessionManager
    
    private val onlineScores = mutableListOf<NetworkService.Score>()
    private var showingMyScores: Boolean = false // 新增状态变量

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)
        
        // 设置返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "挑战模式排行榜"
        
        // 初始化网络服务
        networkService = NetworkService(this)
        userSessionManager = UserSessionManager(this)
        
        // 初始化UI
        initViews()
        setupButtons()
        
        recyclerView = findViewById(R.id.leaderboard_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // 直接显示在线挑战模式排行榜
        loadOnlineLeaderboard() // 初始加载全部排行榜，如果已登录则内部会切换到个人
    }
    
    private fun initViews() {
        progressBar = findViewById(R.id.progressBar)
        textViewStatus = findViewById(R.id.textViewStatus)
        buttonRefresh = findViewById(R.id.buttonRefresh)
        buttonMyScores = findViewById(R.id.buttonMyScores)
        
        // 隐藏关卡类型选择器
        findViewById<Spinner>(R.id.spinnerLevelType).visibility = View.GONE
        
        // 更新状态文本
        textViewStatus.text = "挑战模式在线排行榜"
    }
    

    
    private fun setupButtons() {
        buttonRefresh.setOnClickListener {
            // 根据当前状态刷新对应的排行榜
            if (showingMyScores) {
                loadMyOnlineScores()
            } else {
                loadOnlineLeaderboard(false) // 传入false表示不是初始加载
            }
        }
        
        updateButtonMyScoresText() // 初始化按钮文本

        buttonMyScores.setOnClickListener {
            if (userSessionManager.isLoggedIn()) {
                if (showingMyScores) {
                    // 当前显示个人成绩，切换到全部排行榜
                    loadOnlineLeaderboard(false)
                } else {
                    // 当前显示全部排行榜，切换到个人成绩
                    loadMyOnlineScores()
                }
            } else {
                Toast.makeText(this, "🔐 请先登录查看个人成绩", Toast.LENGTH_SHORT).show()
            }
        }
        
        // 更新用户界面
        updateUserInterface()
    }

    private fun updateButtonMyScoresText() {
        if (userSessionManager.isLoggedIn()) {
            buttonMyScores.text = if (showingMyScores) "查看全部排行" else "查看我的排行"
        } else {
            buttonMyScores.text = "我的成绩"
        }
    }
    

    
    private fun updateUserInterface() {
        if (userSessionManager.isLoggedIn()) {
            buttonMyScores.visibility = View.VISIBLE
        } else {
            buttonMyScores.visibility = View.GONE
        }
        updateButtonMyScoresText() // 确保按钮文本在登录状态改变时也更新
    }
    

    
    private fun loadOnlineLeaderboard(isInitialLoad: Boolean = true) { //增加参数以区分初始加载
        // 移除初始加载时自动切换到个人成绩的逻辑
        // if (isInitialLoad && userSessionManager.isLoggedIn()) {
        //    loadMyOnlineScores()
        //    return
        // }
        
        showLoading(true)
        textViewStatus.text = "正在加载挑战模式排行榜..."
        showingMyScores = false // 确保在加载全部排行榜时状态正确
        updateButtonMyScoresText() // 更新按钮文本
        
        lifecycleScope.launch {
            try {
                // 只加载挑战模式的排行榜
                val result = networkService.getLeaderboard(
                    levelType = "challenge",
                    limit = 100
                )
                
                when (result) {
                    is NetworkService.ApiResult.Success -> {
                        onlineScores.clear()
                        onlineScores.addAll(result.data)
                        adapter = OnlineLeaderboardAdapter(onlineScores)
                        recyclerView.adapter = adapter
                        textViewStatus.text = "🏆 挑战模式排行榜 (${result.data.size} 条记录)"
                        // showingMyScores = false // 状态已在函数开头设置
                        // updateButtonMyScoresText() // 按钮文本已在函数开头更新
                        
                        if (result.data.isEmpty()) {
                            showEmptyState()
                        }
                    }
                    is NetworkService.ApiResult.Error -> {
                        showErrorState("加载失败: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                showErrorState("网络连接失败，请检查网络设置")
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun showEmptyState() {
        textViewStatus.text = "📝 暂无挑战模式成绩记录"
        // 显示空状态的友好提示
        val emptyScores = listOf<NetworkService.Score>()
        adapter = OnlineLeaderboardAdapter(emptyScores)
        recyclerView.adapter = adapter
    }
    
    private fun showErrorState(message: String) {
        textViewStatus.text = "❌ $message"
        Toast.makeText(this@LeaderboardActivity, message, Toast.LENGTH_LONG).show()
        
        // 显示重试按钮提示
        val emptyScores = listOf<NetworkService.Score>()
        adapter = OnlineLeaderboardAdapter(emptyScores)
        recyclerView.adapter = adapter
    }
    
    private fun loadMyOnlineScores() {
        if (!userSessionManager.isLoggedIn()) {
            Toast.makeText(this, "🔐 请先登录查看个人成绩", Toast.LENGTH_SHORT).show()
            return
        }
        
        val userId = userSessionManager.getUserId().toInt()
        
        showLoading(true)
        textViewStatus.text = "正在加载个人挑战模式成绩..."
        
        lifecycleScope.launch {
            try {
                // 只加载挑战模式的个人成绩
                val result = networkService.getUserScores(
                    userId = userId,
                    levelType = "challenge",
                    limit = 50
                )
                
                when (result) {
                    is NetworkService.ApiResult.Success -> {
                        val (user, scores) = result.data
                        onlineScores.clear()
                        onlineScores.addAll(scores)
                        adapter = OnlineLeaderboardAdapter(onlineScores, isPersonalScores = true)
                        recyclerView.adapter = adapter
                        textViewStatus.text = "👤 ${user.username} 的挑战成绩 (${scores.size} 条记录)"
                        showingMyScores = true // 更新状态
                        updateButtonMyScoresText() // 确保在加载个人成绩后按钮文本正确更新
                        
                        if (scores.isEmpty()) {
                            textViewStatus.text = "🎮 您还没有挑战模式成绩，快去挑战吧！"
                        }
                    }
                    is NetworkService.ApiResult.Error -> {
                        showErrorState("加载个人成绩失败: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                showErrorState("网络连接失败，请检查网络设置")
            } finally {
                showLoading(false)
            }
        }
    }
    

    
    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        buttonRefresh.isEnabled = !show
        buttonMyScores.isEnabled = !show
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    

    
    // 在线排行榜适配器
    inner class OnlineLeaderboardAdapter(
        private val scores: List<NetworkService.Score>,
        private val isPersonalScores: Boolean = false
    ) : RecyclerView.Adapter<OnlineLeaderboardAdapter.ViewHolder>() {
        
        inner class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
            val rankText: android.widget.TextView = view.findViewById(R.id.rank_text)
            val usernameText: android.widget.TextView = view.findViewById(R.id.username_text)
            val timeText: android.widget.TextView = view.findViewById(R.id.time_text)
            val scoreText: android.widget.TextView = view.findViewById(R.id.score_text)
            val levelText: android.widget.TextView = view.findViewById(R.id.level_text)
        }
        
        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_online_leaderboard, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val score = scores[position]
            
            if (isPersonalScores) {
                holder.rankText.text = (position + 1).toString()
            } else {
                holder.rankText.text = score.rank?.toString() ?: (position + 1).toString()
            }
            
            holder.usernameText.text = score.username
            holder.timeText.text = String.format("%.2fs", score.completionTime)
            
            // 隐藏分数显示
            holder.scoreText.visibility = View.GONE
            
            // 只显示挑战模式，不显示难度
            holder.levelText.text = "🎯 挑战模式"
            
            // 点击查看详情
            holder.itemView.setOnClickListener {
                showScoreDetails(score)
            }
        }
        
        override fun getItemCount() = scores.size
        
        private fun getLevelTypeDisplayName(levelType: String): String {
            return when (levelType) {
                "standard" -> "标准"
                "custom" -> "自定义"
                "challenge" -> "挑战"
                else -> levelType
            }
        }
        
        private fun showScoreDetails(score: NetworkService.Score) {
            // 只显示挑战模式信息，不显示难度和分数
            val message = "玩家: ${score.username}\n" +
                    "关卡: 挑战模式\n" +
                    "完成时间: ${String.format("%.2f", score.completionTime)}秒\n" +
                    "记录时间: ${score.createdAt}"
            
            androidx.appcompat.app.AlertDialog.Builder(this@LeaderboardActivity)
                .setTitle("成绩详情")
                .setMessage(message)
                .setPositiveButton("确定", null)
                .show()
        }
    }
}
