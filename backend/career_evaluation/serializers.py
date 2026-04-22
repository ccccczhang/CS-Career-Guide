from rest_framework import serializers
from .models import CareerCategory, CareerSubcategory, ComputerCareer


class CareerSubcategorySerializer(serializers.ModelSerializer):
    class Meta:
        model = CareerSubcategory
        fields = ['id', 'name', 'market_demand', 'skills', 'description']


class CareerCategorySerializer(serializers.ModelSerializer):
    subcategories = CareerSubcategorySerializer(many=True, read_only=True)
    
    class Meta:
        model = CareerCategory
        fields = ['id', 'name', 'market_demand', 'description', 'subcategories']


class ComputerCareerSerializer(serializers.ModelSerializer):
    class Meta:
        model = ComputerCareer
        fields = [
            'id', 'position_name', 'address', 'salary_range', 'company_name',
            'industry', 'company_size', 'company_type', 'position_code',
            'position_details', 'update_date', 'company_details', 'position_source_url'
        ]
