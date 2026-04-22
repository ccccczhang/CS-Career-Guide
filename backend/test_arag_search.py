import os
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
import django
django.setup()

from ai_integration.arag_integration import get_career_search

print('测试A-RAG搜索...')
try:
    career_search = get_career_search()
    print('成功获取career_search实例')
    
    import time
    start = time.time()
    result = career_search.search_with_cache('软件工程师', keyword_threshold=0.7, semantic_threshold=0.8, top_k=5)
    elapsed = time.time() - start
    
    print(f'搜索耗时: {elapsed:.2f}秒')
    print(f'搜索结果数量: {len(result)}')
    if result:
        for i, item in enumerate(result[:3], 1):
            print(f'  {i}. {item.get("career", "未知")}')
except Exception as e:
    print(f'A-RAG搜索失败: {e}')
    import traceback
    traceback.print_exc()