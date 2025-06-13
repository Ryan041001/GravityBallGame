from app import app, db, User, Score
from datetime import datetime

def init_database():
    """初始化数据库并添加测试数据"""
    with app.app_context():
        # 删除所有表并重新创建
        db.drop_all()
        db.create_all()
        
        # 创建测试用户
        test_users = [
            {'username': 'player1', 'password': '123456', 'email': 'player1@example.com'},
            {'username': 'player2', 'password': '123456', 'email': 'player2@example.com'},
            {'username': 'player3', 'password': '123456', 'email': 'player3@example.com'},
            {'username': 'admin', 'password': 'admin123', 'email': 'admin@example.com'}
        ]
        
        users = []
        for user_data in test_users:
            user = User(
                username=user_data['username'],
                email=user_data['email']
            )
            user.set_password(user_data['password'])
            db.session.add(user)
            users.append(user)
        
        db.session.commit()
        
        # 创建测试成绩数据
        test_scores = [
            # 标准模式成绩
            {'user_id': 1, 'level_type': 'standard', 'level_number': 1, 'completion_time': 15.5, 'score': 850, 'difficulty': 'easy'},
            {'user_id': 1, 'level_type': 'standard', 'level_number': 2, 'completion_time': 22.3, 'score': 780, 'difficulty': 'easy'},
            {'user_id': 1, 'level_type': 'standard', 'level_number': 3, 'completion_time': 35.8, 'score': 650, 'difficulty': 'medium'},
            
            {'user_id': 2, 'level_type': 'standard', 'level_number': 1, 'completion_time': 12.8, 'score': 920, 'difficulty': 'easy'},
            {'user_id': 2, 'level_type': 'standard', 'level_number': 2, 'completion_time': 18.9, 'score': 860, 'difficulty': 'easy'},
            {'user_id': 2, 'level_type': 'standard', 'level_number': 4, 'completion_time': 45.2, 'score': 580, 'difficulty': 'hard'},
            
            {'user_id': 3, 'level_type': 'standard', 'level_number': 1, 'completion_time': 20.1, 'score': 750, 'difficulty': 'easy'},
            {'user_id': 3, 'level_type': 'standard', 'level_number': 2, 'completion_time': 28.5, 'score': 680, 'difficulty': 'medium'},
            
            # 挑战模式成绩
            {'user_id': 1, 'level_type': 'challenge', 'level_number': None, 'completion_time': 65.3, 'score': 1200, 'difficulty': 'hard'},
            {'user_id': 2, 'level_type': 'challenge', 'level_number': None, 'completion_time': 58.7, 'score': 1350, 'difficulty': 'hard'},
            {'user_id': 3, 'level_type': 'challenge', 'level_number': None, 'completion_time': 72.1, 'score': 1100, 'difficulty': 'hard'},
            
            # 自定义模式成绩
            {'user_id': 1, 'level_type': 'custom', 'level_number': None, 'completion_time': 30.2, 'score': 800, 'difficulty': 'medium'},
            {'user_id': 2, 'level_type': 'custom', 'level_number': None, 'completion_time': 25.8, 'score': 900, 'difficulty': 'medium'},
            {'user_id': 3, 'level_type': 'custom', 'level_number': None, 'completion_time': 40.5, 'score': 700, 'difficulty': 'easy'}
        ]
        
        for score_data in test_scores:
            score = Score(**score_data)
            db.session.add(score)
        
        db.session.commit()
        
        print("数据库初始化完成！")
        print(f"创建了 {len(test_users)} 个测试用户")
        print(f"创建了 {len(test_scores)} 条测试成绩")
        print("\n测试用户账号:")
        for user_data in test_users:
            print(f"用户名: {user_data['username']}, 密码: {user_data['password']}")

if __name__ == '__main__':
    init_database()