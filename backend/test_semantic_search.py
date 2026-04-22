import os
import time

# 设置Django环境
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
import django
django.setup()

from ai_integration.arag_integration import ARAGCareerSearch

print("="*60)
print("🔍 测试语义搜索功能")
print("="*60)

# 创建搜索实例
print("\n1. 创建ARAG搜索实例...")
start = time.time()
try:
    search = ARAGCareerSearch()
    elapsed = time.time() - start
    print(f"✅ 成功创建搜索实例，耗时: {elapsed:.2f}秒")
except Exception as e:
    print(f"❌ 创建搜索实例失败: {e}")
    import traceback
    traceback.print_exc()
    exit(1)

# 测试关键词搜索
print("\n2. 测试关键词搜索...")
start = time.time()
try:
    results = search.keyword_search("软件工程师", threshold=0.5, top_k=3)
    elapsed = time.time() - start
    print(f"✅ 关键词搜索完成，耗时: {elapsed:.2f}秒")
    print(f"   找到 {len(results)} 条结果")
    for i, r in enumerate(results, 1):
        print(f"   {i}. {r.get('introduction_preview', '')[:50]}... (匹配度: {r.get('match_score', 0):.2f})")
except Exception as e:
    print(f"❌ 关键词搜索失败: {e}")

# 测试语义搜索
print("\n3. 测试语义搜索...")
start = time.time()
try:
    results = search.semantic_search("软件工程师", threshold=0.5, top_k=3)
    elapsed = time.time() - start
    print(f"✅ 语义搜索完成，耗时: {elapsed:.2f}秒")
    print(f"   找到 {len(results)} 条结果")
    for i, r in enumerate(results, 1):
        print(f"   {i}. {r.get('introduction_preview', '')[:50]}... (相似度: {r.get('match_score', 0):.2f})")
except Exception as e:
    print(f"❌ 语义搜索失败: {e}")
    import traceback
    traceback.print_exc()

# 测试混合搜索
print("\n4. 测试混合搜索...")
start = time.time()
try:
    results = search.hybrid_search("软件工程师", keyword_threshold=0.5, semantic_threshold=0.6, top_k=3)
    elapsed = time.time() - start
    print(f"✅ 混合搜索完成，耗时: {elapsed:.2f}秒")
    print(f"   找到 {len(results)} 条结果")
    for i, r in enumerate(results, 1):
        match_type = r.get('match_type', 'unknown')
        print(f"   {i}. {r.get('introduction_preview', '')[:50]}... (匹配度: {r.get('match_score', 0):.2f}, 类型: {match_type})")
except Exception as e:
    print(f"❌ 混合搜索失败: {e}")

print("\n" + "="*60)
print("测试完成")
print("="*60)