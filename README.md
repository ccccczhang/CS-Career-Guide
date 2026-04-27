# 计算机学院本科生就业指南

计算机学院本科生就业指南是一个专为计算机专业学生打造的**全栈职业指导平台**，通过数据驱动和AI技术，为学生提供从职业规划到求职面试的全方位支持。

## 🎯 项目目标

- 为计算机学院本科生提供全方位的职业指导
- 利用数据与AI填补信息差
- 打造沉浸式求职体验
- 帮助学生锁定顶级Offer

## 🏗️ 技术架构

### 前端技术栈
- **框架**: Vue 3 + Vue Router + Pinia
- **UI组件**: DaisyUI
- **构建工具**: Vite
- **样式**: Tailwind CSS

### 后端技术栈
- **框架**: Django + Django REST Framework
- **数据库**: PostgreSQL + Redis + LanceDB
- **AI集成**: LangGraph 多智能体工作流
- **语义搜索**: Sentence-BERT (bge-small-zh-v1.5)

### 预留功能接口
- 国内大模型API接口
- 3D数字人接口

## ✨ 核心功能模块

### 1. 职业发展路径选择
- 就业、考研、考公、入伍、创业五大方向
- 20+ 细分技术方向的技能树
- 根据技术趋势动态更新的学习路径

### 2. 职业测评系统
- 多维人格评估
- 技术栈匹配度分析
- 个性化职业建议

### 3. 企业评价系统
- 真实职场评价（脱敏处理）
- 企业推荐与预警
- 严格的人工审核与身份核验

### 4. AI 面试模拟仓
- 深度集成 LangGraph 多智能体
- 模拟真实技术面试官
- 实时反馈与专业改进建议
- 支持语音交互（ASR/TTS）

### 5. 简历管理
- 简历创建与优化
- 模板选择
- 简历投递跟踪

### 6. 语义搜索
- 基于向量数据库的智能搜索
- 支持关键词搜索、语义搜索、混合搜索

## 🚀 快速开始

### 环境要求
- Python >= 3.8
- Node.js >= 18
- PostgreSQL >= 14
- Redis >= 7

### 后端启动

```bash
cd backend
pip install -r requirements.txt
python manage.py migrate
python manage.py runserver 0.0.0.0:8000
```

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

### 配置说明

1. 创建 `backend/.env` 文件：
```env
DEBUG=True
SECRET_KEY=your-secret-key
DATABASE_URL=postgres://user:password@localhost:5432/career_guide
REDIS_URL=redis://localhost:6379/0
```

## 🌟 技术亮点

- **多智能体协作**: 基于LangGraph构建的面试代理、职业代理、推荐代理等专业智能体
- **实时语音交互**: 集成语音识别(VAD)和语音合成(TTS)功能
- **语义搜索**: 使用LanceDB向量数据库实现精准职业推荐
- **响应式设计**: 现代化UI设计，支持移动端访问

## 📁 项目结构

```
cs-career-guide/
├── backend/                    # 后端Django项目
│   ├── ai_integration/         # AI集成模块
│   ├── career_evaluation/      # 职业测评模块
│   ├── career_paths/           # 职业路径模块
│   ├── company_reviews/        # 企业评价模块
│   └── cs_career_guide/        # Django配置
├── frontend/                   # 前端Vue项目
│   ├── src/
│   │   ├── components/         # 组件
│   │   ├── views/              # 页面视图
│   │   ├── api/                # API接口
│   │   └── stores/             # 状态管理
│   └── public/                 # 静态资源
└── README.md                   # 项目文档
```

## 📊 适用人群

- 计算机学院本科生
- 计算机相关专业学生
- 希望进入IT行业的求职者
- 需要职业规划指导的技术人员

## 📄 许可证

MIT License

## 🤝 贡献

欢迎提交Issue和Pull Request！