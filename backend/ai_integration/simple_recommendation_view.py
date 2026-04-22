#!/usr/bin/env python3
"""
简化版职业推荐视图
添加详细的调试日志，帮助排查超时问题
"""

import os
import sys
import json
import time
import logging

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[logging.StreamHandler()]
)

logger = logging.getLogger('simple_recommendation')

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from langchain_core.messages import HumanMessage
from ai_integration.langgraph.agents.recommendation_agent import create_recommendation_agent
from ai_integration.langgraph.tools import keyword_search_tool, semantic_search_tool

@csrf_exempt
def simple_career_recommendation(request):
    """简化版职业推荐API - 添加详细调试日志"""
    start_time = time.time()
    
    if request.method != 'POST':
        return JsonResponse({'error': 'Method not allowed'}, status=405)
    
    try:
        # 解析请求
        data = json.loads(request.body)
        self_introduction = data.get('self_introduction', '')
        
        if not self_introduction:
            return JsonResponse({'error': 'self_introduction is required'}, status=400)
        
        logger.info(f"====== 开始职业推荐 ======")
        logger.info(f"用户自我介绍长度: {len(self_introduction)}")
        logger.info(f"自我介绍预览: {self_introduction[:50]}...")
        
        # Step 1: 直接调用工具搜索
        search_start = time.time()
        logger.info(f"Step 1: 执行关键词搜索")
        
        # 提取关键词
        keywords = extract_keywords(self_introduction)
        logger.info(f"提取的关键词: {keywords}")
        
        # 调用关键词搜索工具
        tool_result = keyword_search_tool.invoke({
            'query': keywords,
            'threshold': 0.001,
            'top_k': 5
        })
        
        search_time = time.time() - search_start
        logger.info(f"关键词搜索完成，耗时: {search_time:.2f}秒")
        logger.info(f"搜索结果数量: {tool_result.get('count', 0)}")
        
        if tool_result.get('success') and tool_result.get('results'):
            # Step 2: 使用搜索结果生成推荐
            logger.info(f"Step 2: 使用搜索结果生成推荐")
            recommendations = []
            
            # 从工具结果中提取推荐
            for result in tool_result['results'][:3]:
                if 'recommendations' in result:
                    recommendations.extend(result['recommendations'][:2])
            
            # 去重
            seen = set()
            unique_recs = []
            for rec in recommendations:
                career = rec.get('career', '')
                if career and career not in seen:
                    seen.add(career)
                    unique_recs.append(rec)
            
            # 确保至少有3个推荐
            if len(unique_recs) < 3:
                unique_recs.extend(get_fallback_recommendations()[:3 - len(unique_recs)])
            
            logger.info(f"生成推荐数量: {len(unique_recs)}")
            
            total_time = time.time() - start_time
            logger.info(f"====== 职业推荐完成，总耗时: {total_time:.2f}秒 ======")
            
            return JsonResponse({
                "success": True,
                "recommendations": unique_recs[:3],
                "raw_analysis": "",
                "session_id": os.urandom(16).hex(),
                "source": "tool_direct",
                "response_time": total_time
            })
        else:
            # Step 3: 搜索结果为空，使用代理生成推荐
            logger.info(f"Step 3: 搜索结果为空，使用代理生成推荐")
            
            agent_start = time.time()
            agent = create_recommendation_agent()
            
            # 调用代理
            result = agent.invoke({
                "messages": [HumanMessage(content=self_introduction)]
            })
            
            agent_time = time.time() - agent_start
            logger.info(f"代理调用完成，耗时: {agent_time:.2f}秒")
            
            # 解析结果
            recommendations = parse_recommendations(result)
            
            total_time = time.time() - start_time
            logger.info(f"====== 职业推荐完成，总耗时: {total_time:.2f}秒 ======")
            
            return JsonResponse({
                "success": True,
                "recommendations": recommendations[:3],
                "raw_analysis": "",
                "session_id": os.urandom(16).hex(),
                "source": "agent",
                "response_time": total_time
            })
        
    except Exception as e:
        total_time = time.time() - start_time
        logger.error(f"职业推荐失败，耗时: {total_time:.2f}秒，错误: {str(e)}")
        import traceback
        traceback.print_exc()
        return JsonResponse({
            "success": False,
            "error": str(e),
            "response_time": total_time
        }, status=500)

def extract_keywords(text):
    """从文本中提取关键词"""
    keywords = []
    
    # 技能关键词
    skills = ['Python', 'Java', 'JavaScript', 'Django', 'Flask', 'Spring',
              'MySQL', 'PostgreSQL', 'Redis', 'MongoDB', '算法', '机器学习',
              '深度学习', '人工智能', '前端', '后端', '全栈', '数据分析']
    
    for skill in skills:
        if skill in text:
            keywords.append(skill)
    
    return ','.join(keywords) if keywords else text[:50]

def parse_recommendations(result):
    """解析代理返回的推荐结果"""
    if hasattr(result, 'content') and result.content:
        content = result.content
        
        # 尝试提取JSON
        import re
        json_match = re.search(r'```json\s*([\s\S]*?)\s*```', content)
        if json_match:
            try:
                parsed = json.loads(json_match.group(1), strict=False)
                if isinstance(parsed, dict) and 'recommendations' in parsed:
                    return parsed['recommendations']
                elif isinstance(parsed, list):
                    return parsed
            except:
                pass
        
        # 尝试从文本提取
        career_matches = re.findall(r'###\s*(.*?)\s*', content)
        return [{"career": c.strip(), "matchScore": 90 - i*10} 
                for i, c in enumerate(career_matches[:3]) if c.strip()]
    
    return get_fallback_recommendations()

def get_fallback_recommendations():
    """获取默认推荐"""
    return [
        {"career": "后端工程师", "matchScore": 90, "reason": "基于您的技能推荐"},
        {"career": "Python开发工程师", "matchScore": 85, "reason": "基于您的技能推荐"},
        {"career": "算法工程师", "matchScore": 80, "reason": "基于您的技能推荐"}
    ]

if __name__ == "__main__":
    # 测试
    test_intro = "我精通Python编程，熟悉Django框架，有2年后端开发经验。"
    print("测试简化版职业推荐...")
    
    # 创建模拟请求
    class MockRequest:
        method = 'POST'
        body = json.dumps({'self_introduction': test_intro})
    
    response = simple_career_recommendation(MockRequest())
    print(f"响应: {response.content.decode()}")