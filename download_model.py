#!/usr/bin/env python3
"""下载 Sentence-BERT 模型用于语义搜索"""

import os
import sys

def download_model():
    print("=" * 60)
    print("下载 Sentence-BERT 模型")
    print("=" * 60)
    
    try:
        print("\n1. 尝试导入 sentence_transformers...")
        from sentence_transformers import SentenceTransformer
        
        print("2. 下载并缓存模型...")
        model_name = 'all-MiniLM-L6-v2'
        print(f"   模型名称: {model_name}")
        
        # 这会自动下载并缓存模型
        model = SentenceTransformer(model_name)
        print("   ✅ 模型下载成功！")
        
        # 打印模型信息
        print(f"\n3. 模型信息:")
        print(f"   模型路径: {model._first_module().tokenizer.name_or_path}")
        
        # 测试编码
        print("\n4. 测试模型编码...")
        embeddings = model.encode(["测试语义搜索", "网络安全"])
        print(f"   ✅ 编码成功，向量维度: {len(embeddings[0])}")
        
        # 计算相似度
        from sklearn.metrics.pairwise import cosine_similarity
        similarity = cosine_similarity([embeddings[0]], [embeddings[1]])[0][0]
        print(f"   相似度: {similarity:.4f}")
        
        print("\n" + "=" * 60)
        print("模型下载完成！")
        print("=" * 60)
        
    except ImportError:
        print("❌ sentence_transformers 未安装")
        print("请运行: pip install sentence-transformers")
        sys.exit(1)
    except Exception as e:
        print(f"❌ 下载失败: {str(e)}")
        sys.exit(1)

if __name__ == '__main__':
    download_model()