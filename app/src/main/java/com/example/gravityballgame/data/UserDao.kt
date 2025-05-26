package com.example.gravityballgame.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * 用户数据访问对象，提供对用户表的操作
 */
@Dao
interface UserDao {
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User): Long
    
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?
    
    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    suspend fun login(username: String, password: String): User?
    
    @Update
    suspend fun updateUser(user: User)
    
    @Query("SELECT * FROM users ORDER BY bestChallengeTime ASC LIMIT 10")
    suspend fun getTopChallengeUsers(): List<User>
    
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Long): User?
}
