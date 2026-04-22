import requests
import json

print('测试简化版职业推荐API...')
try:
    response = requests.post(
        'http://localhost:8000/api/ai/llm/career/recommendation/',
        json={'self_introduction': '姓名: 张三\n学校: 长沙理工大学\n专业: 计算机科学\n年级: 大三\n性别: 男\n技能: Python开发、数据结构\n职业期望: 软件工程师'},
        timeout=120
    )
    print(f'状态码: {response.status_code}')
    response_data = response.json()
    print(f'成功: {response_data.get("success")}')
    if response_data.get("success"):
        print(f'推荐数量: {len(response_data.get("recommendations", []))}')
        for i, rec in enumerate(response_data.get("recommendations", []), 1):
            print(f'  {i}. {rec.get("career")} (匹配度: {rec.get("matchScore")})')
            print(f'     理由: {rec.get("reason")}')
        print(f'响应时间: {response_data.get("response_time", 0):.2f}秒')
        print(f'来源: {response_data.get("source")}')
    else:
        print(f'错误: {response_data.get("error")}')
except requests.exceptions.Timeout:
    print('请求超时')
except Exception as e:
    print(f'错误: {e}')