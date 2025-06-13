package com.example.gravityballgame.network

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * 网络服务类，负责与Flask后端API通信
 */
class NetworkService(private val context: Context) {
    
    companion object {
        private const val TAG = "NetworkService"
        // 服务器地址 - 请根据实际情况修改
        private const val BASE_URL = "http://192.168.159.212:5001/api" // 本机实际IP地址
        
        private const val TIMEOUT_CONNECT = 30000 // 连接超时 30秒
        private const val TIMEOUT_READ = 30000 // 读取超时 30秒
    }
    
    /**
     * 用户数据类
     */
    data class User(
        val id: Int,
        val username: String,
        val email: String?,
        val createdAt: String
    )
    
    /**
     * 成绩数据类
     */
    data class Score(
        val id: Int,
        val userId: Int,
        val username: String,
        val levelType: String,
        val levelNumber: Int?,
        val completionTime: Double,
        val score: Int,
        val difficulty: String?,
        val createdAt: String,
        val rank: Int? = null
    )
    
    /**
     * API响应结果类
     */
    sealed class ApiResult<T> {
        data class Success<T>(val data: T) : ApiResult<T>()
        data class Error<T>(val message: String, val code: Int = -1) : ApiResult<T>()
    }
    
    /**
     * 发送HTTP请求的通用方法
     */
    private suspend fun sendRequest(
        endpoint: String,
        method: String = "GET",
        jsonData: JSONObject? = null
    ): ApiResult<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL$endpoint")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = method
                connectTimeout = TIMEOUT_CONNECT
                readTimeout = TIMEOUT_READ
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                
                if (method != "GET" && jsonData != null) {
                    doOutput = true
                    val writer = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
                    writer.write(jsonData.toString())
                    writer.flush()
                    writer.close()
                }
            }
            
            val responseCode = connection.responseCode
            val inputStream = if (responseCode >= 200 && responseCode < 300) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            
            val reader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
            val response = reader.readText()
            reader.close()
            connection.disconnect()
            
            Log.d(TAG, "Request: $method $endpoint")
            Log.d(TAG, "Response Code: $responseCode")
            Log.d(TAG, "Response: $response")
            
            val jsonResponse = JSONObject(response)
            
            if (responseCode >= 200 && responseCode < 300) {
                ApiResult.Success(jsonResponse)
            } else {
                val errorMessage = jsonResponse.optString("error", "未知错误")
                ApiResult.Error(errorMessage, responseCode)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Network request failed", e)
            ApiResult.Error("网络请求失败: ${e.message}")
        }
    }
    
    /**
     * 健康检查
     */
    suspend fun healthCheck(): ApiResult<String> {
        return when (val result = sendRequest("/health")) {
            is ApiResult.Success -> {
                val status = result.data.optString("status", "unknown")
                ApiResult.Success(status)
            }
            is ApiResult.Error -> ApiResult.Error(result.message, result.code)
        }
    }
    
    /**
     * 用户注册
     */
    suspend fun register(username: String, password: String, email: String? = null): ApiResult<User> {
        val jsonData = JSONObject().apply {
            put("username", username)
            put("password", password)
            if (!email.isNullOrBlank()) {
                put("email", email)
            }
        }
        
        return when (val result = sendRequest("/register", "POST", jsonData)) {
            is ApiResult.Success -> {
                try {
                    val userJson = result.data.getJSONObject("user")
                    val user = User(
                        id = userJson.getInt("id"),
                        username = userJson.getString("username"),
                        email = userJson.optString("email"),
                        createdAt = userJson.getString("created_at")
                    )
                    ApiResult.Success(user)
                } catch (e: Exception) {
                    ApiResult.Error("解析用户数据失败: ${e.message}")
                }
            }
            is ApiResult.Error -> ApiResult.Error(result.message, result.code)
        }
    }
    
    /**
     * 用户登录
     */
    suspend fun login(username: String, password: String): ApiResult<User> {
        val jsonData = JSONObject().apply {
            put("username", username)
            put("password", password)
        }
        
        return when (val result = sendRequest("/login", "POST", jsonData)) {
            is ApiResult.Success -> {
                try {
                    val userJson = result.data.getJSONObject("user")
                    val user = User(
                        id = userJson.getInt("id"),
                        username = userJson.getString("username"),
                        email = userJson.optString("email"),
                        createdAt = userJson.getString("created_at")
                    )
                    ApiResult.Success(user)
                } catch (e: Exception) {
                    ApiResult.Error("解析用户数据失败: ${e.message}")
                }
            }
            is ApiResult.Error -> ApiResult.Error(result.message, result.code)
        }
    }
    
    /**
     * 上传成绩
     */
    suspend fun uploadScore(
        userId: Int,
        levelType: String,
        completionTime: Double,
        score: Int,
        levelNumber: Int? = null,
        difficulty: String? = null
    ): ApiResult<Score> {
        val jsonData = JSONObject().apply {
            put("user_id", userId)
            put("level_type", levelType)
            put("completion_time", completionTime)
            put("score", score)
            levelNumber?.let { put("level_number", it) }
            difficulty?.let { put("difficulty", it) }
        }
        
        return when (val result = sendRequest("/scores", "POST", jsonData)) {
            is ApiResult.Success -> {
                try {
                    val scoreJson = result.data.getJSONObject("score")
                    val scoreObj = Score(
                        id = scoreJson.getInt("id"),
                        userId = scoreJson.getInt("user_id"),
                        username = scoreJson.getString("username"),
                        levelType = scoreJson.getString("level_type"),
                        levelNumber = scoreJson.optInt("level_number"),
                        completionTime = scoreJson.getDouble("completion_time"),
                        score = scoreJson.getInt("score"),
                        difficulty = scoreJson.optString("difficulty"),
                        createdAt = scoreJson.getString("created_at")
                    )
                    ApiResult.Success(scoreObj)
                } catch (e: Exception) {
                    ApiResult.Error("解析成绩数据失败: ${e.message}")
                }
            }
            is ApiResult.Error -> ApiResult.Error(result.message, result.code)
        }
    }
    
    /**
     * 获取排行榜
     */
    suspend fun getLeaderboard(
        levelType: String = "all",
        levelNumber: Int? = null,
        difficulty: String? = null,
        limit: Int = 50
    ): ApiResult<List<Score>> {
        val params = mutableListOf<String>().apply {
            add("level_type=$levelType")
            add("limit=$limit")
            levelNumber?.let { add("level_number=$it") }
            difficulty?.let { add("difficulty=$it") }
        }
        val queryString = params.joinToString("&")
        
        return when (val result = sendRequest("/leaderboard?$queryString")) {
            is ApiResult.Success -> {
                try {
                    val leaderboardArray = result.data.getJSONArray("leaderboard")
                    val scores = mutableListOf<Score>()
                    
                    for (i in 0 until leaderboardArray.length()) {
                        val scoreJson = leaderboardArray.getJSONObject(i)
                        val score = Score(
                            id = scoreJson.getInt("id"),
                            userId = scoreJson.getInt("user_id"),
                            username = scoreJson.getString("username"),
                            levelType = scoreJson.getString("level_type"),
                            levelNumber = scoreJson.optInt("level_number"),
                            completionTime = scoreJson.getDouble("completion_time"),
                            score = scoreJson.getInt("score"),
                            difficulty = scoreJson.optString("difficulty"),
                            createdAt = scoreJson.getString("created_at"),
                            rank = scoreJson.optInt("rank")
                        )
                        scores.add(score)
                    }
                    
                    ApiResult.Success(scores)
                } catch (e: Exception) {
                    ApiResult.Error("解析排行榜数据失败: ${e.message}")
                }
            }
            is ApiResult.Error -> ApiResult.Error(result.message, result.code)
        }
    }
    
    /**
     * 获取用户个人成绩
     */
    suspend fun getUserScores(
        userId: Int,
        levelType: String? = null,
        limit: Int = 20
    ): ApiResult<Pair<User, List<Score>>> {
        val params = mutableListOf<String>().apply {
            add("limit=$limit")
            levelType?.let { add("level_type=$it") }
        }
        val queryString = if (params.isNotEmpty()) "?${params.joinToString("&")}" else ""
        
        return when (val result = sendRequest("/user/$userId/scores$queryString")) {
            is ApiResult.Success -> {
                try {
                    val userJson = result.data.getJSONObject("user")
                    val user = User(
                        id = userJson.getInt("id"),
                        username = userJson.getString("username"),
                        email = userJson.optString("email"),
                        createdAt = userJson.getString("created_at")
                    )
                    
                    val scoresArray = result.data.getJSONArray("scores")
                    val scores = mutableListOf<Score>()
                    
                    for (i in 0 until scoresArray.length()) {
                        val scoreJson = scoresArray.getJSONObject(i)
                        val score = Score(
                            id = scoreJson.getInt("id"),
                            userId = scoreJson.getInt("user_id"),
                            username = scoreJson.getString("username"),
                            levelType = scoreJson.getString("level_type"),
                            levelNumber = scoreJson.optInt("level_number"),
                            completionTime = scoreJson.getDouble("completion_time"),
                            score = scoreJson.getInt("score"),
                            difficulty = scoreJson.optString("difficulty"),
                            createdAt = scoreJson.getString("created_at")
                        )
                        scores.add(score)
                    }
                    
                    ApiResult.Success(Pair(user, scores))
                } catch (e: Exception) {
                    ApiResult.Error("解析用户成绩数据失败: ${e.message}")
                }
            }
            is ApiResult.Error -> ApiResult.Error(result.message, result.code)
        }
    }
    
    /**
     * 获取统计信息
     */
    suspend fun getStats(): ApiResult<JSONObject> {
        return when (val result = sendRequest("/stats")) {
            is ApiResult.Success -> ApiResult.Success(result.data)
            is ApiResult.Error -> ApiResult.Error(result.message, result.code)
        }
    }
}