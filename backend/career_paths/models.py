from django.db import models

class CareerPath(models.Model):
    PATH_CHOICES = [
        ('employment', '就业'),
        ('postgraduate', '考研'),
        ('civil_service', '考公'),
        ('military', '入伍'),
        ('entrepreneurship', '创业'),
    ]

    path_type = models.CharField(max_length=20, choices=PATH_CHOICES, unique=True)
    title = models.CharField(max_length=100)
    description = models.TextField()
    preparation_advice = models.TextField()
    time_plan = models.JSONField(default=dict)
    resource_links = models.JSONField(default=list)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        ordering = ['path_type']

    def __str__(self):
        return self.title

class CareerAssessment(models.Model):
    question = models.TextField()
    options = models.JSONField(default=list)
    path_weights = models.JSONField(default=dict)
    order = models.IntegerField(default=0)
    is_active = models.BooleanField(default=True)

    class Meta:
        ordering = ['order']

    def __str__(self):
        return self.question[:50]

class AssessmentResult(models.Model):
    session_id = models.CharField(max_length=100)
    answers = models.JSONField(default=dict)
    recommended_path = models.CharField(max_length=20, choices=CareerPath.PATH_CHOICES)
    confidence_score = models.FloatField()
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.session_id} - {self.recommended_path}"
