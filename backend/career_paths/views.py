from rest_framework import viewsets, status
from rest_framework.decorators import action
from rest_framework.response import Response
from django.core.cache import cache
from .models import CareerPath, CareerAssessment, AssessmentResult
from .serializers import CareerPathSerializer, CareerAssessmentSerializer, AssessmentResultSerializer
import uuid

class CareerPathViewSet(viewsets.ReadOnlyModelViewSet):
    queryset = CareerPath.objects.all()
    serializer_class = CareerPathSerializer

    def list(self, request, *args, **kwargs):
        cache_key = 'career_paths_all'
        cached_data = cache.get(cache_key)
        if cached_data:
            return Response(cached_data)
        response = super().list(request, *args, **kwargs)
        cache.set(cache_key, response.data, 3600)
        return response

class CareerAssessmentViewSet(viewsets.ReadOnlyModelViewSet):
    queryset = CareerAssessment.objects.filter(is_active=True)
    serializer_class = CareerAssessmentSerializer

    @action(detail=False, methods=['post'])
    def submit(self, request):
        answers = request.data.get('answers', {})
        session_id = str(uuid.uuid4())
        
        path_scores = {
            'employment': 0,
            'postgraduate': 0,
            'civil_service': 0,
            'military': 0,
            'entrepreneurship': 0,
        }
        
        assessments = self.get_queryset()
        for assessment in assessments:
            answer = answers.get(str(assessment.id))
            if answer and answer in assessment.path_weights:
                for path, weight in assessment.path_weights[answer].items():
                    path_scores[path] += weight
        
        recommended_path = max(path_scores.items(), key=lambda x: x[1])[0]
        total_score = sum(path_scores.values())
        confidence = path_scores[recommended_path] / total_score if total_score > 0 else 0
        
        result = AssessmentResult.objects.create(
            session_id=session_id,
            answers=answers,
            recommended_path=recommended_path,
            confidence_score=confidence
        )
        
        return Response({
            'session_id': session_id,
            'recommended_path': recommended_path,
            'confidence_score': confidence,
            'path_scores': path_scores
        }, status=status.HTTP_201_CREATED)
