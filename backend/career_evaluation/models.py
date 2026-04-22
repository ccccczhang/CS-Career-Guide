from django.db import models


class CareerCategory(models.Model):
    name = models.CharField(max_length=100, verbose_name='职业大类')
    market_demand = models.IntegerField(default=0, verbose_name='市场需求')
    description = models.TextField(verbose_name='大类介绍')
    order = models.IntegerField(default=0, verbose_name='排序')
    created_at = models.DateTimeField(auto_now_add=True, verbose_name='创建时间')
    updated_at = models.DateTimeField(auto_now=True, verbose_name='更新时间')
    
    class Meta:
        verbose_name = '职业大类'
        verbose_name_plural = '职业大类'
        ordering = ['order', '-market_demand']
    
    def __str__(self):
        return self.name


class CareerSubcategory(models.Model):
    category = models.ForeignKey(CareerCategory, on_delete=models.CASCADE, related_name='subcategories', verbose_name='所属大类')
    name = models.CharField(max_length=100, verbose_name='职业小类')
    market_demand = models.IntegerField(default=0, verbose_name='市场需求')
    skills = models.TextField(verbose_name='技能要求')
    description = models.TextField(verbose_name='小类介绍')
    order = models.IntegerField(default=0, verbose_name='排序')
    created_at = models.DateTimeField(auto_now_add=True, verbose_name='创建时间')
    updated_at = models.DateTimeField(auto_now=True, verbose_name='更新时间')
    
    class Meta:
        verbose_name = '职业小类'
        verbose_name_plural = '职业小类'
        ordering = ['order', '-market_demand']
    
    def __str__(self):
        return f'{self.category.name} - {self.name}'


class ComputerCareer(models.Model):
    position_name = models.CharField(max_length=255, verbose_name='岗位名称', null=True, blank=True)
    address = models.CharField(max_length=255, verbose_name='地址')
    salary_range = models.CharField(max_length=100, verbose_name='薪资范围')
    company_name = models.CharField(max_length=255, verbose_name='公司名称')
    industry = models.CharField(max_length=100, verbose_name='所属行业')
    company_size = models.CharField(max_length=100, verbose_name='公司规模')
    company_type = models.CharField(max_length=100, verbose_name='公司类型')
    position_code = models.CharField(max_length=100, verbose_name='岗位编码')
    position_details = models.TextField(verbose_name='岗位详情')
    update_date = models.DateField(verbose_name='更新日期')
    company_details = models.TextField(verbose_name='公司详情')
    position_source_url = models.URLField(max_length=500, verbose_name='岗位来源地址')
    created_at = models.DateTimeField(auto_now_add=True, verbose_name='创建时间')
    updated_at = models.DateTimeField(auto_now=True, verbose_name='更新时间')
    
    class Meta:
        verbose_name = '计算机职业'
        verbose_name_plural = '计算机职业'
        ordering = ['-update_date']
    
    def __str__(self):
        return f'{self.position_name} - {self.company_name}'
