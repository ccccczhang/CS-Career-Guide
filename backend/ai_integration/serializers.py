from rest_framework import serializers
from .models import SeniorAdvice


class SeniorAdviceSerializer(serializers.ModelSerializer):
    """学长学姐建议序列化器"""
    class Meta:
        model = SeniorAdvice
        fields = [
            'id', 'company', 'position', 'senior_name', 'graduation_year',
            'current_company', 'advice', 'salary_info', 'pros', 'cons',
            'rating', 'created_at', 'is_approved'
        ]
        read_only_fields = ['id', 'created_at', 'is_approved']