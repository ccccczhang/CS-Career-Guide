"""
A-RAG代理实现 - 结合职业推荐与A-RAG
"""

import json
import time
from typing import List, Dict, Any, Optional
from ai_integration.arag_integration import get_career_search


class ContextTracker:
    """
    上下文追踪器 - 避免重复读取同一个块
    """
    def __init__(self):
        self.read_blocks = set()   # 已读取的块ID
    
    def has_read(self, block_id):
        return block_id in self.read_blocks
    
    def mark_read(self, block_id):
        self.read_blocks.add(block_id)
    
    def reset(self):
        self.read_blocks.clear()


class ARAGAgent:
    """
    A-RAG代理 - 实现职业推荐与A-RAG融合
    """
    
    def __init__(self):
        self.career_search = get_career_search()
        self.tracker = ContextTracker()
    
    def keyword_search(self, query: str, limit: int = 3) -> Dict[str, Any]:
        """
        关键词搜索工具
        
        Args:
            query: 搜索关键词
            limit: 返回结果数量
            
        Returns:
            搜索结果
        """
        try:
            results = self.career_search.keyword_search(query, top_k=limit)
            # 只返回预览信息
            formatted_results = []
            for result in results:
                formatted_results.append({
                    "id": result.get("id"),
                    "preview": result.get("introduction_preview", result.get("self_introduction", "")[:150] + "..."),
                    "match_score": result.get("match_score", 0)
                })
            
            return {
                "success": True,
                "results": formatted_results,
                "count": len(formatted_results)
            }
        except Exception as e:
            return {
                "success": False,
                "error": str(e)
            }
    
    def semantic_search(self, query: str, limit: int = 3) -> Dict[str, Any]:
        """
        语义搜索工具
        
        Args:
            query: 搜索查询
            limit: 返回结果数量
            
        Returns:
            搜索结果
        """
        try:
            results = self.career_search.semantic_search(query, top_k=limit)
            # 只返回预览信息
            formatted_results = []
            for result in results:
                formatted_results.append({
                    "id": result.get("id"),
                    "preview": result.get("introduction_preview", result.get("self_introduction", "")[:150] + "..."),
                    "match_score": result.get("match_score", 0)
                })
            
            return {
                "success": True,
                "results": formatted_results,
                "count": len(formatted_results)
            }
        except Exception as e:
            return {
                "success": False,
                "error": str(e)
            }
    
    def read_full_block(self, block_id: str) -> Dict[str, Any]:
        """
        块读取工具 - 获取完整记录
        
        Args:
            block_id: 记录ID
            
        Returns:
            完整的推荐记录
        """
        try:
            # 检查是否已读取
            if self.tracker.has_read(block_id):
                return {
                    "success": False,
                    "error": f"Block {block_id} already read, skip"
                }
            
            # 读取完整记录
            result = self.career_search.read_chunk(block_id)
            
            if result.get("success"):
                self.tracker.mark_read(block_id)
                return {
                    "success": True,
                    "record": result.get("record")
                }
            else:
                return {
                    "success": False,
                    "error": result.get("error", "Record not found")
                }
        except Exception as e:
            return {
                "success": False,
                "error": str(e)
            }
    
    def get_tools_schema(self) -> List[Dict[str, Any]]:
        """
        获取工具的JSON Schema
        
        Returns:
            工具schema列表
        """
        return [
            {
                "type": "function",
                "function": {
                    "name": "keyword_search",
                    "description": "根据关键词搜索相关的历史职业推荐记录，返回自我介绍片段（预览）。适合查找特定技能词、职位名等。",
                    "parameters": {
                        "type": "object",
                        "properties": {
                            "query": {"type": "string", "description": "搜索关键词，例如 '前端 全栈'"},
                            "limit": {"type": "integer", "description": "返回结果数量，默认3"}
                        },
                        "required": ["query"]
                    }
                }
            },
            {
                "type": "function",
                "function": {
                    "name": "semantic_search",
                    "description": "根据语义相似度搜索相关的历史职业推荐记录，返回自我介绍片段。适合模糊匹配或表达方式不同的查询。",
                    "parameters": {
                        "type": "object",
                        "properties": {
                            "query": {"type": "string", "description": "用户自我介绍全文或关键句"},
                            "limit": {"type": "integer", "description": "返回结果数量，默认3"}
                        },
                        "required": ["query"]
                    }
                }
            },
            {
                "type": "function",
                "function": {
                    "name": "read_full_block",
                    "description": "读取某个历史推荐记录的完整内容（包括自我介绍、推荐的三个职业、分析理由）。必须在看过片段后确认有用才调用。",
                    "parameters": {
                        "type": "object",
                        "properties": {
                            "block_id": {"type": "string", "description": "记录的ID"}
                        },
                        "required": ["block_id"]
                    }
                }
            }
        ]
    
    def agent_recommend(self, user_intro: str, max_steps: int = 5) -> Dict[str, Any]:
        """
        代理推理循环
        
        Args:
            user_intro: 用户自我介绍
            max_steps: 最大步数
            
        Returns:
            职业推荐结果
        """
        # 重置上下文追踪器
        self.tracker.reset()
        
        # 执行A-RAG搜索
        keyword_results = self.keyword_search(user_intro, limit=3)
        semantic_results = self.semantic_search(user_intro, limit=3)
        
        # 读取相关记录
        read_results = []
        if keyword_results.get("success") and keyword_results.get("results"):
            for result in keyword_results.get("results")[:2]:
                block_id = result.get("id")
                if block_id:
                    read_result = self.read_full_block(block_id)
                    if read_result.get("success"):
                        read_results.append(read_result.get("record"))
        
        # 生成个性化的推荐结果
        recommendations = self._generate_personalized_recommendations(user_intro)
        
        # 调用大模型进行进一步个性化推荐
        llm_recommendations = self._call_llm_for_recommendations(user_intro, read_results)
        
        # 合并推荐结果，优先使用大模型的推荐
        if llm_recommendations:
            recommendations = llm_recommendations
        
        # 返回结构化的 JSON 数据
        return {
            "recommendations": recommendations,
            "reference_cases": [
                {
                    "self_introduction": record.get("self_introduction", "")[:100] + "...",
                    "recommendations": [r.get("career", "") for r in record.get("recommendations", [])[:3]]
                }
                for record in read_results[:2]
            ]
        }
    
    def _call_llm_for_recommendations(self, user_intro: str, reference_cases: list) -> List[Dict[str, Any]]:
        """
        调用大模型生成个性化推荐
        
        Args:
            user_intro: 用户自我介绍
            reference_cases: 参考案例
            
        Returns:
            大模型生成的推荐结果
        """
        try:
            from ai_integration.langgraph.graphs.graph import create_chat_workflow
            from ai_integration.langgraph.utils.prompt_manager import get_career_categories, get_prompt
            
            # 获取职业分类信息
            career_info = get_career_categories()
            
            # 从数据库获取职业推荐提示词
            recommendation_prompt = get_prompt('recommendation')
            
            # 构建参考案例信息
            reference_info = ""
            if reference_cases:
                reference_info = "\n\n参考历史案例：\n"
                for i, case in enumerate(reference_cases[:2]):
                    intro = case.get("self_introduction", "")[:100]
                    careers = [r.get("career", "") for r in case.get("recommendations", [])[:3]]
                    reference_info += f"案例{i+1}：{intro}...\n推荐职业：{', '.join(careers)}\n"
            
            # 构建完整的提示词
            enhanced_prompt = "用户自我介绍：" + user_intro + reference_info + "\n\n" + career_info + "\n\n" + recommendation_prompt
            
            # 创建工作流并执行
            chat_workflow = create_chat_workflow()
            state = {
                "message": enhanced_prompt,
                "mode": "recommendation"
            }
            
            # 执行LangGraph工作流
            result = chat_workflow.invoke(state)
            
            # 提取响应内容
            response_content = result.get("response", "")
            
            # 解析推荐结果
            processed_recommendations = []
            try:
                import re
                # 尝试从JSON块中提取
                json_match = re.search(r'```json\s*([\s\S]*?)\s*```', response_content)
                if json_match:
                    clean_content = json_match.group(1).strip()
                    clean_content = clean_content.replace('{{', '{').replace('}}', '}')
                    parsed_content = json.loads(clean_content, strict=False)
                    
                    if isinstance(parsed_content, dict) and 'recommendations' in parsed_content:
                        processed_recommendations = parsed_content['recommendations']
                    elif isinstance(parsed_content, list):
                        processed_recommendations = parsed_content
            except Exception as e:
                # 如果JSON解析失败，尝试从文本中提取
                import re
                career_matches = re.findall(r'###\s*(.*?)\s*', response_content)
                for i, career in enumerate(career_matches[:3]):
                    career_name = career.strip()
                    if career_name:
                        processed_recommendations.append({
                            "career": career_name,
                            "matchScore": 90 - i*10,
                            "reason": "基于您的背景和技能推荐",
                            "skillsMatch": [],
                            "missingSkills": [],
                            "improvement": ""
                        })
            
            return processed_recommendations[:3]
        except Exception as e:
            import logging
            logging.error(f"Error calling LLM for recommendations: {str(e)}")
            # 如果大模型调用失败，返回空列表，使用默认推荐
            return []
    
    def _generate_personalized_recommendations(self, user_intro: str) -> List[Dict[str, Any]]:
        """
        根据用户自我介绍生成个性化推荐
        
        Args:
            user_intro: 用户自我介绍
            
        Returns:
            个性化推荐结果
        """
        # 简单的关键词匹配逻辑
        intro_lower = user_intro.lower()
        
        # 定义职业关键词映射
        career_keywords = {
            "前端工程师": ["前端", "web", "html", "css", "javascript", "react", "vue", "angular"],
            "后端工程师": ["后端", "java", "python", "php", "golang", "c++", "node.js", "数据库"],
            "全栈工程师": ["全栈", "前后端", "fullstack"],
            "测试工程师": ["测试", "qa", "quality", "自动化"],
            "数据分析师": ["数据", "分析", "statistics", "sql", "python", "pandas"],
            "运维工程师": ["运维", "devops", "linux", "服务器", "网络"],
            "算法工程师": ["算法", "机器学习", "深度学习", "ai", "data mining"],
            "UI/UX设计师": ["设计", "ui", "ux", "用户体验", "界面"]
        }
        
        # 计算每个职业的匹配度
        matches = []
        for career, keywords in career_keywords.items():
            score = 0
            for keyword in keywords:
                if keyword in intro_lower:
                    score += 1
            if score > 0:
                matches.append((career, score))
        
        # 按匹配度排序
        matches.sort(key=lambda x: x[1], reverse=True)
        
        # 生成推荐结果
        recommendations = []
        for i, (career, score) in enumerate(matches[:3], 1):
            match_score = min(85 + (i-1)*-5, 100)  # 85, 80, 75
            
            # 根据职业生成推荐理由
            reason = self._generate_reason(career, user_intro)
            
            # 生成技能匹配情况
            skills_match = self._extract_skills(user_intro)
            missing_skills = self._get_missing_skills(career)
            improvement = self._get_improvement(career)
            
            recommendations.append({
                "career": career,
                "matchScore": match_score,
                "reason": reason,
                "skillsMatch": skills_match,
                "missingSkills": missing_skills,
                "improvement": improvement
            })
        
        # 如果没有匹配的职业，返回默认推荐
        if not recommendations:
            recommendations = [
                {
                    "career": "前端工程师",
                    "matchScore": 85,
                    "reason": "基于您的自我介绍，您对前端开发有兴趣且具备相关技能。",
                    "skillsMatch": ["基本电脑操作"],
                    "missingSkills": ["HTML/CSS", "JavaScript", "框架库"],
                    "improvement": "学习前端基础技术，掌握至少一种前端框架"
                },
                {
                    "career": "全栈工程师",
                    "matchScore": 80,
                    "reason": "您的技能涵盖前后端，适合全栈开发岗位。",
                    "skillsMatch": ["基本电脑操作"],
                    "missingSkills": ["前端技术", "后端技术", "数据库"],
                    "improvement": "全面学习前后端技术栈"
                },
                {
                    "career": "UI/UX设计师",
                    "matchScore": 75,
                    "reason": "您对用户界面设计有兴趣，适合UI/UX设计岗位。",
                    "skillsMatch": ["基本电脑操作"],
                    "missingSkills": ["设计工具", "用户研究", "交互设计"],
                    "improvement": "学习设计工具和用户体验知识"
                }
            ]
        
        return recommendations
    
    def _generate_reason(self, career: str, user_intro: str) -> str:
        """生成推荐理由"""
        reasons = {
            "前端工程师": "基于您的自我介绍，您对前端开发有兴趣且具备相关技能。",
            "后端工程师": "基于您的自我介绍，您对后端开发有兴趣且具备相关技能。",
            "全栈工程师": "您的技能涵盖前后端，适合全栈开发岗位。",
            "测试工程师": "基于您的自我介绍，您对测试工作有兴趣且具备相关技能。",
            "数据分析师": "基于您的自我介绍，您对数据分析有兴趣且具备相关技能。",
            "运维工程师": "基于您的自我介绍，您对运维工作有兴趣且具备相关技能。",
            "算法工程师": "基于您的自我介绍，您对算法开发有兴趣且具备相关技能。",
            "UI/UX设计师": "您对用户界面设计有兴趣，适合UI/UX设计岗位。"
        }
        return reasons.get(career, "基于您的自我介绍，我们推荐这个职业。")
    
    def _extract_skills(self, user_intro: str) -> List[str]:
        """从自我介绍中提取技能"""
        skills = []
        intro_lower = user_intro.lower()
        
        # 常见技能关键词
        common_skills = [
            "基本电脑操作", "python", "java", "c++", "javascript", "html", "css",
            "数据库", "linux", "网络", "算法", "机器学习", "数据分析", "设计"
        ]
        
        for skill in common_skills:
            if skill in intro_lower:
                skills.append(skill)
        
        if not skills:
            skills = ["基本电脑操作"]
        
        return skills
    
    def _get_missing_skills(self, career: str) -> List[str]:
        """获取缺失的技能"""
        missing_skills = {
            "前端工程师": ["HTML/CSS", "JavaScript", "框架库"],
            "后端工程师": ["后端语言", "数据库", "API设计"],
            "全栈工程师": ["前端技术", "后端技术", "数据库"],
            "测试工程师": ["测试理论", "自动化测试", "测试工具"],
            "数据分析师": ["SQL", "统计分析", "数据可视化"],
            "运维工程师": ["Linux", "网络知识", "自动化工具"],
            "算法工程师": ["算法基础", "机器学习", "编程能力"],
            "UI/UX设计师": ["设计工具", "用户研究", "交互设计"]
        }
        return missing_skills.get(career, ["相关技能"])
    
    def _get_improvement(self, career: str) -> str:
        """获取提升建议"""
        improvements = {
            "前端工程师": "学习前端基础技术，掌握至少一种前端框架",
            "后端工程师": "深入学习后端技术，掌握数据库和API设计",
            "全栈工程师": "全面学习前后端技术栈",
            "测试工程师": "学习测试理论和自动化测试工具",
            "数据分析师": "学习SQL和数据可视化工具",
            "运维工程师": "学习Linux和自动化运维工具",
            "算法工程师": "深入学习算法和机器学习",
            "UI/UX设计师": "学习设计工具和用户体验知识"
        }
        return improvements.get(career, "学习相关技能和知识")


# 全局ARAG代理实例
arag_agent = ARAGAgent()


def get_arag_agent() -> ARAGAgent:
    """
    获取ARAG代理实例
    
    Returns:
        ARAGAgent实例
    """
    return arag_agent
