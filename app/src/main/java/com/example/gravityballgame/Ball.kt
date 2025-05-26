package com.example.gravityballgame

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.abs
import kotlin.math.sqrt

class Ball(private var x: Float, private var y: Float, private val radius: Float) {
    private var velocityX = 100f
    private var velocityY = 100f
    private var accelerationX = 0f
    private var accelerationY = 0f
    private var screenWidth = 0
    private var screenHeight = 0
    private val friction = 0.99f // 物理摩擦系数，影响小球减速
    private val bounce = 0.7f // 弹性系数，控制碰撞后反弹强度
    private val minSpeedToStop = 0.1f // 最小速度阈值，低于此值时停止移动以防抖动
    private val maxSpeed = 35f // 小球最大速度上限，防止速度过高导致穿墙

    companion object {
        private const val EPSILON = 0.0001f
    }
    
    // 更新屏幕尺寸边界值
    fun updateScreenBounds(width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
    }
    
    // 设置小球受到的加速度向量
    fun setAcceleration(x: Float, y: Float) {
        accelerationX = x
        accelerationY = y
    }
    
    // 更新小球物理状态和位置
    fun update() {
        if (screenWidth <= 0 || screenHeight <= 0) return // 屏幕边界无效时中止更新
        
        // 根据当前加速度更新速度
        velocityX += accelerationX
        velocityY += accelerationY
        
        // 应用摩擦力减速
        velocityX *= friction
        velocityY *= friction

        // 限制最大移动速度
        val currentSpeed = sqrt(velocityX * velocityX + velocityY * velocityY)
        if (currentSpeed > maxSpeed) {
            val scale = maxSpeed / currentSpeed
            velocityX *= scale
            velocityY *= scale
        }
        
        // 计算下一帧位置
        val nextX = x + velocityX
        val nextY = y + velocityY
        
        // 屏幕边界碰撞检测与反弹处理
        if (nextX - radius < 0) {
            x = radius // 限制不超出左边界
            velocityX = -velocityX * bounce // 水平方向反弹
        } else if (nextX + radius > screenWidth) {
            x = screenWidth - radius // 限制不超出右边界
            velocityX = -velocityX * bounce // 水平方向反弹
        } else {
            x = nextX // 正常更新X坐标
        }
        
        if (nextY - radius < 0) {
            y = radius // 限制不超出上边界
            velocityY = -velocityY * bounce // 垂直方向反弹
        } else if (nextY + radius > screenHeight) {
            y = screenHeight - radius // 限制不超出下边界
            velocityY = -velocityY * bounce // 垂直方向反弹
        } else {
            y = nextY // 正常更新Y坐标
        }
        
        // 当速度低于阈值且无加速度时停止运动，防止微小抖动
        if (abs(velocityX) < minSpeedToStop && abs(accelerationX) < EPSILON) velocityX = 0f
        if (abs(velocityY) < minSpeedToStop && abs(accelerationY) < EPSILON) velocityY = 0f
    }
    
    // 渲染小球图形
    fun draw(canvas: Canvas, paint: Paint) {
        paint.color = Color.parseColor("#FF5722") // 设置小球颜色为深橙色
        canvas.drawCircle(x, y, radius, paint)
    }
    
    // 检测小球与障碍物的碰撞
    fun checkCollision(obstacle: Obstacle): Boolean {
        val rect = obstacle.getBounds()
        
        // 计算矩形上距离球心最近的点坐标
        val closestX = x.coerceIn(rect.left, rect.right)
        val closestY = y.coerceIn(rect.top, rect.bottom)
        
        // 计算球心到最近点的距离平方
        val distanceX = x - closestX
        val distanceY = y - closestY
        val distanceSquared = distanceX * distanceX + distanceY * distanceY
        
        // 判断距离是否小于球半径
        return distanceSquared < radius * radius
    }
    
    // 处理小球与障碍物的碰撞反弹
    fun handleCollision(obstacle: Obstacle) {
        val rect = obstacle.getBounds()

        // 计算矩形上距离球心最近的点
        val closestX = x.coerceIn(rect.left, rect.right)
        val closestY = y.coerceIn(rect.top, rect.bottom)

        // 计算从最近点指向球心的向量
        val dirToBallX = x - closestX
        val dirToBallY = y - closestY

        // 计算距离平方
        val distanceSq = dirToBallX * dirToBallX + dirToBallY * dirToBallY

        // 若距离大于等于半径，且非极小值，则无碰撞或仅为擦边
        if (distanceSq >= radius * radius && distanceSq > EPSILON) {
            return
        }

        // 计算精确距离
        val distance = sqrt(distanceSq)
        
        var normalX: Float // 碰撞法线X分量
        var normalY: Float // 碰撞法线Y分量
        var penetration: Float // 穿透深度

        if (distance > EPSILON) {
            // 标准情况：球心在障碍物外部，但球体与障碍物有交叉
            normalX = dirToBallX / distance // 归一化法线X分量
            normalY = dirToBallY / distance // 归一化法线Y分量
            penetration = radius - distance // 计算穿透深度
        } else {
            // 特殊情况：球心恰好在障碍物边缘或内部
            // 计算球心到障碍物四边的距离，找出最佳推出方向
            val distToLeft = x - rect.left
            val distToRight = rect.right - x
            val distToTop = y - rect.top
            val distToBottom = rect.bottom - y

            // 寻找最小非负距离作为推出方向
            var minDist = Float.MAX_VALUE
            normalX = 0f
            normalY = 0f

            // 检查左边距离
            if (distToLeft >= 0 && distToLeft < minDist) {
                minDist = distToLeft
                normalX = -1f; normalY = 0f
            }
            // 检查右边距离
            if (distToRight >= 0 && distToRight < minDist) {
                minDist = distToRight
                normalX = 1f; normalY = 0f
            }
            // 检查上边距离
            if (distToTop >= 0 && distToTop < minDist) {
                minDist = distToTop
                normalX = 0f; normalY = -1f
            }
            // 检查下边距离
            if (distToBottom >= 0 && distToBottom < minDist) {
                minDist = distToBottom
                normalX = 0f; normalY = 1f
            }
            
            // 若无法确定推出方向，基于当前速度选择反向
            if (normalX == 0f && normalY == 0f) {
                 // 极端情况处理：根据速度方向确定推出方向
                if (abs(velocityX) > abs(velocityY)) {
                    normalX = if (velocityX > 0) -1f else 1f; normalY = 0f
                } else {
                    normalY = if (velocityY > 0) -1f else 1f; normalX = 0f
                }
            }
            penetration = radius + minDist

            // 基于重叠情况计算更精确的穿透深度和法线
            val overlapX = (radius + (rect.right - rect.left) / 2) - abs(x - (rect.left + rect.right) / 2)
            val overlapY = (radius + (rect.bottom - rect.top) / 2) - abs(y - (rect.top + rect.bottom) / 2)

            // 选择较小的重叠轴作为碰撞主轴
            if (overlapX < overlapY) {
                penetration = overlapX
                normalX = if (x < (rect.left + rect.right) / 2) 1f else -1f // 法线指向远离矩形中心
                normalY = 0f
            } else {
                penetration = overlapY
                normalX = 0f
                normalY = if (y < (rect.top + rect.bottom) / 2) 1f else -1f // 法线指向远离矩形中心
            }
            // 确保穿透深度非负
            if (penetration < 0) penetration = 0f; // 无实际重叠，但距离很小
        }

        // 若存在有效穿透，执行碰撞响应
        if (penetration > 0) {
            // 1. 位置校正：将小球推出障碍物
            // 添加微小偏移量避免下一帧再次碰撞
            x += normalX * (penetration + 0.1f) 
            y += normalY * (penetration + 0.1f)

            // 2. 速度反弹：计算并应用反射向量
            val dotProduct = velocityX * normalX + velocityY * normalY
            if (dotProduct < 0) { // 确保小球正在向障碍物移动
                // 计算反射后的速度向量，并应用弹性系数
                velocityX -= 2 * dotProduct * normalX * bounce
                velocityY -= 2 * dotProduct * normalY * bounce
            }
        }
    }
    
    // 检测小球是否到达终点区域
    fun checkGoal(goal: Goal): Boolean {
        val rect = goal.getBounds()
        return x > rect.left && x < rect.right && y > rect.top && y < rect.bottom
    }
    
    // 重置小球状态至初始位置
    fun reset(newX: Float, newY: Float) {
        x = newX
        y = newY
        velocityX = 0f
        velocityY = 0f
        accelerationX = 0f
        accelerationY = 0f
    }
    
    // 获取小球当前X坐标
    fun getX(): Float {
        return x
    }
    
    // 获取小球当前Y坐标
    fun getY(): Float {
        return y
    }
    
    // 获取小球半径
    fun getRadius(): Float {
        return radius
    }
}

// 障碍物类：定义游戏中的障碍物或安全路径
class Obstacle(
    private var left: Float,
    private var top: Float,
    private var right: Float,
    private var bottom: Float,
    val isTrap: Boolean = false // 标记是否为危险陷阱，true为陷阱，false为安全路径
) {
    private val bounds = RectF(left, top, right, bottom)
    private var cornerRadius = 15f // 障碍物圆角半径，影响视觉效果
    
    // 获取障碍物边界矩形
    fun getBounds(): RectF {
        return bounds
    }
    
    // 调整障碍物位置与尺寸
    fun adjustBounds(newLeft: Float, newTop: Float, newRight: Float, newBottom: Float) {
        left = newLeft
        top = newTop
        right = newRight
        bottom = newBottom
        bounds.set(left, top, right, bottom)
    }
    
    // 设置障碍物圆角半径
    fun setCornerRadius(radius: Float) {
        cornerRadius = radius
    }
    
    // 渲染障碍物图形
    fun draw(canvas: Canvas, paint: Paint) {
        paint.color = if (isTrap) Color.parseColor("#D32F2F") else Color.parseColor("#455A64") // 陷阱显示为红色，安全路径为深蓝灰色
        
        // 根据圆角设置选择适当的绘制方法
        if (cornerRadius > 0f) {
            canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, paint) // 绘制圆角矩形
        } else {
            canvas.drawRect(bounds, paint) // 绘制标准矩形
        }
    }
}

// 终点类：定义游戏的目标区域
class Goal(private var left: Float, private var top: Float, private var right: Float, private var bottom: Float) {
    private val bounds = RectF(left, top, right, bottom)
    private val cornerRadius = 20f // 终点区域圆角半径
    
    // 获取终点区域边界矩形
    fun getBounds(): RectF {
        return bounds
    }
    
    // 调整终点区域位置与尺寸
    fun adjustBounds(newLeft: Float, newTop: Float, newRight: Float, newBottom: Float) {
        left = newLeft
        top = newTop
        right = newRight
        bottom = newBottom
        bounds.set(left, top, right, bottom)
    }
    
    // 渲染终点区域图形
    fun draw(canvas: Canvas, paint: Paint) {
        paint.color = Color.parseColor("#4CAF50") // 终点区域使用绿色表示
        canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, paint)
    }
}