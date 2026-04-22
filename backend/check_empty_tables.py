import os
import django

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')
django.setup()

from django.db import connection

with connection.cursor() as cursor:
    cursor.execute('SHOW TABLES')
    tables = cursor.fetchall()
    
    empty_tables = []
    non_empty_tables = []
    
    for table in tables:
        table_name = table[0]
        cursor.execute(f'SELECT COUNT(*) FROM {table_name}')
        count = cursor.fetchone()[0]
        if count == 0:
            empty_tables.append(table_name)
        else:
            non_empty_tables.append((table_name, count))
    
    print('=' * 80)
    print('数据库表记录统计')
    print('=' * 80)
    
    print('\n📋 非空表（按记录数降序排列）:')
    print('-' * 60)
    for table_name, count in sorted(non_empty_tables, key=lambda x: x[1], reverse=True):
        print(f'  {table_name}: {count} 条记录')
    
    print(f'\n🗑️ 空表列表 ({len(empty_tables)} 张):')
    print('-' * 60)
    for i, table_name in enumerate(empty_tables, 1):
        print(f'  {i}. {table_name}')
    
    print(f'\n📊 总计: {len(tables)} 张表，{len(non_empty_tables)} 张有数据，{len(empty_tables)} 张为空')
