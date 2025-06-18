#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import sys
import subprocess
from pathlib import Path

def check_python_version():
    """检查Python版本"""
    if sys.version_info < (3, 7):
        print("错误: 需要Python 3.7或更高版本")
        sys.exit(1)
    print(f"Python版本: {sys.version}")

def install_requirements():
    """安装依赖包"""
    requirements_file = Path(__file__).parent / 'requirements.txt'
    if requirements_file.exists():
        print("正在安装依赖包...")
        try:
            subprocess.check_call([sys.executable, '-m', 'pip', 'install', '-r', str(requirements_file)])
            print("依赖包安装完成")
        except subprocess.CalledProcessError as e:
            print(f"安装依赖包失败: {e}")
            print("请手动运行: pip install -r requirements.txt")
            return False
    else:
        print("未找到requirements.txt文件")
    return True

def init_database():
    """初始化数据库"""
    init_script = Path(__file__).parent / 'init_db.py'
    if init_script.exists():
        print("正在初始化数据库...")
        try:
            subprocess.check_call([sys.executable, str(init_script)])
            print("数据库初始化完成")
        except subprocess.CalledProcessError as e:
            print(f"数据库初始化失败: {e}")
            return False
    return True

def start_flask_app():
    """启动Flask应用"""
    app_file = Path(__file__).parent / 'app.py'
    if app_file.exists():
        print("正在启动Flask服务器...")
        print("服务器地址: http://localhost:5001")
        print("API文档: http://localhost:5001/api/health")
        print("按 Ctrl+C 停止服务器")
        print("-" * 50)
        
        try:
            # 设置环境变量
            env = os.environ.copy()
            env['FLASK_APP'] = str(app_file)
            env['FLASK_ENV'] = 'development'
            
            # 启动Flask应用
            subprocess.check_call([sys.executable, str(app_file)], env=env)
        except KeyboardInterrupt:
            print("\n服务器已停止")
        except subprocess.CalledProcessError as e:
            print(f"启动Flask应用失败: {e}")
            return False
    else:
        print("未找到app.py文件")
        return False
    return True

def main():
    """主函数"""
    print("=" * 60)
    print("重力球游戏 - Flask后端服务启动器")
    print("=" * 60)
    
    # 检查Python版本
    check_python_version()
    
    # 切换到脚本目录
    script_dir = Path(__file__).parent
    os.chdir(script_dir)
    print(f"工作目录: {script_dir}")
    
    # 安装依赖
    if not install_requirements():
        print("依赖安装失败，但继续尝试启动...")
    
    # 检查是否需要初始化数据库
    db_file = script_dir / 'game_data.db'
    if not db_file.exists():
        print("检测到首次运行，正在初始化数据库...")
        if not init_database():
            print("数据库初始化失败")
            return
    else:
        print("数据库已存在，跳过初始化")
    
    # 启动Flask应用
    start_flask_app()

if __name__ == '__main__':
    main()