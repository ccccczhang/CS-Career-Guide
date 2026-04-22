from rest_framework import serializers
from .models import CareerPath, CareerAssessment, AssessmentResult

class CareerPathSerializer(serializers.ModelSerializer):
    class Meta:
        model = CareerPath
        fields = '__all__'

class CareerAssessmentSerializer(serializers.ModelSerializer):
    class Meta:
        model = CareerAssessment
        fields = '__all__'

class AssessmentResultSerializer(serializers.ModelSerializer):
    class Meta:
        model = AssessmentResult
        fields = '__all__'
