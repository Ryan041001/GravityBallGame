package com.example.gravityballgame

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gravityballgame.data.AppDatabase
import com.example.gravityballgame.data.User
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LeaderboardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)
        
        // 设置返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "挑战模式排行榜"
        
        recyclerView = findViewById(R.id.leaderboard_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // 加载排行榜数据
        loadLeaderboardData()
    }
    
    private fun loadLeaderboardData() {
        val userDao = AppDatabase.getDatabase(this).userDao()
        
        lifecycleScope.launch {
            val topUsers = userDao.getTopChallengeUsers()
            adapter = LeaderboardAdapter(topUsers)
            recyclerView.adapter = adapter
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    
    // 排行榜适配器
    inner class LeaderboardAdapter(private val users: List<User>) : 
            RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {
        
        inner class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
            val rankText: android.widget.TextView = view.findViewById(R.id.rank_text)
            val usernameText: android.widget.TextView = view.findViewById(R.id.username_text)
            val timeText: android.widget.TextView = view.findViewById(R.id.time_text)
        }
        
        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_leaderboard, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val user = users[position]
            holder.rankText.text = (position + 1).toString()
            holder.usernameText.text = user.username
            
            // 格式化时间显示
            if (user.bestChallengeTime > 0) {
                holder.timeText.text = formatTime(user.bestChallengeTime)
            } else {
                holder.timeText.text = "未完成"
            }
        }
        
        override fun getItemCount() = users.size
        
        private fun formatTime(timeInMillis: Long): String {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) % 60
            val millis = timeInMillis % 1000
            return String.format("%02d:%02d.%03d", minutes, seconds, millis)
        }
    }
}
