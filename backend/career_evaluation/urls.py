from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import CareerCategoryViewSet, ComputerCareerViewSet

router = DefaultRouter()
router.register(r'categories', CareerCategoryViewSet)
router.register(r'computer-careers', ComputerCareerViewSet)

urlpatterns = [
    path('', include(router.urls)),
]
