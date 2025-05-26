package com.example.gravityballgame.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户实体类，用于存储用户信息
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val password: String,
    val bestChallengeTime: Long = 0 // 挑战模式最佳时间，单位为毫秒，0表示未完成
)
