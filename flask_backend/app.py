from flask import Flask, request, jsonify
from flask_sqlalchemy import SQLAlchemy
from flask_cors import CORS
from werkzeug.security import generate_password_hash, check_password_hash
from datetime import datetime
import os

app = Flask(__name__)
CORS(app, resources={r"/api/*": {"origins": ["*"], "methods": ["GET", "POST", "PUT", "DELETE"], "allow_headers": ["Content-Type"]}})  # 配置跨域请求，允许所有来源

# 数据库配置
basedir = os.path.abspath(os.path.dirname(__file__))
app.config['SQLALCHEMY_DATABASE_URI'] = f'sqlite:///{os.path.join(basedir, "game_data.db")}'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
app.config['SECRET_KEY'] = 'your-secret-key-here'

db = SQLAlchemy(app)

# 用户模型
class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(80), unique=True, nullable=False)
    password_hash = db.Column(db.String(120), nullable=False)
    email = db.Column(db.String(120), unique=True, nullable=True)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    
    # 关系：一个用户可以有多个成绩记录
    scores = db.relationship('Score', backref='user', lazy=True, cascade='all, delete-orphan')
    
    def set_password(self, password):
        self.password_hash = generate_password_hash(password)
    
    def check_password(self, password):
        return check_password_hash(self.password_hash, password)
    
    def to_dict(self):
        return {
            'id': self.id,
            'username': self.username,
            'email': self.email,
            'created_at': self.created_at.isoformat()
        }

# 成绩模型
class Score(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)
    level_type = db.Column(db.String(50), nullable=False)  # 'standard', 'custom', 'challenge'
    level_number = db.Column(db.Integer, nullable=True)  # 关卡编号（标准模式）
    completion_time = db.Column(db.Float, nullable=False)  # 完成时间（秒）
    score = db.Column(db.Integer, nullable=False)  # 分数
    difficulty = db.Column(db.String(20), nullable=True)  # 难度等级
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    
    def to_dict(self):
        return {
            'id': self.id,
            'user_id': self.user_id,
            'username': self.user.username,
            'level_type': self.level_type,
            'level_number': self.level_number,
            'completion_time': self.completion_time,
            'score': self.score,
            'difficulty': self.difficulty,
            'created_at': self.created_at.isoformat()
        }

# 用户注册
@app.route('/api/register', methods=['POST'])
def register():
    try:
        data = request.get_json()
        username = data.get('username')
        password = data.get('password')
        email = data.get('email')
        
        if not username or not password:
            return jsonify({'error': '用户名和密码不能为空'}), 400
        
        # 检查用户名是否已存在
        if User.query.filter_by(username=username).first():
            return jsonify({'error': '用户名已存在'}), 400
        
        # 检查邮箱是否已存在
        if email and User.query.filter_by(email=email).first():
            return jsonify({'error': '邮箱已被注册'}), 400
        
        # 创建新用户
        user = User(username=username, email=email)
        user.set_password(password)
        
        db.session.add(user)
        db.session.commit()
        
        return jsonify({
            'message': '注册成功',
            'user': user.to_dict()
        }), 201
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500

# 用户登录
@app.route('/api/login', methods=['POST'])
def login():
    try:
        data = request.get_json()
        username = data.get('username')
        password = data.get('password')
        
        if not username or not password:
            return jsonify({'error': '用户名和密码不能为空'}), 400
        
        user = User.query.filter_by(username=username).first()
        
        if user and user.check_password(password):
            return jsonify({
                'message': '登录成功',
                'user': user.to_dict()
            }), 200
        else:
            return jsonify({'error': '用户名或密码错误'}), 401
            
    except Exception as e:
        return jsonify({'error': str(e)}), 500

# 上传成绩
@app.route('/api/scores', methods=['POST'])
def upload_score():
    try:
        data = request.get_json()
        user_id = data.get('user_id')
        level_type = data.get('level_type')
        completion_time = data.get('completion_time')
        score = data.get('score')
        level_number = data.get('level_number')
        difficulty = data.get('difficulty')
        
        if not all([user_id, level_type, completion_time is not None, score is not None]):
            return jsonify({'error': '缺少必要参数'}), 400
        
        # 验证用户是否存在
        user = User.query.get(user_id)
        if not user:
            return jsonify({'error': '用户不存在'}), 404
        
        # 创建成绩记录
        new_score = Score(
            user_id=user_id,
            level_type=level_type,
            level_number=level_number,
            completion_time=completion_time,
            score=score,
            difficulty=difficulty
        )
        
        db.session.add(new_score)
        db.session.commit()
        
        return jsonify({
            'message': '成绩上传成功',
            'score': new_score.to_dict()
        }), 201
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500

# 获取排行榜
@app.route('/api/leaderboard', methods=['GET'])
def get_leaderboard():
    try:
        level_type = request.args.get('level_type', 'all')
        level_number = request.args.get('level_number')
        difficulty = request.args.get('difficulty')
        limit = int(request.args.get('limit', 50))
        
        query = Score.query
        
        # 根据参数过滤
        if level_type != 'all':
            query = query.filter_by(level_type=level_type)
        
        if level_number:
            query = query.filter_by(level_number=int(level_number))
        
        if difficulty:
            query = query.filter_by(difficulty=difficulty)
        
        # 按分数降序排列，然后按完成时间升序排列
        scores = query.order_by(Score.score.desc(), Score.completion_time.asc()).limit(limit).all()
        
        leaderboard = []
        for i, score in enumerate(scores, 1):
            score_dict = score.to_dict()
            score_dict['rank'] = i
            leaderboard.append(score_dict)
        
        return jsonify({
            'leaderboard': leaderboard,
            'total_count': len(leaderboard)
        }), 200
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500

# 获取用户个人成绩
@app.route('/api/user/<int:user_id>/scores', methods=['GET'])
def get_user_scores(user_id):
    try:
        user = User.query.get(user_id)
        if not user:
            return jsonify({'error': '用户不存在'}), 404
        
        level_type = request.args.get('level_type')
        limit = int(request.args.get('limit', 20))
        
        query = Score.query.filter_by(user_id=user_id)
        
        if level_type:
            query = query.filter_by(level_type=level_type)
        
        scores = query.order_by(Score.created_at.desc()).limit(limit).all()
        
        return jsonify({
            'user': user.to_dict(),
            'scores': [score.to_dict() for score in scores]
        }), 200
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500

# 获取统计信息
@app.route('/api/stats', methods=['GET'])
def get_stats():
    try:
        total_users = User.query.count()
        total_scores = Score.query.count()
        
        # 各模式的成绩统计
        standard_scores = Score.query.filter_by(level_type='standard').count()
        custom_scores = Score.query.filter_by(level_type='custom').count()
        challenge_scores = Score.query.filter_by(level_type='challenge').count()
        
        # 最高分
        highest_score = Score.query.order_by(Score.score.desc()).first()
        
        return jsonify({
            'total_users': total_users,
            'total_scores': total_scores,
            'mode_stats': {
                'standard': standard_scores,
                'custom': custom_scores,
                'challenge': challenge_scores
            },
            'highest_score': highest_score.to_dict() if highest_score else None
        }), 200
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500

# 健康检查
@app.route('/api/health', methods=['GET'])
def health_check():
    return jsonify({'status': 'healthy', 'timestamp': datetime.utcnow().isoformat()}), 200

# 初始化数据库
@app.before_first_request
def create_tables():
    db.create_all()

if __name__ == '__main__':
    print("\n" + "="*60)
    print("重力球游戏 Flask 后端服务")
    print("="*60)
    print(f"服务地址: http://localhost:5001")
    print(f"数据库: {app.config['SQLALCHEMY_DATABASE_URI']}")
    print("="*60 + "\n")
    
    try:
        with app.app_context():
            db.create_all()
        app.run(host='0.0.0.0', port=5001, debug=True)
    except KeyboardInterrupt:
        print("\n服务器已停止")
    except Exception as e:
        print(f"\n服务器启动失败: {e}")