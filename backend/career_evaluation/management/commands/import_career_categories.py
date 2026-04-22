from django.core.management.base import BaseCommand
from career_evaluation.models import CareerCategory, CareerSubcategory

class Command(BaseCommand):
    help = 'Import predefined career categories and subcategories'

    def handle(self, *args, **options):
        try:
            # Define career categories and their subcategories
            career_data = {
                '后端开发': [
                    'Java', 'C++', 'PHP', 'Golang', '安全工程师', '游戏后端', '区块链', 
                    '信息技术岗', 'C工程师', 'C#工程师', '.NET', 'Python', 'Delphi', 'GIS工程师', 
                    'VB', 'Perl', 'Ruby', 'Node.js', 'Erlang', '后端工程师', '语音视频图形开发', '全栈开发'
                ],
                '前端开发': [
                    '前端工程师', 'Web前端', '前端开发其他', '游戏前端', 'HTML5'
                ],
                '客户端开发': [
                    '安卓', 'iOS开发', '客户端其他', 'Unity3d客户端', '客户端开发', 
                    'UE4', 'COCOS2DN', '引擎开发', 'UE5'
                ],
                '测试': [
                    '测试工程师', '测试开发', '测试其他', '游戏测试', '硬件测试', 
                    '软件测试', '自动化测试'
                ],
                '数据': [
                    '数据分析师', '数据库工程师', '大数据开发工程师', '数据其他', '数据架构师', 
                    'ETL工程师', '数据采集', '数据仓库', 'Hadoop', 'DBA'
                ],
                '运维': [
                    '运维工程师', '运维开发工程师', '网络工程师', '系统工程师', '运维其他', 
                    '网络安全', 'IT技术支持', '系统安全'
                ],
                '人工智能': [
                    '算法工程师', '深度学习', '自然语言处理', '机器学习', '搜索算法', 
                    '数据挖掘', '图像识别', '语音识别', '推荐算法', '人工智能', 
                    '智能驾驶系统工程师', '反欺诈风控算法'
                ],
                '研发工程师': [
                    '研发工程师', '嵌入式软件开发'
                ],
                '销售技术支持': [
                    '售前技术工程师', '销售技术工程师', '商业技术工程师', '客户成功', '售后技术工程师'
                ]
            }
            
            # Import categories and subcategories
            for category_name, subcategories in career_data.items():
                # Create or update category
                category, created = CareerCategory.objects.get_or_create(
                    name=category_name,
                    defaults={
                        'market_demand': 50,  # Default value
                        'description': f'{category_name}相关职业',
                        'order': 0
                    }
                )
                
                if created:
                    self.stdout.write(self.style.SUCCESS(f'Created category: {category_name}'))
                else:
                    # Update existing category
                    category.description = f'{category_name}相关职业'
                    category.save()
                    self.stdout.write(self.style.SUCCESS(f'Updated category: {category_name}'))
                
                # Create or update subcategories
                for subcategory_name in subcategories:
                    subcategory, sub_created = CareerSubcategory.objects.get_or_create(
                        category=category,
                        name=subcategory_name,
                        defaults={
                            'market_demand': 50,  # Default value
                            'skills': f'{subcategory_name}相关技能',
                            'description': f'{subcategory_name}职业描述',
                            'order': 0
                        }
                    )
                    
                    if sub_created:
                        self.stdout.write(self.style.SUCCESS(f'  Created subcategory: {subcategory_name}'))
                    else:
                        # Update existing subcategory
                        subcategory.skills = f'{subcategory_name}相关技能'
                        subcategory.description = f'{subcategory_name}职业描述'
                        subcategory.save()
                        self.stdout.write(self.style.SUCCESS(f'  Updated subcategory: {subcategory_name}'))
            
            self.stdout.write(self.style.SUCCESS('Import completed successfully'))
            
        except Exception as e:
            self.stdout.write(self.style.ERROR(f'Error during import: {str(e)}'))
