import os
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cs_career_guide.settings')

import django
django.setup()

from django.contrib.auth.hashers import make_password
from users.models import User

username = 'admin'
new_password = '123456'

try:
    user = User.objects.get(username=username)
    user.password = make_password(new_password)
    user.save()
    print(f'✅ {username} 用户密码已重置为: {new_password}')
except User.DoesNotExist:
    print(f'❌ 用户 {username} 不存在')