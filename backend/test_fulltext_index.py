#!/usr/bin/env python3
"""测试MySQL全文索引"""

import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from django.conf import settings

def test_fulltext_index():
    """测试全文索引"""
    print("="*70)
    print("测试MySQL全文索引")
    print("="*70)
    
    try:
        from sqlalchemy import create_engine, text
        
        # 从Django配置获取数据库连接信息
        db_settings = settings.DATABASES['default']
        db_user = db_settings['USER']
        db_password = db_settings['PASSWORD']
        db_host = db_settings['HOST']
        db_port = db_settings.get('PORT', '3306')
        db_name = db_settings['NAME']
        db_charset = db_settings.get('OPTIONS', {}).get('charset', 'utf8mb4')
        
        # 创建连接
        db_url = f"mysql+pymysql://{db_user}:{db_password}@{db_host}:{db_port}/{db_name}?charset={db_charset}"
        engine = create_engine(db_url)
        
        with engine.connect() as conn:
            # 检查索引是否存在
            print("🔍 检查全文索引是否存在:")
            sql = """
                SHOW INDEX FROM ai_integration_careerrecommendationrecord 
                WHERE Key_name = 'ft_self_intro';
            """
            result = conn.execute(text(sql))
            index_exists = len(result.fetchall()) > 0
            print(f"   {'✅' if index_exists else '❌'} 全文索引 ft_self_intro {'存在' if index_exists else '不存在'}")
            
            if not index_exists:
                print("\n⚠️ 全文索引不存在，正在创建...")
                sql = """
                    ALTER TABLE ai_integration_careerrecommendationrecord 
                    ADD FULLTEXT INDEX ft_self_intro (self_introduction);
                """
                try:
                    conn.execute(text(sql))
                    conn.commit()
                    print("✅ 全文索引创建成功!")
                except Exception as e:
                    print(f"❌ 创建索引失败: {str(e)}")
                    return
            
            # 测试全文搜索
            print("\n🔍 测试全文搜索:")
            test_queries = ["Python", "数据分析", "后端开发", "Java"]
            
            for query in test_queries:
                sql = """
                    SELECT 
                        id, 
                        self_introduction,
                        MATCH(self_introduction) AGAINST(:query IN NATURAL LANGUAGE MODE) AS match_score
                    FROM ai_integration_careerrecommendationrecord
                    WHERE MATCH(self_introduction) AGAINST(:query IN NATURAL LANGUAGE MODE)
                    ORDER BY match_score DESC
                    LIMIT 3;
                """
                result = conn.execute(text(sql), {'query': query})
                rows = result.fetchall()
                
                print(f"\n   查询: '{query}'")
                print(f"   结果数: {len(rows)}")
                for row in rows:
                    print(f"     - ID: {row[0]}, 匹配度: {row[2]:.4f}, 内容: {row[1][:30]}...")
        
        print("\n" + "="*70)
        print("测试完成!")
        print("="*70)
        
    except Exception as e:
        print(f"❌ 测试失败: {str(e)}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    test_fulltext_index()