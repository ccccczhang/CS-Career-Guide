#!/usr/bin/env python3
"""
检查数据库中的职业推荐记录数量
"""

import os
import sys

# 设置Django环境
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import django
django.setup()

from ai_integration.models import CareerRecommendationRecord

print("=" * 60)
print("数据库职业推荐记录检查")
print("=" * 60)

# 获取记录总数
total_count = CareerRecommendationRecord.objects.count()
print(f"\n职业推荐记录总数: {total_count}")

# 获取前5条记录
records = CareerRecommendationRecord.objects.all()[:5]

if records:
    print(f"\n前5条记录:")
    for i, record in enumerate(records):
        print(f"\n记录 {i+1}:")
        print(f"  ID: {record.id}")
        print(f"  User: {record.user}")
        print(f"  Session ID: {record.session_id}")
        print(f"  Created At: {record.created_at}")
        print(f"  Self Introduction: {record.self_introduction[:50]}..." if record.self_introduction else "  Self Introduction: None")
else:
    print("\n数据库中没有职业推荐记录")

print("\n" + "=" * 60)
