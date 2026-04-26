from django.core.management.base import BaseCommand
from company_reviews.models import JobFair
from datetime import datetime

class Command(BaseCommand):
    help = '导入湖南高校双选会数据到 JobFair 模型'

    def handle(self, *args, **options):
        job_fairs = [
            {
                'university': 'hufe',
                'university_display': '湖南财政经济学院',
                'name': '"智汇潇湘 职通未来"2026届毕业生春季校园双选会',
                'location': '湖南财政经济学院',
                'description': '湖南财政经济学院2026届毕业生春季校园双选会，为毕业生提供优质就业机会。',
                'registration_link': 'https://jy.hufe.edu.cn/',
                'university_url': 'https://jy.hufe.edu.cn/'
            },
            {
                'university': 'hnxxy',
                'university_display': '湖南信息学院',
                'name': '2026届毕业生春季供需见面会',
                'location': '湖南信息学院',
                'description': '湖南信息学院2026届毕业生春季供需见面会，促进毕业生就业。',
                'registration_link': 'https://hnxxy.bysjy.com.cn/',
                'university_url': 'https://hnxxy.bysjy.com.cn/'
            },
            {
                'university': 'hnyxy',
                'university_display': '湖南应用技术学院',
                'name': '2026届毕业生春季供需见面会暨"校友回湘"专场招聘会',
                'location': '湖南应用技术学院',
                'description': '湖南应用技术学院2026届毕业生春季供需见面会，特设校友回湘专场。',
                'registration_link': 'http://hnyxy.bysjy.com.cn/',
                'university_url': 'http://hnyxy.bysjy.com.cn/'
            },
            {
                'university': 'hnist',
                'university_display': '湖南理工学院',
                'name': '湘北地区2026届高校毕业生秋季供需见面会',
                'location': '湖南理工学院',
                'description': '湖南理工学院举办的湘北地区2026届高校毕业生秋季供需见面会。',
                'registration_link': 'https://zjc.hnist.cn/',
                'university_url': 'https://zjc.hnist.cn/'
            },
            {
                'university': 'hynu',
                'university_display': '衡阳师范学院',
                'name': '2026届毕业生春季大型供需见面会',
                'location': '衡阳师范学院',
                'description': '衡阳师范学院2026届毕业生春季大型供需见面会。',
                'registration_link': 'http://jy.hynu.cn/',
                'university_url': 'http://jy.hynu.cn/'
            },
            {
                'university': 'huas',
                'university_display': '湖南文理学院',
                'name': '2026届毕业生春季供需见面会',
                'location': '湖南文理学院',
                'description': '湖南文理学院2026届毕业生春季供需见面会。',
                'registration_link': 'http://jy.huas.edu.cn/',
                'university_url': 'http://jy.huas.edu.cn/'
            },
            {
                'university': 'hnjcxy',
                'university_display': '湖南警察学院',
                'name': '2026届毕业生供需见面会（政法类）',
                'location': '湖南警察学院',
                'description': '湖南警察学院2026届毕业生供需见面会，侧重政法类岗位。',
                'registration_link': 'https://hnjcxy.bysjy.com.cn/',
                'university_url': 'https://hnjcxy.bysjy.com.cn/'
            },
            {
                'university': 'hngxy',
                'university_display': '湖南工学院',
                'name': '2026届毕业生春季校园招聘会（第三场）',
                'location': '湖南工学院',
                'description': '湖南工学院2026届毕业生春季校园招聘会第三场。',
                'registration_link': 'http://hngxycdn.bysjy.com.cn/',
                'university_url': 'http://hngxycdn.bysjy.com.cn/'
            },
            {
                'university': 'syxy',
                'university_display': '邵阳学院',
                'name': '2026届毕业生春季校园供需见面会（第二场）',
                'location': '邵阳学院',
                'description': '邵阳学院2026届毕业生春季校园供需见面会第二场。',
                'registration_link': 'http://syxy.bibibi.net/',
                'university_url': 'http://syxy.bibibi.net/'
            },
            {
                'university': 'xnu',
                'university_display': '湘南学院',
                'name': '2026届毕业生春季综合双选会',
                'location': '湘南学院',
                'description': '湘南学院2026届毕业生春季综合双选会。',
                'registration_link': 'http://www.xnu.edu.cn/jyw/',
                'university_url': 'http://www.xnu.edu.cn/jyw/'
            },
            {
                'university': 'hnrwkjxy',
                'university_display': '湖南人文科技学院',
                'name': '2026届毕业生第二场校园招聘会',
                'location': '湖南人文科技学院',
                'description': '湖南人文科技学院2026届毕业生第二场校园招聘会。',
                'registration_link': 'https://hnrwkjxy.bysjy.com.cn/',
                'university_url': 'https://hnrwkjxy.bysjy.com.cn/'
            },
            {
                'university': 'hnu_medicine',
                'university_display': '湖南医药学院',
                'name': '2026届毕业生供需见面会',
                'location': '湖南医药学院',
                'description': '湖南医药学院2026届毕业生供需见面会。',
                'registration_link': 'http://hnyxy.bysjy.com.cn/',
                'university_url': 'http://hnyxy.bysjy.com.cn/'
            },
            {
                'university': 'hnjtgcxy',
                'university_display': '湖南交通工程学院',
                'name': '2026届毕业生春季大型双选会',
                'location': '湖南交通工程学院',
                'description': '湖南交通工程学院2026届毕业生春季大型双选会。',
                'registration_link': 'http://hnjtgcxy.bysjy.com.cn/',
                'university_url': 'http://hnjtgcxy.bysjy.com.cn/'
            },
            {
                'university': 'hnrjzy',
                'university_display': '湖南软件职业技术大学',
                'name': '2026届毕业生春季校园双选会',
                'location': '湖南软件职业技术大学',
                'description': '湖南软件职业技术大学2026届毕业生春季校园双选会。',
                'registration_link': 'http://hnrjzy.bibibi.net/',
                'university_url': 'http://hnrjzy.bibibi.net/'
            }
        ]

        # 更新 UNIVERSITY_CHOICES 添加缺失的高校
        from company_reviews.models import JobFair
        import inspect
        source = inspect.getsource(JobFair)
        current_choices = []
        for line in source.split('\n'):
            if '(' in line and ',' in line and ')' in line and '=' not in line:
                parts = line.strip().strip(',').strip('(').strip(')').split(',')
                if len(parts) >= 2:
                    key = parts[0].strip().strip("'")
                    value = ','.join(parts[1:]).strip().strip("'")
                    current_choices.append((key, value))

        # 添加缺失的高校
        new_choices = [
            ('hufe', '湖南财政经济学院'),
            ('hnxxy', '湖南信息学院'),
            ('hnyxy', '湖南应用技术学院'),
            ('hnist', '湖南理工学院'),
            ('hynu', '衡阳师范学院'),
            ('huas', '湖南文理学院'),
            ('hnjcxy', '湖南警察学院'),
            ('hngxy', '湖南工学院'),
            ('syxy', '邵阳学院'),
            ('xnu', '湘南学院'),
            ('hnrwkjxy', '湖南人文科技学院'),
            ('hnjtgcxy', '湖南交通工程学院'),
            ('hnrjzy', '湖南软件职业技术大学'),
        ]

        created_count = 0
        updated_count = 0

        for fair in job_fairs:
            existing = JobFair.objects.filter(
                name=fair['name']
            ).first()

            if existing:
                for key, value in fair.items():
                    if key != 'university_display':
                        setattr(existing, key, value)
                existing.save()
                updated_count += 1
                self.stdout.write(f"更新: {fair['university_display']} - {fair['name']}")
            else:
                fair_copy = {k: v for k, v in fair.items() if k != 'university_display'}
                JobFair.objects.create(**fair_copy)
                created_count += 1
                self.stdout.write(f"创建: {fair['university_display']} - {fair['name']}")

        self.stdout.write(self.style.SUCCESS(f"导入完成！创建: {created_count} 条，更新: {updated_count} 条"))