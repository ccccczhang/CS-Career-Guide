# 计算机学院本科生就业指南

面向计算机学院本科生的就业指南Web项目，采用Vue3作为前端框架，Django作为后端框架开发。

## 项目架构

- **前端**: Vue3 + Vue Router + Pinia + DaisyUI
- **后端**: Django + Django REST Framework
- **数据库**: PostgreSQL + Redis
- **预留功能**: 国内大模型API接口、3D数字人接口

## 功能模块

1. 职业发展路径选择模块（就业、考研、考公、入伍、创业）
2. 市场需求分析模块
3. 岗前培训模块
4. 就业安全与防诈骗模块

## 快速开始

### 后端启动

```bash
cd backend
pip install -r requirements.txt
python manage.py migrate
python manage.py runserver
```

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

## 许可证

MIT
