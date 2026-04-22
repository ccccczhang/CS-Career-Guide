from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import CompanyViewSet, CompanyInfoViewSet, JobFairViewSet

router = DefaultRouter()
router.register(r'companies', CompanyViewSet)
router.register(r'company-info', CompanyInfoViewSet)
router.register(r'job-fairs', JobFairViewSet)

urlpatterns = [
    path('', include(router.urls)),
]
