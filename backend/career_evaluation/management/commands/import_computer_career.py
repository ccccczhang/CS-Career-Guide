import pandas as pd
from django.core.management.base import BaseCommand
from career_evaluation.models import ComputerCareer
from datetime import datetime

class Command(BaseCommand):
    help = 'Import computer career data from Excel file'

    def add_arguments(self, parser):
        parser.add_argument('file_path', type=str, help='Path to the Excel file')

    def parse_date(self, date_str):
        """Parse date from various formats including Chinese format"""
        if pd.isna(date_str):
            return None
        
        if isinstance(date_str, str):
            # Try different date formats
            formats = [
                '%Y年%m月%d日',  # 2025年07月29日
                '%Y-%m-%d %H:%M:%S',  # 2025-07-27 00:24:24
                '%Y-%m-%d',      # 2025-07-29
                '%Y/%m/%d',      # 2025/07/29
            ]
            
            for fmt in formats:
                try:
                    return datetime.strptime(date_str, fmt).date()
                except ValueError:
                    pass
            
            # Handle Chinese format without year: 7月25日
            if '月' in date_str and '日' in date_str:
                try:
                    current_year = datetime.now().year
                    date_str = f'{current_year}年{date_str}'
                    return datetime.strptime(date_str, '%Y年%m月%d日').date()
                except ValueError:
                    pass
            
            # Handle Chinese format with month and day only: 2月29日
            if '月' in date_str and '日' in date_str:
                try:
                    current_year = datetime.now().year
                    date_str = f'{current_year}年{date_str}'
                    return datetime.strptime(date_str, '%Y年%m月%d日').date()
                except ValueError:
                    pass
        
        # If already a date object
        if isinstance(date_str, datetime):
            return date_str.date()
        
        return None

    def handle(self, *args, **options):
        file_path = options['file_path']
        
        try:
            # Read Excel file
            df = pd.read_excel(file_path)
            
            # Check required columns
            required_columns = ['岗位名称', '地址', '薪资范围', '公司名称', '所属行业', '公司规模', 
                              '公司类型', '岗位编码', '岗位详情', '更新日期', '公司详情', '岗位来源地址']
            
            if not all(col in df.columns for col in required_columns):
                missing_columns = [col for col in required_columns if col not in df.columns]
                self.stdout.write(self.style.ERROR(f'Excel file missing required columns: {missing_columns}'))
                return
            
            # Import data
            imported_count = 0
            updated_count = 0
            skipped_count = 0
            
            for index, row in df.iterrows():
                # Convert update date
                update_date = self.parse_date(row['更新日期'])
                if update_date is None:
                    self.stdout.write(self.style.WARNING(f'Skipped row {index} due to invalid date format: {row["更新日期"]}'))
                    skipped_count += 1
                    continue
                
                # Get position code
                position_code = row['岗位编码']
                if pd.isna(position_code):
                    self.stdout.write(self.style.WARNING(f'Skipped row {index} due to missing position code'))
                    skipped_count += 1
                    continue
                
                # Create or update computer career
                computer_career, created = ComputerCareer.objects.get_or_create(
                    position_code=position_code,
                    defaults={
                        'position_name': row.get('岗位名称', ''),
                        'address': row.get('地址', ''),
                        'salary_range': row.get('薪资范围', ''),
                        'company_name': row.get('公司名称', ''),
                        'industry': row.get('所属行业', ''),
                        'company_size': row.get('公司规模', ''),
                        'company_type': row.get('公司类型', ''),
                        'position_details': row.get('岗位详情', ''),
                        'update_date': update_date,
                        'company_details': row.get('公司详情', ''),
                        'position_source_url': row.get('岗位来源地址', '')
                    }
                )
                
                if created:
                    imported_count += 1
                    if imported_count % 100 == 0:
                        self.stdout.write(self.style.SUCCESS(f'Imported {imported_count} records so far...'))
                else:
                    # Update existing record
                    computer_career.position_name = row.get('岗位名称', '')
                    computer_career.address = row.get('地址', '')
                    computer_career.salary_range = row.get('薪资范围', '')
                    computer_career.company_name = row.get('公司名称', '')
                    computer_career.industry = row.get('所属行业', '')
                    computer_career.company_size = row.get('公司规模', '')
                    computer_career.company_type = row.get('公司类型', '')
                    computer_career.position_details = row.get('岗位详情', '')
                    computer_career.update_date = update_date
                    computer_career.company_details = row.get('公司详情', '')
                    computer_career.position_source_url = row.get('岗位来源地址', '')
                    computer_career.save()
                    updated_count += 1
                    if updated_count % 100 == 0:
                        self.stdout.write(self.style.SUCCESS(f'Updated {updated_count} records so far...'))
            
            self.stdout.write(self.style.SUCCESS(f'Import completed successfully. Imported: {imported_count}, Updated: {updated_count}, Skipped: {skipped_count}'))
            
        except Exception as e:
            self.stdout.write(self.style.ERROR(f'Error during import: {str(e)}'))
