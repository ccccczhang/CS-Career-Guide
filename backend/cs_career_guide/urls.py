from django.contrib import admin
from django.urls import path, include
from django.conf import settings
from django.conf.urls.static import static

urlpatterns = [
    path('admin/', admin.site.urls),
    path('api/career-paths/', include('career_paths.urls')),
    path('api/company-reviews/', include('company_reviews.urls')),
    path('api/career-evaluation/', include('career_evaluation.urls')),
    path('api/users/', include('users.urls')),
    path('api/ai/', include('ai_integration.urls')),
    path('api/resumes/', include('resumes.urls')),
    path('api/asr/', include('asr.urls')),

]

if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
