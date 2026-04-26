from django.core.management.base import BaseCommand
from company_reviews.models import CompanyInfo
from datetime import datetime

class Command(BaseCommand):
    help = '导入湖南高校双选会数据'

    def handle(self, *args, **options):
        career_fairs = [
            {
                'company_name': '湖南财政经济学院',
                'job_title': '"智汇潇湘 职通未来"2026届毕业生春季校园双选会',
                'address': '湖南长沙',
                'industry': '教育',
                'company_type': '高校',
                'company_scale': '高校',
                'company_details': '湖南财政经济学院2026届毕业生春季校园双选会，为毕业生提供优质就业机会。',
                'job_details': '大型校园招聘会，众多企业参与，涵盖多个行业和岗位。',
                'job_source_url': 'https://jy.hufe.edu.cn/'
            },
            {
                'company_name': '湖南信息学院',
                'job_title': '2026届毕业生春季供需见面会',
                'address': '湖南长沙',
                'industry': '教育',
                'company_type': '高校',
                'company_scale': '高校',
                'company_details': '湖南信息学院2026届毕业生春季供需见面会，促进毕业生就业。',
                'job_details': '春季大型供需见面会，为毕业生与企业搭建对接平台。',
                'job_source_url': 'https://hnxxy.bysjy.com.cn/'
            },
            {
                'company_name': '湖南应用技术学院',
                'job_title': '2026届毕业生春季供需见面会暨"校友回湘"专场招聘会',
                'address': '湖南常德',
                'industry': '教育',
                'company_type': '高校',
                'company_scale': '高校',
                'company_details': '湖南应用技术学院2026届毕业生春季供需见面会，特设校友回湘专场。',
                'job_details': '春季供需见面会，包含校友回湘专场招聘会。',
                'job_source_url': 'http://hnyxy.bysjy.com.cn/'
            },
            {
                'company_name': '湖南理工学院',
                'job_title': '湘北地区2026届高校毕业生秋季供需见面会',
                'address': '湖南岳阳',
                'industry': '教育',
                'company_type': '高校',
                'company_scale': '高校',
                'company_details': '湖南理工学院举办的湘北地区2026届高校毕业生秋季供需见面会。',
                'job_details': '湘北地区高校毕业生秋季供需见面会，覆盖多个高校毕业生。',
                'job_source_url': 'https://zjc.hnist.cn/'
            },
            {
                'company_name': '衡阳师范学院',
                'job_title': '2026届毕业生春季大型供需见面会',
                'address': '湖南衡阳',
                'industry': '教育',
                'company_type': '高校',
                'company_scale': '高校',
                'company_details': '衡阳师范学院2026届毕业生春季大型供需见面会。',
                'job_details': '春季大型供需见面会，提供大量就业岗位。',
                'job_source_url': 'http://jy.hynu.cn/'
            },
            {
                'company_name': '湖南文理学院',
                'job_title': '2026届毕业生春季供需见面会',
                'address': '湖南常德',
                'industry': '教育',
                'company_type': '高校',
                'company_scale': '高校',
                'company_details': '湖南文理学院2026届毕业生春季供需见面会。',
                'job_details': '春季供需见面会，促进毕业生就业。',
                'job_source_url': 'http://jy.huas.edu.cn/'
            },
            {
                'company_name': '湖南警察学院',
                'job_title': '2026届毕业生供需见面会（政法类）',
                'address': '湖南长沙',
                'industry': '教育',
                'company_type': '高校',
                'company_scale': '高校',
                'company_details': '湖南警察学院2026届毕业生供需见面会，侧重政法类岗位。',
                'job_details': '政法类专场招聘会，为政法专业毕业生提供就业机会。',
                'job_source_url': 'https://hnjcxy.bysjy.com.cn/'
            },
            {
                'company_name': '湖南工学院',
                'job_title': '2026届毕业生春季校园招聘会（第三场）',
                'address': '湖南衡阳',
                'industry': '教育',
                'company_type': '高校',
                'company_scale': '高校',
                'company_details': '湖南工学院2026届毕业生春季校园招聘会第三场。',
                'job_details': '春季校园招聘会第三场，持续为毕业生提供就业服务。',
                'job_source_url': 'http://hngxycdn.bysjy.com.cn/'
            },
            {
                'company_name': '邵阳学院',
                'job_title': '2026届毕业生春季校园供需见面会（第二场）',
                'address': '湖南邵阳',
                'industry': '教育',
                'company_type': '高校',
                'company_scale': '高校',
                'company_details': '邵阳学院2026届毕业生春季校园供需见面会第二场。',
                'job_details': '春季校园供需见面会第二场，继续为毕业生提供就业机会。',
                'job_source_url': 'http://syxy.bibibi.net/'
            },
            {
                'company_name': '湘南学院',
                'job_title': '2026届毕业生春季综合双选会',
                'address': '湖南郴州',
                'industry': '教育',
                'company_type': '高校',
                'company_scale': '高校',
                'company_details': '湘南学院2026届毕业生春季综合双选会。',
                'job_details': '春季综合双选会，涵盖多个行业，提供丰富就业岗位。',
                'job_source_url': 'http://www.xnu.edu.cn/jyw/'
            },
            {
                'company_name': '湖南人文科技学院',
                'job_title': '2026届毕业生第二场校园招聘会',
                'address': '湖南娄底',
                'industry': '教育',
                'company_type': '高校',
                'company_scale': '高校',
                'company_details': '湖南人文科技学院2026届毕业生第二场校园招聘会。',
                'job_details': '第二场校园招聘会，继续促进毕业生就业。',
                'job_source_url': 'https://hnrwkjxy.bysjy.com.cn/'
            },
            {
                'company_name': '湖南医药学院',
                'job_title': '2026届毕业生供需见面会',
                'address': '湖南怀化',
                'industry': '教育',
                'company_type': '高校',
                'company_scale': '高校',
                'company_details': '湖南医药学院2026届毕业生供需见面会。',
                'job_details': '医药类专场供需见面会，为医药专业毕业生提供就业机会。',
                'job_source_url': 'http://hnyxy.bysjy.com.cn/'
            },
            {
                'company_name': '湖南交通工程学院',
                'job_title': '2026届毕业生春季大型双选会',
                'address': '湖南衡阳',
                'industry': '教育',
                'company_type': '高校',
                'company_scale': '高校',
                'company_details': '湖南交通工程学院2026届毕业生春季大型双选会。',
                'job_details': '春季大型双选会，为交通工程相关专业毕业生提供就业机会。',
                'job_source_url': 'http://hnjtgcxy.bysjy.com.cn/'
            },
            {
                'company_name': '湖南软件职业技术大学',
                'job_title': '2026届毕业生春季校园双选会',
                'address': '湖南湘潭',
                'industry': '教育',
                'company_type': '高校',
                'company_scale': '高校',
                'company_details': '湖南软件职业技术大学2026届毕业生春季校园双选会。',
                'job_details': '春季校园双选会，为软件相关专业毕业生提供就业机会。',
                'job_source_url': 'http://hnrjzy.bibibi.net/'
            }
        ]

        created_count = 0
        updated_count = 0

        for fair in career_fairs:
            # 检查是否已存在
            existing = CompanyInfo.objects.filter(
                company_name=fair['company_name'],
                job_title=fair['job_title']
            ).first()

            if existing:
                # 更新现有记录
                for key, value in fair.items():
                    setattr(existing, key, value)
                existing.update_date = datetime.now()
                existing.save()
                updated_count += 1
                self.stdout.write(f"更新: {fair['company_name']} - {fair['job_title']}")
            else:
                # 创建新记录
                fair['update_date'] = datetime.now()
                CompanyInfo.objects.create(**fair)
                created_count += 1
                self.stdout.write(f"创建: {fair['company_name']} - {fair['job_title']}")

        self.stdout.write(self.style.SUCCESS(f"导入完成！创建: {created_count} 条，更新: {updated_count} 条"))