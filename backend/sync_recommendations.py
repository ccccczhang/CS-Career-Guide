#!/usr/bin/env python3
"""
同步数据库中的职业推荐记录到LanceDB
"""

import os
import sys
import json
import shutil

# 设置Django环境
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import django
django.setup()

import lancedb
import pyarrow as pa
from ai_integration.models import CareerRecommendationRecord

print("=" * 60)
print("同步职业推荐记录到LanceDB")
print("=" * 60)

# 获取数据库中的记录
db_records = CareerRecommendationRecord.objects.all()
print(f"\n数据库记录总数: {db_records.count()}")

# 连接LanceDB
db_path = os.path.join('ai_integration', 'lancedb')

# 删除现有的LanceDB表目录（如果存在）
lancedb_dir = os.path.join(db_path, 'career_recommendations.lance')
if os.path.exists(lancedb_dir):
    print(f"\n删除现有的LanceDB表目录: {lancedb_dir}")
    shutil.rmtree(lancedb_dir)
    print("LanceDB表目录已删除")

db = lancedb.connect(db_path)

table_name = 'career_recommendations'

# 生成预览函数
def generate_preview(self_introduction, max_length=150):
    """生成自我介绍的预览（高亮部分）"""
    if not self_introduction:
        return ""
    
    lines = self_introduction.split('\n')
    preview_lines = []
    
    for line in lines:
        line = line.strip()
        if line:
            preview_lines.append(line)
            if len(' '.join(preview_lines)) > max_length:
                break
    
    preview = ' '.join(preview_lines)
    if len(preview) > max_length:
        preview = preview[:max_length] + '...'
    
    return preview

# 重新创建表
print("\n重新创建LanceDB表...")
schema = pa.schema([
    ("id", pa.string()),
    ("user_id", pa.string()),
    ("self_introduction", pa.string()),
    ("introduction_preview", pa.string()),
    ("recommendations", pa.string()),
    ("analysis_result", pa.string()),
    ("created_at", pa.string()),
    ("embedding", pa.list_(pa.float32(), 384))
])
table = db.create_table(table_name, schema=schema)
print("LanceDB表已创建")

# 将数据库记录添加到LanceDB
print(f"\n开始添加 {db_records.count()} 条记录到LanceDB...")
records_to_add = []

for i, record in enumerate(db_records):
    try:
        # 生成预览
        preview = generate_preview(record.self_introduction)
        
        # 更新数据库中的预览字段
        record.introduction_preview = preview
        record.save()
        
        record_data = {
            "id": str(record.id),
            "user_id": str(record.user.id) if record.user else "",
            "self_introduction": record.self_introduction or "",
            "introduction_preview": preview,
            "recommendations": json.dumps(record.recommendations, ensure_ascii=False) if record.recommendations else "[]",
            "analysis_result": record.analysis_result or "",
            "created_at": record.created_at.isoformat() if record.created_at else "",
            "embedding": [0.0] * 384  # 暂时不生成embedding
        }
        records_to_add.append(record_data)
        print(f"  准备添加记录 {i+1}/{db_records.count()}: ID={record.id}")
    except Exception as e:
        print(f"  准备记录 {i+1} 失败: {e}")

# 批量添加记录
if records_to_add:
    try:
        table.add(records_to_add)
        print(f"\n成功添加 {len(records_to_add)} 条记录到LanceDB")
    except Exception as e:
        print(f"\n添加记录失败: {e}")

print("\n" + "=" * 60)
print("同步完成")
print("=" * 60)

# 验证同步结果
table = db[table_name]
results = table.to_pandas().to_dict('records')
print(f"\nLanceDB记录总数: {len(results)}")

# 显示同步后的记录
print("\n同步后的LanceDB记录:")
for i, result in enumerate(results[:5]):
    print(f"记录 {i+1}:")
    print(f"  ID: {result.get('id')}")
    print(f"  User ID: {result.get('user_id')}")
    print(f"  Self Introduction: {result.get('self_introduction', '')[:50]}..." if result.get('self_introduction') else "  Self Introduction: None")
    print(f"  Created At: {result.get('created_at')}")
    print()
