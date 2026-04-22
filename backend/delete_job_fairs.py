#!/usr/bin/env python3
"""
删除所有湖南高校双选会数据
"""

import os
import django

# 设置Django环境
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from company_reviews.models import JobFair

def delete_all_job_fairs():
    """删除所有双选会数据"""
    print("开始删除所有湖南高校双选会数据...")
    
    # 获取所有双选会数据
    job_fairs = JobFair.objects.all()
    count = job_fairs.count()
    
    if count > 0:
        # 删除所有数据
        job_fairs.delete()
        print(f"成功删除 {count} 条双选会数据")
    else:
        print("没有双选会数据需要删除")
    
    print("删除完成！")

if __name__ == '__main__':
    delete_all_job_fairs()
