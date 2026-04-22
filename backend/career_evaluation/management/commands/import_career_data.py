import pandas as pd
from django.core.management.base import BaseCommand
from career_evaluation.models import CareerCategory, CareerSubcategory

class Command(BaseCommand):
    help = 'Import career data from Excel file'

    def add_arguments(self, parser):
        parser.add_argument('file_path', type=str, help='Path to the Excel file')

    def handle(self, *args, **options):
        file_path = options['file_path']
        
        try:
            # Read Excel file
            df = pd.read_excel(file_path)
            
            # Check required columns
            required_columns = ['岗位名称', '所属行业', '薪资范围', '岗位详情']
            if not all(col in df.columns for col in required_columns):
                self.stdout.write(self.style.ERROR('Excel file missing required columns'))
                return
            
            # Group by industry (as career category)
            categories = df.groupby('所属行业')
            
            for category_name, group in categories:
                # Create or update category
                category, created = CareerCategory.objects.get_or_create(
                    name=category_name,
                    defaults={
                        'market_demand': 50,  # Default value, can be adjusted
                        'description': f'{category_name}行业',
                        'order': 0
                    }
                )
                
                if created:
                    self.stdout.write(self.style.SUCCESS(f'Created category: {category_name}'))
                else:
                    # Update existing category
                    category.description = f'{category_name}行业'
                    category.save()
                    self.stdout.write(self.style.SUCCESS(f'Updated category: {category_name}'))
                
                # Process job positions as subcategories
                for _, row in group.iterrows():
                    subcategory_name = row['岗位名称']
                    if pd.notna(subcategory_name):
                        # Extract market demand from salary range (simplified approach)
                        salary = row.get('薪资范围', '')
                        market_demand = 50  # Default value
                        
                        # Process skills from job details
                        job_details = row.get('岗位详情', '')
                        skills = job_details[:500]  # Limit to 500 characters
                        
                        # Create description combining job details and other info
                        description_parts = []
                        if '地址' in row and pd.notna(row['地址']):
                            description_parts.append(f'地址: {row["地址"]}')
                        if '薪资范围' in row and pd.notna(row['薪资范围']):
                            description_parts.append(f'薪资: {row["薪资范围"]}')
                        if '公司名称' in row and pd.notna(row['公司名称']):
                            description_parts.append(f'公司: {row["公司名称"]}')
                        if '公司规模' in row and pd.notna(row['公司规模']):
                            description_parts.append(f'规模: {row["公司规模"]}')
                        if '公司类型' in row and pd.notna(row['公司类型']):
                            description_parts.append(f'类型: {row["公司类型"]}')
                        if '岗位详情' in row and pd.notna(row['岗位详情']):
                            description_parts.append(f'详情: {row["岗位详情"]}')
                        
                        description = '\n'.join(description_parts)
                        
                        subcategory, sub_created = CareerSubcategory.objects.get_or_create(
                            category=category,
                            name=subcategory_name,
                            defaults={
                                'market_demand': market_demand,
                                'skills': skills,
                                'description': description[:1000],  # Limit to 1000 characters
                                'order': 0
                            }
                        )
                        
                        if sub_created:
                            self.stdout.write(self.style.SUCCESS(f'  Created subcategory: {subcategory_name}'))
                        else:
                            # Update existing subcategory
                            subcategory.skills = skills
                            subcategory.description = description[:1000]
                            subcategory.save()
                            self.stdout.write(self.style.SUCCESS(f'  Updated subcategory: {subcategory_name}'))
            
            self.stdout.write(self.style.SUCCESS('Import completed successfully'))
            
        except Exception as e:
            self.stdout.write(self.style.ERROR(f'Error during import: {str(e)}'))
