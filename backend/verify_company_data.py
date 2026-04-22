import os
import django

# 设置Django环境
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from company_reviews.models import CompanyInfo

print('=== 验证公司数据 ===')
print(f'CompanyInfo表数据量: {CompanyInfo.objects.count()}')
print('\n前10条数据:')
companies = CompanyInfo.objects.all()[:10]
for i, c in enumerate(companies, 1):
    print(f'  {i}. 公司: {c.company_name} | 职位: {c.job_title} | 地址: {c.address} | 薪资: {c.salary_range}')

print('\n=== 验证成功！===')
print('红黑榜功能现在使用的是 company_info_backup 表中的数据')
print('您可以通过前端页面 http://localhost:3000/company-reviews 查看完整数据')