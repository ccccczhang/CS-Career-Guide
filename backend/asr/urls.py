from django.urls import path
from .asr import ASRView

urlpatterns = [
    path('speech-to-text/', ASRView.as_view(), name='asr-speech-to-text'),
]
