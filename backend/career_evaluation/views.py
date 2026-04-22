from rest_framework import viewsets
from rest_framework.pagination import PageNumberPagination
from rest_framework.permissions import AllowAny
from .models import CareerCategory, ComputerCareer
from .serializers import CareerCategorySerializer, ComputerCareerSerializer


class StandardPagination(PageNumberPagination):
    page_size = 10
    page_size_query_param = 'page_size'
    max_page_size = 100


class CareerCategoryViewSet(viewsets.ModelViewSet):
    queryset = CareerCategory.objects.all()
    serializer_class = CareerCategorySerializer
    
    def get_queryset(self):
        # 按市场需求和排序字段排序
        return CareerCategory.objects.order_by('order', '-market_demand')


class ComputerCareerViewSet(viewsets.ReadOnlyModelViewSet):
    """计算机职业API"""
    queryset = ComputerCareer.objects.all()
    serializer_class = ComputerCareerSerializer
    pagination_class = StandardPagination
    permission_classes = [AllowAny]  # 允许任何请求
    
    def get_queryset(self):
        queryset = super().get_queryset()
        
        # 筛选参数
        position_name = self.request.query_params.get('position_name')
        company_name = self.request.query_params.get('company_name')
        address = self.request.query_params.get('address')
        industry = self.request.query_params.get('industry')
        
        # 应用筛选
        if position_name:
            queryset = queryset.filter(position_name__icontains=position_name)
        if company_name:
            queryset = queryset.filter(company_name__icontains=company_name)
        if address:
            queryset = queryset.filter(address__icontains=address)
        if industry:
            queryset = queryset.filter(industry__icontains=industry)
        
        # 按更新日期排序
        queryset = queryset.order_by('-update_date')
        
        return queryset
