// 更新自我介绍数据，包含项目描述
const selfIntroduction = {
  name: "用户名",
  school: "计算机学院",
  gender: "男",
  skills: ["Vue3", "Django", "PostgreSQL", "AI集成"],
  otherSkills: "全栈开发、项目管理",
  selfDescription: "我是一名计算机专业学生，专注于全栈开发和AI技术应用。",
  goal: "成为一名优秀的全栈工程师，专注于职业发展平台的开发。"
};

// 构建自我介绍文本
const selfIntroductionText = `姓名: ${selfIntroduction.name || ''}
学校: ${selfIntroduction.school || ''}
性别: ${selfIntroduction.gender || ''}
技能: ${selfIntroduction.skills?.join('、') || '无'}
其他技能: ${selfIntroduction.otherSkills || '无'}
条件自述: ${selfIntroduction.selfDescription || '无'}
职业期望: ${selfIntroduction.goal || '无'}

## 项目描述

### 项目概述
计算机学院本科生就业指南是一个专为计算机专业学生打造的全栈职业指导平台，旨在通过数据驱动和AI技术，为学生提供从职业规划到求职面试的全方位支持。

### 技术架构
- **前端**：Vue 3 + Vue Router + Pinia + DaisyUI + Tailwind CSS
- **后端**：Django + Django REST Framework
- **数据库**：PostgreSQL + Redis
- **AI集成**：LangGraph 多智能体工作流

### 核心功能模块
1. **职业发展路径选择**：就业、考研、考公、入伍、创业五大方向，20+ 细分技术方向的技能树
2. **职业测评系统**：多维人格评估、技术栈匹配度分析、个性化职业建议
3. **企业红黑榜**：真实职场评价、企业推荐与预警
4. **AI 面试模拟仓**：深度集成 LangGraph 多智能体，模拟真实技术面试官
5. **简历管理**：简历创建与优化、模板选择、简历投递跟踪
6. **用户管理系统**：个人信息管理、学习进度跟踪、个性化推荐

### 平台优势
- **全栈架构**：系统化覆盖从底层计算机组成原理到上层云原生应用的所有技能节点
- **AI 驱动**：基于最新大模型技术，为用户定制个性化的学习路径与简历改写建议
- **真实评价**：拒绝虚假宣传，所有企业点评均经过严格的人工审核与身份核验
- **沉浸式体验**：现代化的UI设计，流畅的用户交互

### 项目目标
- 为计算机学院本科生提供全方位的职业指导
- 利用数据与 AI 填补信息差
- 打造沉浸式求职体验
- 帮助学生锁定顶级 Offer
- 成为计算机专业学生职业发展的首选平台`;

console.log('更新后的自我介绍文本:');
console.log(selfIntroductionText);

// 保存到localStorage
// localStorage.setItem('selfIntroduction', JSON.stringify(selfIntroduction));
// localStorage.setItem('selfIntroductionText', selfIntroductionText);

console.log('\n请将以上内容复制到个人资料的自我介绍中。');
