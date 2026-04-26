from django.db import models
from django.conf import settings
from django.utils import timezone


class AITag(models.Model):
    """AI使用记录标签"""
    name = models.CharField(max_length=50, unique=True, verbose_name='标签名称')
    description = models.TextField(null=True, blank=True, verbose_name='标签描述')
    created_at = models.DateTimeField(auto_now_add=True, verbose_name='创建时间')
    
    class Meta:
        verbose_name = 'AI标签'
        verbose_name_plural = 'AI标签'
    
    def __str__(self):
        return self.name


class AIConversation(models.Model):
    """AI对话会话"""
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, null=True, blank=True, verbose_name='用户')
    session_id = models.CharField(max_length=100, unique=True, verbose_name='会话ID')
    mode = models.CharField(max_length=50, choices=[
        ('interview', '面试模式'),
        ('review', '复盘模式'),
        ('normal', '普通模式'),
        ('career', '职业规划模式')
    ], verbose_name='对话模式')
    tags = models.ManyToManyField(AITag, blank=True, related_name='conversations', verbose_name='标签')
    context_aware = models.BooleanField(default=True, verbose_name='是否上下文感知')
    short_term_memory_size = models.IntegerField(default=10, verbose_name='短期记忆大小')
    created_at = models.DateTimeField(auto_now_add=True, verbose_name='创建时间')
    updated_at = models.DateTimeField(auto_now=True, verbose_name='更新时间')
    
    class Meta:
        verbose_name = 'AI对话会话'
        verbose_name_plural = 'AI对话会话'
        ordering = ['-created_at']
    
    def __str__(self):
        return f"{self.session_id} - {self.mode}"


class LongTermMemory(models.Model):
    """长期记忆存储"""
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, null=True, blank=True, verbose_name='用户')
    key = models.CharField(max_length=100, verbose_name='记忆键')
    value = models.TextField(verbose_name='记忆值')
    category = models.CharField(max_length=50, choices=[
        ('career_goal', '职业目标'),
        ('skills', '技能特长'),
        ('interview_feedback', '面试反馈'),
        ('preferences', '重要偏好'),
        ('other', '其他')
    ], verbose_name='记忆类别')
    conversation = models.ForeignKey(AIConversation, on_delete=models.CASCADE, null=True, blank=True, related_name='long_term_memories', verbose_name='关联会话')
    created_at = models.DateTimeField(auto_now_add=True, verbose_name='创建时间')
    updated_at = models.DateTimeField(auto_now=True, verbose_name='更新时间')
    
    class Meta:
        verbose_name = '长期记忆'
        verbose_name_plural = '长期记忆'
        ordering = ['-updated_at']
    
    def __str__(self):
        return f"{self.key} - {self.category}"


class AIChatPair(models.Model):
    """AI聊天问答对"""
    conversation = models.ForeignKey(AIConversation, on_delete=models.CASCADE, related_name='pairs', verbose_name='会话')
    user_input = models.TextField(verbose_name='用户输入')
    ai_output = models.TextField(verbose_name='AI输出')
    input_time = models.DateTimeField(auto_now_add=True, verbose_name='输入时间')
    output_time = models.DateTimeField(null=True, blank=True, verbose_name='输出时间')
    
    class Meta:
        verbose_name = 'AI聊天问答对'
        verbose_name_plural = 'AI聊天问答对'
        ordering = ['input_time']
    
    def __str__(self):
        return f"{self.conversation.session_id} - Q&A"





class CareerEvaluationRecord(models.Model):
    """职业评测记录"""
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, null=True, blank=True, verbose_name='用户')
    session_id = models.CharField(max_length=100, unique=True, verbose_name='评测会话ID')
    selected_careers = models.JSONField(default=list, verbose_name='选择的职业')
    evaluation_result = models.JSONField(default=dict, verbose_name='评测结果')
    tags = models.ManyToManyField(AITag, blank=True, related_name='evaluation_records', verbose_name='标签')
    created_at = models.DateTimeField(auto_now_add=True, verbose_name='创建时间')
    updated_at = models.DateTimeField(auto_now=True, verbose_name='更新时间')
    
    class Meta:
        verbose_name = '职业评测记录'
        verbose_name_plural = '职业评测记录'
        ordering = ['-created_at']
    
    def __str__(self):
        return f"{self.session_id}"


class CareerPlanReport(models.Model):
    """职业生涯规划报告"""
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, null=True, blank=True, verbose_name='用户')
    session_id = models.CharField(max_length=100, unique=True, verbose_name='报告会话ID')
    report_content = models.TextField(verbose_name='报告内容')
    radar_data = models.JSONField(default=dict, verbose_name='雷达图数据')
    tags = models.ManyToManyField(AITag, blank=True, related_name='plan_reports', verbose_name='标签')
    created_at = models.DateTimeField(auto_now_add=True, verbose_name='创建时间')
    updated_at = models.DateTimeField(auto_now=True, verbose_name='更新时间')
    
    class Meta:
        verbose_name = '职业生涯规划报告'
        verbose_name_plural = '职业生涯规划报告'
        ordering = ['-created_at']
    
    def __str__(self):
        return f"{self.session_id}"


class CareerRecommendationRecord(models.Model):
    """职业推荐记录"""
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, null=True, blank=True, verbose_name='用户')
    session_id = models.CharField(max_length=100, unique=True, verbose_name='推荐会话ID')
    self_introduction = models.TextField(verbose_name='自我介绍内容')
    introduction_preview = models.TextField(blank=True, verbose_name='自我介绍预览（高亮部分）')
    recommendations = models.JSONField(default=list, verbose_name='推荐结果')
    analysis_result = models.TextField(blank=True, verbose_name='大模型分析结果')
    # 暂时注释掉tags字段，因为中间表不存在
    # tags = models.ManyToManyField(AITag, blank=True, related_name='recommendation_records', verbose_name='标签')
    created_at = models.DateTimeField(auto_now_add=True, verbose_name='创建时间')
    updated_at = models.DateTimeField(auto_now=True, verbose_name='更新时间')
    
    class Meta:
        verbose_name = '职业推荐记录'
        verbose_name_plural = '职业推荐记录'
        ordering = ['-created_at']
    
    def __str__(self):
        return f"{self.session_id}"


class AIUsageRecord(models.Model):
    """统一AI使用记录"""
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, null=True, blank=True, verbose_name='用户')
    usage_type = models.CharField(max_length=50, choices=[
        ('chat', '聊天对话'),
        ('career_consultant', '职业规划咨询'),
        ('evaluation', '职业评测'),
        ('plan', '职业规划报告'),
        ('recommendation', '职业推荐')
    ], verbose_name='使用类型')
    related_id = models.CharField(max_length=100, verbose_name='关联ID')
    tags = models.ManyToManyField(AITag, blank=True, related_name='usage_records', verbose_name='标签')
    created_at = models.DateTimeField(auto_now_add=True, verbose_name='创建时间')
    
    class Meta:
        verbose_name = 'AI使用记录'
        verbose_name_plural = 'AI使用记录'
        ordering = ['-created_at']
    
    def __str__(self):
        return f"{self.user.username if self.user else '匿名'} - {self.usage_type}"


class AIPrompt(models.Model):
    """AI提示词配置"""
    PROMPT_TYPES = [
        ('chat', '闲聊模式'),
        ('interview', '面试模式'),
        ('review', '面试复盘模式'),
        ('career', '职业规划模式'),
        ('recommendation', '职业推荐模式'),
        ('resume', '简历模式'),
    ]
    
    prompt_type = models.CharField(max_length=50, choices=PROMPT_TYPES, unique=True, verbose_name='提示词类型')
    system_prompt = models.TextField(verbose_name='系统提示词')
    user_prompt_template = models.TextField(null=True, blank=True, verbose_name='用户提示词模板')
    is_active = models.BooleanField(default=True, verbose_name='是否激活')
    created_at = models.DateTimeField(auto_now_add=True, verbose_name='创建时间')
    updated_at = models.DateTimeField(auto_now=True, verbose_name='更新时间')
    
    class Meta:
        verbose_name = 'AI提示词配置'
        verbose_name_plural = 'AI提示词配置'
        ordering = ['prompt_type']
    
    def __str__(self):
        return f"{self.get_prompt_type_display()} - {'激活' if self.is_active else '未激活'}"


class SeniorAdvice(models.Model):
    """学长学姐建议模型"""
    # 基本信息
    company = models.CharField(max_length=255, verbose_name="公司名称")
    position = models.CharField(max_length=255, verbose_name="职位名称")
    senior_name = models.CharField(max_length=100, verbose_name="学长/学姐姓名")
    graduation_year = models.CharField(max_length=4, verbose_name="毕业年份")
    current_company = models.CharField(max_length=255, verbose_name="当前公司")
    
    # 建议内容
    advice = models.TextField(verbose_name="详细建议")
    salary_info = models.CharField(max_length=100, verbose_name="薪资信息")
    pros = models.JSONField(verbose_name="优点", default=list)
    cons = models.JSONField(verbose_name="缺点", default=list)
    rating = models.FloatField(verbose_name="评分", max_length=5.0)
    
    # 元数据
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.SET_NULL, null=True, verbose_name="提交用户")
    created_at = models.DateTimeField(auto_now_add=True, verbose_name="创建时间")
    updated_at = models.DateTimeField(auto_now=True, verbose_name="更新时间")
    is_approved = models.BooleanField(default=False, verbose_name="是否审核通过")
    
    class Meta:
        verbose_name = "学长学姐建议"
        verbose_name_plural = "学长学姐建议"
        ordering = ['-created_at']
    
    def __str__(self):
        return f"{self.company} - {self.position} - {self.senior_name}"


class InterviewReviewRecord(models.Model):
    """面试复盘记录"""
    # 关联用户和会话
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, null=True, blank=True, verbose_name='用户')
    conversation = models.ForeignKey(AIConversation, on_delete=models.CASCADE, related_name='review_records', verbose_name='关联会话')
    
    # 面试信息
    interview_style = models.CharField(max_length=50, choices=[
        ('gentle', '温和面'),
        ('technical', '技术面'),
        ('pressure', '压力面'),
        ('behavioral', '行为面')
    ], verbose_name='面试风格')
    interview_duration = models.IntegerField(null=True, blank=True, verbose_name='面试时长(分钟)')
    
    # 复盘内容
    review_content = models.TextField(verbose_name='复盘报告内容')
    strengths = models.TextField(null=True, blank=True, verbose_name='优点总结')
    weaknesses = models.TextField(null=True, blank=True, verbose_name='待改进点')
    suggestions = models.TextField(null=True, blank=True, verbose_name='改进建议')
    
    # 评分
    overall_score = models.FloatField(null=True, blank=True, verbose_name='综合评分')
    communication_score = models.FloatField(null=True, blank=True, verbose_name='沟通表达评分')
    technical_score = models.FloatField(null=True, blank=True, verbose_name='技术能力评分')
    
    # 统计信息
    question_count = models.IntegerField(default=0, verbose_name='问题数量')
    answer_count = models.IntegerField(default=0, verbose_name='回答数量')
    
    # 元数据
    created_at = models.DateTimeField(auto_now_add=True, verbose_name='创建时间')
    updated_at = models.DateTimeField(auto_now=True, verbose_name='更新时间')
    
    class Meta:
        verbose_name = '面试复盘记录'
        verbose_name_plural = '面试复盘记录'
        ordering = ['-created_at']
    
    def __str__(self):
        return f"复盘记录 - {self.conversation.session_id}"
