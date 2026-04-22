#!/usr/bin/env python3
"""创建多字段全文索引"""

import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from django.conf import settings

def create_multi_field_index():
    """创建多字段全文索引"""
    print("="*70)
    print("创建多字段全文索引")
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
            # 先检查是否已存在旧索引
            print("🔍 检查现有索引:")
            sql = """SHOW INDEX FROM ai_integration_careerrecommendationrecord;"""
            result = conn.execute(text(sql))
            indexes = result.fetchall()
            
            index_names = [row[2] for row in indexes]
            print(f"   当前索引: {index_names}")
            
            # 删除旧的单字段索引（如果存在）
            if 'ft_self_intro' in index_names:
                print("\n⚠️ 删除旧的单字段索引...")
                sql = """ALTER TABLE ai_integration_careerrecommendationrecord DROP INDEX ft_self_intro;"""
                conn.execute(text(sql))
                conn.commit()
                print("✅ 旧索引删除成功")
            
            # 创建新的多字段索引
            print("\n🔧 创建多字段全文索引...")
            sql = """
                ALTER TABLE ai_integration_careerrecommendationrecord 
                ADD FULLTEXT INDEX ft_intro_both (self_introduction, introduction_preview);
            """
            conn.execute(text(sql))
            conn.commit()
            print("✅ 多字段索引创建成功!")
            
            # 验证新索引
            print("\n🔍 验证新索引:")
            sql = """SHOW INDEX FROM ai_integration_careerrecommendationrecord WHERE Key_name = 'ft_intro_both';"""
            result = conn.execute(text(sql))
            index_exists = len(result.fetchall()) > 0
            print(f"   {'✅' if index_exists else '❌'} 多字段索引 ft_intro_both {'存在' if index_exists else '不存在'}")
            
            # 测试新索引
            print("\n🔍 测试多字段搜索:")
            sql = """
                SELECT 
                    id,
                    self_introduction,
                    introduction_preview,
                    MATCH(self_introduction, introduction_preview) AGAINST('Python' IN NATURAL LANGUAGE MODE) AS match_score
                FROM ai_integration_careerrecommendationrecord
                WHERE MATCH(self_introduction, introduction_preview) AGAINST('Python' IN NATURAL LANGUAGE MODE)
                ORDER BY match_score DESC
                LIMIT 3;
            """
            result = conn.execute(text(sql))
            rows = result.fetchall()
            print(f"   搜索 'Python' 结果数: {len(rows)}")
            for row in rows:
                print(f"     - ID: {row[0]}, 匹配度: {row[3]:.4f}")
        
        print("\n" + "="*70)
        print("完成!")
        print("="*70)
        
    except Exception as e:
        print(f"❌ 创建索引失败: {str(e)}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    create_multi_field_index()