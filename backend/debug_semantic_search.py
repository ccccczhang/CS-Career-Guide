#!/usr/bin/env python3
"""
语义搜索调试脚本
"""

import os
import sys
import logging

logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

def test_model_loading():
    """测试模型加载"""
    print("\n" + "="*70)
    print("1. 测试 SentenceTransformer 模型加载")
    print("="*70)
    
    try:
        from sentence_transformers import SentenceTransformer
        
        # 检查本地缓存路径
        model_name = 'all-MiniLM-L6-v2'
        local_paths = [
            os.path.join(os.path.expanduser('~'), '.cache', 'huggingface', 'hub',
                        f'models--sentence-transformers--{model_name.replace("-", "--")}'),
            os.path.join(os.path.expanduser('~'), '.cache', 'torch', 'sentence_transformers',
                        f'sentence-transformers_{model_name}'),
        ]
        
        print("\n📁 检查本地模型缓存路径:")
        for path in local_paths:
            exists = os.path.exists(path)
            print(f"   {'✅' if exists else '❌'} {path}")
        
        # 尝试加载模型
        print("\n🔄 尝试加载模型...")
        model = SentenceTransformer(model_name)
        print(f"✅ 模型加载成功!")
        print(f"   模型名称: {model_name}")
        print(f"   维度: {model.get_sentence_embedding_dimension()}")
        
        return model
        
    except Exception as e:
        print(f"❌ 模型加载失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return None

def test_lancedb_connection():
    """测试 LanceDB 连接"""
    print("\n" + "="*70)
    print("2. 测试 LanceDB 连接")
    print("="*70)
    
    try:
        import lancedb
        
        db_path = os.path.join(os.path.dirname(__file__), "ai_integration", "lancedb")
        print(f"📁 数据库路径: {db_path}")
        print(f"   路径存在: {'✅' if os.path.exists(db_path) else '❌'}")
        
        db = lancedb.connect(db_path)
        print(f"✅ 连接成功!")
        
        tables = db.table_names()
        print(f"📊 表列表: {tables}")
        
        if "career_recommendations" in tables:
            table = db["career_recommendations"]
            count = table.count_rows()
            print(f"   career_recommendations 表记录数: {count}")
            
            # 查看表结构
            schema = table.schema
            print(f"   表结构:")
            for field in schema:
                print(f"     - {field.name}: {field.type}")
            
            # 查看部分数据
            if count > 0:
                sample = table.to_pandas().head(1).to_dict('records')
                print(f"   示例数据: {sample[0].keys() if sample else '无'}")
                
        return db
        
    except Exception as e:
        print(f"❌ LanceDB 连接失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return None

def test_semantic_search():
    """测试语义搜索"""
    print("\n" + "="*70)
    print("3. 测试语义搜索功能")
    print("="*70)
    
    try:
        from ai_integration.arag_integration import ARAGCareerSearch
        
        search = ARAGCareerSearch()
        print("✅ ARAGCareerSearch 初始化成功")
        
        # 测试语义搜索
        print("\n🔍 执行语义搜索...")
        results = search.semantic_search("Python 数据分析", threshold=0.5, top_k=3)
        
        print(f"📊 搜索结果: {len(results)} 条")
        for i, result in enumerate(results):
            print(f"\n   结果 {i+1}:")
            print(f"      ID: {result.get('id')}")
            print(f"      匹配度: {result.get('match_score', 0):.4f}")
            print(f"      类型: {result.get('match_type')}")
            print(f"      预览: {result.get('introduction_preview', result.get('self_introduction', ''))[:50]}...")
        
        return results
        
    except Exception as e:
        print(f"❌ 语义搜索失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return None

def test_embedding_generation():
    """测试嵌入向量生成"""
    print("\n" + "="*70)
    print("4. 测试嵌入向量生成")
    print("="*70)
    
    try:
        from sentence_transformers import SentenceTransformer
        
        model = SentenceTransformer('all-MiniLM-L6-v2')
        print("✅ 模型加载成功")
        
        # 测试编码
        texts = ["Python 数据分析", "网络安全", "前端开发"]
        embeddings = model.encode(texts, normalize_embeddings=True)
        
        print(f"\n📊 生成嵌入向量:")
        for i, (text, embedding) in enumerate(zip(texts, embeddings)):
            print(f"   '{text}' -> 维度: {len(embedding)}, 前3个值: {embedding[:3]}")
        
        # 计算相似度
        print("\n🔢 计算相似度:")
        for i, text1 in enumerate(texts):
            for j, text2 in enumerate(texts):
                if i < j:
                    similarity = embeddings[i].dot(embeddings[j])
                    print(f"   '{text1}' 与 '{text2}' 的相似度: {similarity:.4f}")
        
        return embeddings
        
    except Exception as e:
        print(f"❌ 嵌入向量生成失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return None

if __name__ == "__main__":
    print("="*70)
    print("语义搜索调试脚本")
    print("="*70)
    
    # 运行所有测试
    model = test_model_loading()
    db = test_lancedb_connection()
    
    if model and db:
        test_embedding_generation()
        test_semantic_search()
    
    print("\n" + "="*70)
    print("调试完成!")
    print("="*70)