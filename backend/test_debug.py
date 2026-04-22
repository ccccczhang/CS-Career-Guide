import os
from dotenv import load_dotenv
load_dotenv('.env')

print('测试简化版LLM推荐函数...')

# 直接测试LLM调用
from langchain_openai import ChatOpenAI
from langchain_core.prompts import ChatPromptTemplate

api_key = os.getenv('api_key')
base_url = os.getenv('base_url')

print(f'API Key: {api_key[:20]}...' if api_key else 'API Key not set')
print(f'Base URL: {base_url}')

try:
    llm = ChatOpenAI(
        model='qwen-turbo-2025-07-15',
        temperature=0.7,
        api_key=api_key,
        base_url=base_url,
        timeout=60
    )
    
    prompt = """你是一个专业的职业推荐顾问。请根据用户提供的自我介绍，分析其技能、背景和职业期望，推荐3个最适合的职业方向。

用户信息：
姓名: 张三
学校: 长沙理工大学
专业: 计算机科学
年级: 大三
技能: Python开发、数据结构
职业期望: 软件工程师

请推荐3个合适的职业，并以JSON格式输出：
[{"career": "职业名称", "matchScore": 匹配度, "reason": "推荐理由"}]
"""
    
    print('调用大模型...')
    result = llm.invoke(prompt)
    print(f'响应内容: {result.content}')
    
except Exception as e:
    print(f'错误: {e}')
    import traceback
    traceback.print_exc()