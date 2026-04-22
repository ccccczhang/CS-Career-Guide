import os
import django

# 设置Django环境
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from django.db import connection

# 检查所有模型的数据库表结构
tables = [
    'ai_integration_aitag',
    'ai_integration_aiconversation',
    'ai_integration_aichatpair',
    'ai_integration_careerevaluationrecord',
    'ai_integration_careerplanreport',
    'ai_integration_careerrecommendationrecord',
    'ai_integration_aiusagerecord',
    'ai_integration_aiprompt',
    'ai_integration_longtermmemory'
]

for table in tables:
    print(f"\n检查表: {table}")
    with connection.cursor() as cursor:
        cursor.execute(f"DESCRIBE {table}")
        columns = cursor.fetchall()
        for column in columns:
            field_name = column[0]
            field_type = column[1]
            null = column[2]
            key = column[3]
            default = column[4]
            extra = column[5]
            print(f"  {field_name}: {field_type}, NULL: {null}, KEY: {key}, DEFAULT: {default}, EXTRA: {extra}")
