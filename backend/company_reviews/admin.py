from django.contrib import admin
from .models import Company, Job, CompanyInfo, JobFair

# 企业招聘管理
@admin.register(JobFair)
class JobFairAdmin(admin.ModelAdmin):
    list_display = ['university', 'name', 'date', 'location', 'created_at']
    list_filter = ['university', 'date']
    search_fields = ['name', 'location', 'description']
    ordering = ['-date']
    fields = ['university', 'name', 'date', 'location', 'contact_phone', 'contact_email', 'description', 'registration_link', 'university_url']
