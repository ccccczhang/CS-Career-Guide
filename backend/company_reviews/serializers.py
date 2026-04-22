from rest_framework import serializers
from .models import Company, Job, CompanyInfo, JobFair


class JobSerializer(serializers.ModelSerializer):
    class Meta:
        model = Job
        fields = ['id', 'title', 'salary_min', 'salary_max', 'requirements']


class CompanySerializer(serializers.ModelSerializer):
    jobs = JobSerializer(many=True, read_only=True)
    
    class Meta:
        model = Company
        fields = [
            'id', 'name', 'city', 'scale', 'nature', 'ranking', 
            'avg_salary', 'description', 'warning_info', 'jobs'
        ]


class CompanyInfoSerializer(serializers.ModelSerializer):
    title = serializers.CharField(source='job_title')
    company = serializers.CharField(source='company_name')
    salary = serializers.CharField(source='salary_range')
    tags = serializers.SerializerMethodField()
    jobDetails = serializers.CharField(source='job_details')
    companyDetails = serializers.CharField(source='company_details')
    jobSourceUrl = serializers.URLField(source='job_source_url')
    
    class Meta:
        model = CompanyInfo
        fields = [
            'id', 'title', 'company', 'salary', 'tags',
            'jobDetails', 'companyDetails', 'jobSourceUrl'
        ]
    
    def get_tags(self, obj):
        tags = []
        if obj.industry:
            tags.append(obj.industry)
        if obj.company_scale:
            tags.append(obj.company_scale)
        if obj.company_type:
            tags.append(obj.company_type)
        return tags


class JobFairSerializer(serializers.ModelSerializer):
    class Meta:
        model = JobFair
        fields = [
            'id', 'university', 'name', 'date', 'location',
            'contact_phone', 'contact_email', 'description',
            'registration_link', 'university_url', 'created_at', 'updated_at'
        ]
