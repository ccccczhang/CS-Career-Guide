from rest_framework import viewsets, status
from rest_framework.decorators import action
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticated
from django.core.files.storage import default_storage
from django.core.files.base import ContentFile
import os
import uuid

from .models import Resume, Education, Experience
from .serializers import ResumeSerializer


class ResumeViewSet(viewsets.ModelViewSet):
    serializer_class = ResumeSerializer
    permission_classes = [IsAuthenticated]
    
    def get_queryset(self):
        return Resume.objects.filter(user=self.request.user)
    
    def perform_create(self, serializer):
        serializer.save(user=self.request.user)
    
    def list(self, request):
        try:
            resume = Resume.objects.get(user=request.user)
            serializer = self.get_serializer(resume, context={'request': request})
            return Response(serializer.data)
        except Resume.DoesNotExist:
            return Response({'detail': '简历不存在'}, status=status.HTTP_404_NOT_FOUND)
    
    @action(detail=False, methods=['get', 'post', 'put'])
    def my(self, request):
        if request.method == 'GET':
            try:
                resume = Resume.objects.get(user=request.user)
                serializer = self.get_serializer(resume)
                return Response(serializer.data)
            except Resume.DoesNotExist:
                return Response({'detail': '简历不存在'}, status=status.HTTP_404_NOT_FOUND)
        
        elif request.method in ['POST', 'PUT']:
            try:
                resume = Resume.objects.get(user=request.user)
                serializer = self.get_serializer(resume, data=request.data, partial=True, context={'request': request})
            except Resume.DoesNotExist:
                serializer = self.get_serializer(data=request.data, context={'request': request})
            
            if serializer.is_valid():
                serializer.save(user=request.user)
                return Response(serializer.data, status=status.HTTP_200_OK)
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    
    @action(detail=False, methods=['post'])
    def upload_avatar(self, request):
        try:
            resume = Resume.objects.get(user=request.user)
        except Resume.DoesNotExist:
            resume = Resume.objects.create(user=request.user)
        
        if 'avatar' not in request.FILES:
            return Response({'error': '请选择要上传的图片'}, status=status.HTTP_400_BAD_REQUEST)
        
        avatar_file = request.FILES['avatar']
        
        # 验证文件类型
        allowed_types = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
        if avatar_file.content_type not in allowed_types:
            return Response({'error': '只支持 JPEG, PNG, GIF, WEBP 格式的图片'}, status=status.HTTP_400_BAD_REQUEST)
        
        # 验证文件大小 (最大 5MB)
        if avatar_file.size > 5 * 1024 * 1024:
            return Response({'error': '图片大小不能超过 5MB'}, status=status.HTTP_400_BAD_REQUEST)
        
        # 删除旧头像
        if resume.avatar:
            resume.avatar.delete(save=False)
        
        # 保存新头像
        resume.avatar = avatar_file
        resume.save()
        
        serializer = self.get_serializer(resume, context={'request': request})
        return Response({
            'message': '头像上传成功',
            'avatar_url': serializer.data.get('avatar_url')
        })
    
    @action(detail=False, methods=['delete'])
    def delete_avatar(self, request):
        try:
            resume = Resume.objects.get(user=request.user)
            if resume.avatar:
                resume.avatar.delete()
                resume.save()
                return Response({'message': '头像删除成功'})
            return Response({'error': '没有头像可删除'}, status=status.HTTP_400_BAD_REQUEST)
        except Resume.DoesNotExist:
            return Response({'error': '简历不存在'}, status=status.HTTP_404_NOT_FOUND)
