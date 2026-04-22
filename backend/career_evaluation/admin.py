from django.contrib import admin
from .models import CareerCategory, CareerSubcategory, ComputerCareer

@admin.register(CareerCategory)
class CareerCategoryAdmin(admin.ModelAdmin):
    list_display = ['name', 'market_demand', 'order', 'created_at']
    list_filter = ['order']
    search_fields = ['name', 'description']

@admin.register(CareerSubcategory)
class CareerSubcategoryAdmin(admin.ModelAdmin):
    list_display = ['name', 'category', 'market_demand', 'order', 'created_at']
    list_filter = ['category']
    search_fields = ['name', 'description', 'skills']

@admin.register(ComputerCareer)
class ComputerCareerAdmin(admin.ModelAdmin):
    list_display = ['position_name', 'address', 'salary_range', 'company_name', 'industry', 'company_size', 'company_type', 'position_code', 'update_date']
    list_filter = ['industry', 'update_date']
    search_fields = ['position_name', 'company_name', 'position_code', 'address']
    ordering = ['-update_date']
    fields = ['position_name', 'address', 'salary_range', 'company_name', 'industry', 'company_size', 'company_type', 'position_code', 'position_details', 'update_date', 'company_details', 'position_source_url']
