from django.core.management.base import BaseCommand
from career_evaluation.models import CareerCategory, CareerSubcategory

class Command(BaseCommand):
    help = 'Clear all career categories and subcategories'

    def handle(self, *args, **options):
        try:
            # Delete all subcategories first
            subcategory_count = CareerSubcategory.objects.count()
            CareerSubcategory.objects.all().delete()
            self.stdout.write(self.style.SUCCESS(f'Deleted {subcategory_count} subcategories'))
            
            # Delete all categories
            category_count = CareerCategory.objects.count()
            CareerCategory.objects.all().delete()
            self.stdout.write(self.style.SUCCESS(f'Deleted {category_count} categories'))
            
            self.stdout.write(self.style.SUCCESS('All career data cleared successfully'))
            
        except Exception as e:
            self.stdout.write(self.style.ERROR(f'Error during clearing: {str(e)}'))
