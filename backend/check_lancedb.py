#!/usr/bin/env python3
"""
检查LanceDB中的数据条数
"""

import os
import sys

# 设置Django环境
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import django
django.setup()

import lancedb

# 连接LanceDB
db_path = os.path.join('ai_integration', 'lancedb')
db = lancedb.connect(db_path)

table_name = 'career_recommendations'

print("=" * 60)
print("LanceDB 数据检查")
print("=" * 60)

if table_name in db.table_names():
    table = db[table_name]
    
    # 获取所有记录
    results = table.to_pandas().to_dict('records')
    
    print(f"\n表名: {table_name}")
    print(f"记录总数: {len(results)}")
    
    if results:
        print(f"\n前5条记录:")
        for i, result in enumerate(results[:5]):
            print(f"\n记录 {i+1}:")
            print(f"  ID: {result.get('id')}")
            print(f"  User ID: {result.get('user_id')}")
            print(f"  Created At: {result.get('created_at')}")
            print(f"  Self Introduction: {result.get('self_introduction', '')[:50]}..." if result.get('self_introduction') else "  Self Introduction: None")
    else:
        print("\n表中没有数据")
else:
    print(f"\n表 {table_name} 不存在")

print("\n" + "=" * 60)
