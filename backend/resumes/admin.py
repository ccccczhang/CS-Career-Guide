from django.contrib import admin
from .models import Resume, Education, Experience, Project, Certificate, Award


class EducationInline(admin.TabularInline):
    model = Education
    extra = 0
    fields = ('school', 'major', 'period', 'description')
    show_change_link = False
    can_delete = True


class ExperienceInline(admin.TabularInline):
    model = Experience
    extra = 0
    fields = ('company', 'position', 'period', 'description')
    show_change_link = False
    can_delete = True


class ProjectInline(admin.TabularInline):
    model = Project
    extra = 0
    fields = ('name', 'role', 'period', 'url', 'description')
    show_change_link = False
    can_delete = True


class CertificateInline(admin.TabularInline):
    model = Certificate
    extra = 0
    fields = ('name', 'issuer', 'period', 'description')
    show_change_link = False
    can_delete = True


@admin.register(Resume)
class ResumeAdmin(admin.ModelAdmin):
    list_display = ('user', 'name', 'target_position', 'phone', 'email', 'updated_at')
    list_filter = ('updated_at',)
    search_fields = ('user__username', 'name', 'target_position', 'phone', 'email')
    readonly_fields = ('created_at', 'updated_at')
    fieldsets = (
        ('基本信息', {
            'fields': ('user', 'name', 'target_position', 'phone', 'email', 'address', 'avatar')
        }),
        ('个人自评', {
            'fields': ('self_evaluation',)
        }),
        ('专业技能', {
            'fields': ('skills',)
        }),
        ('时间信息', {
            'fields': ('created_at', 'updated_at')
        })
    )
    inlines = [
        EducationInline,
        ExperienceInline,
        ProjectInline,
        CertificateInline
    ]
    
    def get_queryset(self, request):
        qs = super().get_queryset(request)
        return qs.select_related('user').prefetch_related('education', 'experience', 'projects', 'certificates')