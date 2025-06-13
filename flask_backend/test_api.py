import requests
import json
from datetime import datetime

# 服务器地址
BASE_URL = 'http://127.0.0.1:5001/api'

def test_health_check():
    """测试健康检查接口"""
    print("\n=== 测试健康检查 ===")
    try:
        response = requests.get(f'{BASE_URL}/health')
        print(f"状态码: {response.status_code}")
        print(f"响应: {response.json()}")
        return response.status_code == 200
    except Exception as e:
        print(f"错误: {e}")
        return False

def test_register():
    """测试用户注册"""
    print("\n=== 测试用户注册 ===")
    test_user = {
        'username': 'testuser',
        'password': 'testpass123',
        'email': 'test@example.com'
    }
    
    try:
        response = requests.post(f'{BASE_URL}/register', json=test_user)
        print(f"状态码: {response.status_code}")
        print(f"响应: {response.json()}")
        return response.status_code == 201
    except Exception as e:
        print(f"错误: {e}")
        return False

def test_login():
    """测试用户登录"""
    print("\n=== 测试用户登录 ===")
    login_data = {
        'username': 'player1',
        'password': '123456'
    }
    
    try:
        response = requests.post(f'{BASE_URL}/login', json=login_data)
        print(f"状态码: {response.status_code}")
        result = response.json()
        print(f"响应: {result}")
        
        if response.status_code == 200:
            return result.get('user', {}).get('id')
        return None
    except Exception as e:
        print(f"错误: {e}")
        return None

def test_upload_score(user_id):
    """测试成绩上传"""
    print("\n=== 测试成绩上传 ===")
    score_data = {
        'user_id': user_id,
        'level_type': 'standard',
        'level_number': 5,
        'completion_time': 42.5,
        'score': 950,
        'difficulty': 'medium'
    }
    
    try:
        response = requests.post(f'{BASE_URL}/scores', json=score_data)
        print(f"状态码: {response.status_code}")
        print(f"响应: {response.json()}")
        return response.status_code == 201
    except Exception as e:
        print(f"错误: {e}")
        return False

def test_leaderboard():
    """测试排行榜获取"""
    print("\n=== 测试排行榜获取 ===")
    
    # 测试全部排行榜
    try:
        response = requests.get(f'{BASE_URL}/leaderboard')
        print(f"全部排行榜 - 状态码: {response.status_code}")
        result = response.json()
        print(f"排行榜条目数: {result.get('total_count', 0)}")
        
        if result.get('leaderboard'):
            print("前3名:")
            for i, entry in enumerate(result['leaderboard'][:3]):
                print(f"  {i+1}. {entry['username']} - 分数: {entry['score']} - 时间: {entry['completion_time']}s")
    except Exception as e:
        print(f"错误: {e}")
    
    # 测试标准模式排行榜
    try:
        response = requests.get(f'{BASE_URL}/leaderboard?level_type=standard')
        print(f"\n标准模式排行榜 - 状态码: {response.status_code}")
        result = response.json()
        print(f"排行榜条目数: {result.get('total_count', 0)}")
    except Exception as e:
        print(f"错误: {e}")
    
    # 测试挑战模式排行榜
    try:
        response = requests.get(f'{BASE_URL}/leaderboard?level_type=challenge')
        print(f"\n挑战模式排行榜 - 状态码: {response.status_code}")
        result = response.json()
        print(f"排行榜条目数: {result.get('total_count', 0)}")
    except Exception as e:
        print(f"错误: {e}")

def test_user_scores(user_id):
    """测试用户个人成绩获取"""
    print("\n=== 测试用户个人成绩 ===")
    try:
        response = requests.get(f'{BASE_URL}/user/{user_id}/scores')
        print(f"状态码: {response.status_code}")
        result = response.json()
        print(f"用户: {result.get('user', {}).get('username')}")
        print(f"成绩记录数: {len(result.get('scores', []))}")
        
        if result.get('scores'):
            print("最近的成绩:")
            for score in result['scores'][:3]:
                print(f"  {score['level_type']} - 分数: {score['score']} - 时间: {score['completion_time']}s")
    except Exception as e:
        print(f"错误: {e}")

def test_stats():
    """测试统计信息获取"""
    print("\n=== 测试统计信息 ===")
    try:
        response = requests.get(f'{BASE_URL}/stats')
        print(f"状态码: {response.status_code}")
        result = response.json()
        print(f"总用户数: {result.get('total_users')}")
        print(f"总成绩数: {result.get('total_scores')}")
        print(f"模式统计: {result.get('mode_stats')}")
        
        highest = result.get('highest_score')
        if highest:
            print(f"最高分: {highest['score']} (用户: {highest['username']})")
    except Exception as e:
        print(f"错误: {e}")

def main():
    """运行所有测试"""
    print("开始API测试...")
    print(f"测试时间: {datetime.now()}")
    
    # 健康检查
    if not test_health_check():
        print("健康检查失败，请确保服务器正在运行")
        return
    
    # 用户注册（可能失败，如果用户已存在）
    test_register()
    
    # 用户登录
    user_id = test_login()
    if not user_id:
        print("登录失败，无法继续测试")
        return
    
    # 成绩上传
    test_upload_score(user_id)
    
    # 排行榜
    test_leaderboard()
    
    # 用户个人成绩
    test_user_scores(user_id)
    
    # 统计信息
    test_stats()
    
    print("\n=== 测试完成 ===")

if __name__ == '__main__':
    main()