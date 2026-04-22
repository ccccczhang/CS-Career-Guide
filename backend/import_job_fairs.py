#!/usr/bin/env python3
"""
导入湖南高校双选会数据
"""

import os
import django

# 设置Django环境
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from company_reviews.models import JobFair

# 湖南高校双选会数据
job_fairs_data = [
    {
        'university': 'csu',
        'name': '2026届秋季大型综合双选会',
        'university_url': 'http://career.csu.edu.cn/'
    },
    {
        'university': 'hnu',
        'name': '2026届毕业生春季巡回双选会',
        'university_url': 'https://scc.hnu.edu.cn/'
    },
    {
        'university': 'hunnu_science',
        'name': '2026届师范类高校毕业生大型招聘会',
        'university_url': 'http://job.hunnu.edu.cn/'
    },
    {
        'university': 'csust',
        'name': '2026届毕业生春季综合双选会',
        'university_url': 'http://csust.bysjy.com.cn/'
    },
    {
        'university': 'hunnu',
        'name': '2026届毕业生"春风行动"大型综合类双选会',
        'university_url': 'http://jy.hunau.edu.cn/'
    },
    {
        'university': 'csuft',
        'name': '2026届毕业生春季大型供需见面会',
        'university_url': 'http://jy.csuft.edu.cn/'
    },
    {
        'university': 'hnu_medicine',
        'name': '2026届毕业生秋季双选会',
        'university_url': 'http://jiuye.hnucm.edu.cn/'
    },
    {
        'university': 'xtu',
        'name': '2026届毕业生线上双选会',
        'university_url': 'https://career.xtu.edu.cn/'
    },
    {
        'university': 'hnust',
        'name': '2026年春季毕业生供需见面会',
        'university_url': 'https://jy.hnust.edu.cn/'
    },
    {
        'university': 'usc',
        'name': '2026年全国城市联合招聘高校毕业生春季专场活动',
        'university_url': 'https://jiuye.usc.edu.cn/'
    },
    {
        'university': 'hutb',
        'name': '2026届毕业生"春风送岗"系列双选会',
        'university_url': 'http://job.hutb.edu.cn/'
    },
    {
        'university': 'hnctu',
        'name': '2026届毕业生春季双选会',
        'university_url': 'http://job.hut.edu.cn/'
    },
    {
        'university': 'jsu',
        'name': '2026届毕业生春季校园招聘会',
        'university_url': 'https://jy.jsu.edu.cn/'
    },
    {
        'university': 'hhtc',
        'name': '湖南省怀化片区2026届高校毕业生综合类供需见面会',
        'university_url': 'http://jy.hhtc.edu.cn/'
    },
    {
        'university': 'hnfnu',
        'name': '2026届毕业生春季大型双选会',
        'university_url': 'https://hnfnu.bibibi.net/'
    }
]

def import_job_fairs():
    """导入双选会数据"""
    print("开始导入湖南高校双选会数据...")
    
    for data in job_fairs_data:
        # 检查是否已存在
        existing = JobFair.objects.filter(
            university=data['university'],
            name=data['name']
        ).first()
        
        if existing:
            print(f"双选会 '{data['name']}' 已存在，跳过...")
            continue
        
        # 创建新记录
        job_fair = JobFair(
            university=data['university'],
            name=data['name'],
            university_url=data['university_url']
        )
        job_fair.save()
        print(f"成功导入双选会: {data['name']}")
    
    print("双选会数据导入完成！")

if __name__ == '__main__':
    import_job_fairs()
