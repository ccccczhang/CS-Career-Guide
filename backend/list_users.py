import pymysql

conn = pymysql.connect(
    host='localhost',
    port=3306,
    user='root',
    password='123456',
    database='cs_career_guide',
    charset='utf8mb4'
)

with conn.cursor() as cursor:
    cursor.execute('SELECT id, username, email, is_superuser FROM users')
    users = cursor.fetchall()
    print('数据库中的用户:')
    for user in users:
        is_super = '是' if user[3] else '否'
        print(f'  ID:{user[0]} - {user[1]} ({user[2]}) - 超级管理员:{is_super}')

conn.close()