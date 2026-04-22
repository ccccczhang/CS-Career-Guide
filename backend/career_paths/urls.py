from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import CareerPathViewSet, CareerAssessmentViewSet

router = DefaultRouter()
router.register(r'paths', CareerPathViewSet)
router.register(r'assessments', CareerAssessmentViewSet)

urlpatterns = [
    path('', include(router.urls)),
]
