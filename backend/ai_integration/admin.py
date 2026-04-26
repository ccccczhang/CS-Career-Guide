from django.contrib import admin
from .models import AIConversation, AITag, CareerEvaluationRecord, CareerPlanReport, AIUsageRecord, CareerRecommendationRecord, AIChatPair, AIPrompt, SeniorAdvice, InterviewReviewRecord


@admin.register(AITag)
class AITagAdmin(admin.ModelAdmin):
    list_display = ('name', 'description', 'created_at')
    search_fields = ('name', 'description')
    ordering = ('-created_at',)


@admin.register(AIConversation)
class AIConversationAdmin(admin.ModelAdmin):
    list_display = ('session_id', 'mode', 'get_user_info', 'created_at')
    list_filter = ('mode', 'created_at')
    search_fields = ('session_id', 'user__username')
    ordering = ('-created_at',)
    filter_horizontal = ('tags',)
    
    def get_user_info(self, obj):
        if obj.user:
            return f"{obj.user.username} (ID: {obj.user.id})"
        return "无"
    get_user_info.short_description = '用户'





@admin.register(AIChatPair)
class AIChatPairAdmin(admin.ModelAdmin):
    list_display = ('conversation', 'input_time', 'output_time')
    list_filter = ('input_time', 'output_time')
    search_fields = ('user_input', 'ai_output', 'conversation__session_id')
    ordering = ('-input_time',)


@admin.register(CareerEvaluationRecord)
class CareerEvaluationRecordAdmin(admin.ModelAdmin):
    list_display = ('session_id', 'get_user_info', 'created_at')
    list_filter = ('created_at',)
    search_fields = ('session_id', 'user__username')
    ordering = ('-created_at',)
    filter_horizontal = ('tags',)
    
    def get_user_info(self, obj):
        if obj.user:
            return f"{obj.user.username} (ID: {obj.user.id})"
        return "无"
    get_user_info.short_description = '用户'


@admin.register(CareerPlanReport)
class CareerPlanReportAdmin(admin.ModelAdmin):
    list_display = ('session_id', 'get_user_info', 'created_at')
    list_filter = ('created_at',)
    search_fields = ('session_id', 'user__username')
    ordering = ('-created_at',)
    filter_horizontal = ('tags',)
    
    def get_user_info(self, obj):
        if obj.user:
            return f"{obj.user.username} (ID: {obj.user.id})"
        return "无"
    get_user_info.short_description = '用户'


@admin.register(AIUsageRecord)
class AIUsageRecordAdmin(admin.ModelAdmin):
    list_display = ('get_user_info', 'usage_type', 'related_id', 'created_at')
    list_filter = ('usage_type', 'created_at')
    search_fields = ('user__username', 'related_id')
    ordering = ('-created_at',)
    filter_horizontal = ('tags',)
    
    def get_user_info(self, obj):
        if obj.user:
            return f"{obj.user.username} (ID: {obj.user.id})"
        return "无"
    get_user_info.short_description = '用户'


@admin.register(CareerRecommendationRecord)
class CareerRecommendationRecordAdmin(admin.ModelAdmin):
    list_display = ('session_id', 'get_user_info', 'created_at')
    list_filter = ('created_at',)
    search_fields = ('session_id', 'user__username')
    ordering = ('-created_at',)
    # 暂时注释掉filter_horizontal，因为tags字段已被注释
    # filter_horizontal = ('tags',)
    fields = ('session_id', 'user', 'self_introduction', 'recommendations', 'analysis_result', 'created_at', 'updated_at')
    readonly_fields = ('created_at', 'updated_at')
    
    def get_user_info(self, obj):
        if obj.user:
            return f"{obj.user.username} (ID: {obj.user.id})"
        return "无"
    get_user_info.short_description = '用户'


@admin.register(AIPrompt)
class AIPromptAdmin(admin.ModelAdmin):
    list_display = ('prompt_type', 'is_active', 'created_at')
    list_filter = ('is_active', 'prompt_type')
    search_fields = ('system_prompt',)
    ordering = ('prompt_type',)
    
    def get_form(self, request, obj=None, **kwargs):
        form = super().get_form(request, obj, **kwargs)
        # 调整字段顺序
        if obj:  # 编辑现有对象时
            form.base_fields['prompt_type'].widget.attrs['readonly'] = True  # 类型一旦创建就不能修改
        return form


@admin.register(SeniorAdvice)
class SeniorAdviceAdmin(admin.ModelAdmin):
    list_display = ('company', 'position', 'senior_name', 'graduation_year', 'rating', 'is_approved', 'created_at')
    list_filter = ('is_approved', 'created_at')
    search_fields = ('company', 'position', 'senior_name')
    actions = ['approve_selected']
    
    def approve_selected(self, request, queryset):
        queryset.update(is_approved=True)
    approve_selected.short_description = '审核通过选中的建议'


@admin.register(InterviewReviewRecord)
class InterviewReviewRecordAdmin(admin.ModelAdmin):
    list_display = ('conversation', 'interview_style', 'overall_score', 'interview_duration', 'question_count', 'created_at')
    list_filter = ('interview_style', 'created_at')
    search_fields = ('conversation__session_id', 'review_content', 'user__username')
    ordering = ('-created_at',)
    readonly_fields = ('created_at', 'updated_at')
    
    def get_user_info(self, obj):
        if obj.user:
            return f"{obj.user.username} (ID: {obj.user.id})"
        return "无"
    get_user_info.short_description = '用户'
