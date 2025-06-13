#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import sys
from app import app, db

def start_server():
    """启动Flask服务器"""
    print("\n" + "="*50)
    print("重力球游戏 Flask 后端服务")
    print("="*50)
    
    # 创建数据库表
    with app.app_context():
        try:
            db.create_all()
            print("✓ 数据库初始化成功")
        except Exception as e:
            print(f"✗ 数据库初始化失败: {e}")
            return False
    
    # 启动服务器
    try:
        print(f"✓ 服务器启动中...")
        print(f"✓ 本地访问地址: http://127.0.0.1:5001")
        print(f"✓ 网络访问地址: http://localhost:5001")
        print("="*50 + "\n")
        
        # 使用简单配置启动
        app.run(
            host='0.0.0.0',  # 监听所有网络接口
            port=5001,
            debug=False,        # 关闭调试模式
            threaded=True,      # 启用多线程
            use_reloader=False  # 关闭自动重载
        )
        
    except KeyboardInterrupt:
        print("\n服务器已停止")
        return True
    except Exception as e:
        print(f"\n服务器启动失败: {e}")
        return False

if __name__ == '__main__':
    start_server()