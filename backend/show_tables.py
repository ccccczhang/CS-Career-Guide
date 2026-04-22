import os
import django

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from django.db import connection

with connection.cursor() as cursor:
    cursor.execute('SHOW TABLES')
    tables = cursor.fetchall()
    print('数据库中的所有表:')
    for i, table in enumerate(tables, 1):
        print(f'  {i}. {table[0]}')
    print(f'\n总计: {len(tables)} 张表')
