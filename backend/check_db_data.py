#!/usr/bin/env python3
"""检查数据库数据"""

import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from ai_integration.models import CareerRecommendationRecord

print("="*70)
print("检查数据库数据")
print("="*70)

records = CareerRecommendationRecord.objects.all()
print(f"总记录数: {records.count()}")

# 查看前5条记录
print("\n前5条记录:")
for i, r in enumerate(records[:5], 1):
    print(f"\n记录 {i}:")
    print(f"  ID: {r.id}")
    print(f"  self_introduction: {'有内容' if r.self_introduction else '空'}")
    if r.self_introduction:
        print(f"    长度: {len(r.self_introduction)}")
        print(f"    预览: {r.self_introduction[:100]}...")
    print(f"  introduction_preview: {'有内容' if r.introduction_preview else '空'}")
    print(f"  recommendations: {'有内容' if r.recommendations else '空'}")
    if r.recommendations:
        print(f"    数量: {len(r.recommendations)}")

print("\n" + "="*70)
print("完成!")
print("="*70)