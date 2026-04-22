#!/usr/bin/env python3
"""
职业推荐API调试版
添加详细的调试日志和超时控制
"""

import os
import sys
import json
import time
import logging
import signal
from functools import wraps

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[logging.StreamHandler()]
)

logger = logging.getLogger('debug_recommendation_api')

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from ai_integration.arag_integration import get_career_search, ARAGCareerSearch
from ai_integration.langgraph.tools import keyword_search_tool

def timeout_handler(signum, frame):
    raise TimeoutError("API call timed out")

def timeout(seconds=60):
    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            signal.signal(signal.SIGALRM, timeout_handler)
            signal.alarm(seconds)
            try:
                return func(*args, **kwargs)
            finally:
                signal.alarm(0)
        return wrapper
    return decorator

@csrf_exempt
def debug_career_recommendation(request):
    """职业推荐API调试版"""
    request_id = os.urandom(8).hex()
    start_time = time.time()
    
    print(f"\n{'='*80}")
    print(f"🚀 请求开始 [{request_id}]")
    print(f"{'='*80}")
    
    if request.method != 'POST':
        return JsonResponse({'error': 'Method not allowed'}, status=405)
    
    try:
        # Step 1: 解析请求
        parse_start = time.time()
        try:
            data = json.loads(request.body)
        except json.JSONDecodeError as e:
            print(f"❌ 解析请求失败: {str(e)}")
            return JsonResponse({'error': 'Invalid JSON'}, status=400)
        
        self_introduction = data.get('self_introduction', '')
        parse_time = time.time() - parse_start
        
        print(f"📝 Step 1/5: 解析请求 - 耗时: {parse_time:.2f}s")
        print(f"   自我介绍长度: {len(self_introduction)}")
        print(f"   自我介绍预览: {self_introduction[:50]}...")
        
        if not self_introduction:
            print("❌ 自我介绍为空")
            return JsonResponse({'error': 'self_introduction is required'}, status=400)
        
        # Step 2: 关键词搜索
        search_start = time.time()
        print(f"\n🔍 Step 2/5: 执行关键词搜索")
        
        try:
            # 提取关键词
            keywords = extract_keywords(self_introduction)
            print(f"   提取关键词: {keywords}")
            
            # 调用工具
            tool_result = keyword_search_tool.invoke({
                'query': keywords,
                'threshold': 0.001,
                'top_k': 5
            })
            
            search_success = tool_result.get('success', False)
            search_count = tool_result.get('count', 0)
            
            if search_success:
                print(f"✅ 搜索成功")
                print(f"   找到 {search_count} 条匹配记录")
                
                if search_count > 0:
                    for i, result in enumerate(tool_result['results'][:3]):
                        score = result.get('match_score', 0)
                        print(f"   结果 {i+1}: 匹配度 {score:.4f}")
            else:
                print(f"❌ 搜索失败")
                
        except Exception as e:
            print(f"❌ 搜索异常: {str(e)}")
            tool_result = {'success': False, 'results': []}
            search_count = 0
        
        search_time = time.time() - search_start
        print(f"   搜索耗时: {search_time:.2f}s")
        
        # Step 3: 构建推荐结果
        build_start = time.time()
        print(f"\n🏗️ Step 3/5: 构建推荐结果")
        
        recommendations = []
        
        if search_success and tool_result.get('results'):
            # 从搜索结果提取推荐
            for result in tool_result['results'][:3]:
                if 'recommendations' in result:
                    recommendations.extend(result['recommendations'][:2])
            
            # 去重
            seen = set()
            unique_recs = []
            for rec in recommendations:
                career = rec.get('career', '').strip()
                if career and career not in seen:
                    seen.add(career)
                    unique_recs.append(rec)
            
            recommendations = unique_recs
            print(f"✅ 从搜索结果构建了 {len(recommendations)} 个推荐")
        else:
            # 使用默认推荐
            recommendations = get_default_recommendations()
            print(f"⚠️ 使用默认推荐")
        
        build_time = time.time() - build_start
        print(f"   构建耗时: {build_time:.2f}s")
        
        # Step 4: 保存记录
        save_start = time.time()
        print(f"\n💾 Step 4/5: 保存记录")
        
        try:
            session_id = save_recommendation_record(
                self_introduction,
                recommendations,
                request.user if request.user.is_authenticated else None
            )
            print(f"✅ 记录已保存, session_id: {session_id[:8]}...")
        except Exception as e:
            print(f"⚠️ 保存记录失败: {str(e)}")
            session_id = os.urandom(16).hex()
        
        save_time = time.time() - save_start
        print(f"   保存耗时: {save_time:.2f}s")
        
        # Step 5: 返回结果
        total_time = time.time() - start_time
        print(f"\n📤 Step 5/5: 返回结果")
        print(f"   总耗时: {total_time:.2f}s")
        
        print(f"\n{'='*80}")
        print(f"✅ 请求完成 [{request_id}] - 总耗时: {total_time:.2f}s")
        print(f"{'='*80}")
        
        return JsonResponse({
            "success": True,
            "recommendations": recommendations[:3],
            "raw_analysis": "",
            "session_id": session_id,
            "source": "keyword_search" if search_success else "default",
            "response_time": total_time,
            "timing": {
                "parse": parse_time,
                "search": search_time,
                "build": build_time,
                "save": save_time,
                "total": total_time
            }
        })
        
    except TimeoutError:
        total_time = time.time() - start_time
        print(f"\n⏰ 请求超时 [{request_id}] - 耗时: {total_time:.2f}s")
        return JsonResponse({
            "success": False,
            "error": "Request timed out",
            "response_time": total_time
        }, status=504)
        
    except Exception as e:
        total_time = time.time() - start_time
        print(f"\n❌ 请求失败 [{request_id}] - 耗时: {total_time:.2f}s")
        print(f"   错误: {str(e)}")
        import traceback
        traceback.print_exc()
        
        return JsonResponse({
            "success": False,
            "error": str(e),
            "response_time": total_time
        }, status=500)

def extract_keywords(text):
    """提取关键词"""
    keywords = []
    
    skill_list = [
        'Python', 'Java', 'JavaScript', 'Django', 'Flask', 'Spring',
        'MySQL', 'PostgreSQL', 'Redis', 'MongoDB', '算法', '机器学习',
        '深度学习', '人工智能', '前端', '后端', '全栈', '数据分析',
        'React', 'Vue', 'Node', 'Go', 'C++', 'Linux', 'Git', 'Docker'
    ]
    
    for skill in skill_list:
        if skill in text:
            keywords.append(skill)
    
    return ','.join(keywords) if keywords else text[:50]

def get_default_recommendations():
    """获取默认推荐"""
    return [
        {"career": "后端工程师", "matchScore": 90, "reason": "基于您的技能推荐"},
        {"career": "Python开发工程师", "matchScore": 85, "reason": "基于您的技能推荐"},
        {"career": "算法工程师", "matchScore": 80, "reason": "基于您的技能推荐"}
    ]

def save_recommendation_record(self_introduction, recommendations, user):
    """保存推荐记录"""
    from ai_integration.models import CareerRecommendationRecord
    
    session_id = os.urandom(16).hex()
    
    record, _ = CareerRecommendationRecord.objects.get_or_create(
        session_id=session_id,
        defaults={
            'user': user,
            'self_introduction': self_introduction,
            'recommendations': recommendations,
            'analysis_result': ''
        }
    )
    
    return session_id

if __name__ == "__main__":
    # 测试
    print("🎯 测试调试版职业推荐API")
    
    class MockRequest:
        method = 'POST'
        body = json.dumps({
            'self_introduction': '我精通Python编程，熟悉Django框架，有2年后端开发经验。'
        })
        
        class MockUser:
            is_authenticated = False
        
        user = MockUser()
    
    response = debug_career_recommendation(MockRequest())
    print(f"\n📊 响应: {response.content.decode()[:500]}...")