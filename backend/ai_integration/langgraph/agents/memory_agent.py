"""
长期记忆agent模块
负责提取和存储对话中的关键信息
"""

import logging
import re
from ai_integration.models import LongTermMemory, AIConversation, AIChatPair

logger = logging.getLogger(__name__)

class MemoryAgent:
    """长期记忆agent，用于提取和存储对话中的关键信息"""
    
    def __init__(self):
        """初始化MemoryAgent"""
        self.keyword_patterns = {
            'career_goal': [
                r'目标.*职业',
                r'想成为.*',
                r'希望.*职业',
                r'职业.*规划',
                r'未来.*发展',
                r'理想.*工作',
                r'职业.*目标'
            ],
            'skills': [
                r'擅长.*',
                r'精通.*',
                r'掌握.*',
                r'熟悉.*',
                r'技能.*',
                r'会.*',
                r'能.*'
            ],
            'interview_feedback': [
                r'面试.*反馈',
                r'面试.*评价',
                r'面试.*表现',
                r'面试.*建议',
                r'面试官.*说',
                r'面试.*问题'
            ],
            'preferences': [
                r'喜欢.*',
                r'偏好.*',
                r'倾向.*',
                r'希望.*',
                r'不喜欢.*',
                r'讨厌.*',
                r'偏好.*环境',
                r'偏好.*公司'
            ]
        }
    
    def extract_key_info(self, text, category=None):
        """
        从文本中提取关键信息
        
        Args:
            text: 要分析的文本
            category: 要提取的信息类别，如果为None则尝试所有类别
            
        Returns:
            dict: 提取的关键信息，格式为 {category: [info1, info2, ...]}
        """
        extracted_info = {}
        
        if category and category in self.keyword_patterns:
            categories = [category]
        else:
            categories = self.keyword_patterns.keys()
        
        for cat in categories:
            patterns = self.keyword_patterns[cat]
            info_list = []
            
            for pattern in patterns:
                matches = re.findall(pattern, text, re.IGNORECASE)
                if matches:
                    # 提取包含匹配关键词的完整句子
                    sentences = re.split(r'[。！？；：]', text)
                    for sentence in sentences:
                        for match in matches:
                            if match in sentence:
                                info_list.append(sentence.strip())
            
            if info_list:
                # 去重
                info_list = list(set(info_list))
                extracted_info[cat] = info_list
        
        return extracted_info
    
    def process_conversation(self, conversation_id):
        """
        处理对话，提取并存储关键信息
        
        Args:
            conversation_id: 对话ID
        """
        try:
            # 获取对话
            conversation = AIConversation.objects.get(session_id=conversation_id)
            
            # 获取对话中的所有问答对
            chat_pairs = AIChatPair.objects.filter(conversation=conversation).order_by('input_time')
            
            # 合并所有对话内容
            full_conversation = ""
            for pair in chat_pairs:
                full_conversation += f"用户：{pair.user_input}\n"
                full_conversation += f"AI：{pair.ai_output}\n"
            
            # 提取关键信息
            extracted_info = self.extract_key_info(full_conversation)
            
            # 存储关键信息到长期记忆
            for category, info_list in extracted_info.items():
                for info in info_list:
                    # 检查是否已存在相同的记忆
                    existing_memory = LongTermMemory.objects.filter(
                        user=conversation.user,
                        category=category,
                        value=info,
                        conversation=conversation
                    ).first()
                    
                    if not existing_memory:
                        # 创建新的长期记忆
                        LongTermMemory.objects.create(
                            user=conversation.user,
                            key=f"{category}_{conversation_id}",
                            value=info,
                            category=category,
                            conversation=conversation
                        )
                        logger.info(f"Stored long-term memory: {category} - {info}")
            
            return True
        except Exception as e:
            logger.error(f"Error processing conversation: {str(e)}")
            return False
    
    def get_long_term_memory(self, user=None, categories=None, limit=10):
        """
        获取长期记忆
        
        Args:
            user: 用户对象
            categories: 要获取的记忆类别列表
            limit: 返回的记忆数量限制
            
        Returns:
            list: 长期记忆列表
        """
        try:
            query = LongTermMemory.objects
            
            if user:
                query = query.filter(user=user)
            
            if categories:
                query = query.filter(category__in=categories)
            
            # 按更新时间排序，获取最新的记忆
            memories = query.order_by('-updated_at')[:limit]
            
            # 转换为字典列表
            memory_list = []
            for memory in memories:
                memory_list.append({
                    'id': memory.id,
                    'key': memory.key,
                    'value': memory.value,
                    'category': memory.category,
                    'created_at': memory.created_at.isoformat(),
                    'updated_at': memory.updated_at.isoformat()
                })
            
            return memory_list
        except Exception as e:
            logger.error(f"Error getting long-term memory: {str(e)}")
            return []
    
    def update_memory(self, memory_id, new_value):
        """
        更新长期记忆
        
        Args:
            memory_id: 记忆ID
            new_value: 新的记忆值
            
        Returns:
            bool: 更新是否成功
        """
        try:
            memory = LongTermMemory.objects.get(id=memory_id)
            memory.value = new_value
            memory.save()
            logger.info(f"Updated long-term memory: {memory.id}")
            return True
        except Exception as e:
            logger.error(f"Error updating memory: {str(e)}")
            return False
    
    def delete_memory(self, memory_id):
        """
        删除长期记忆
        
        Args:
            memory_id: 记忆ID
            
        Returns:
            bool: 删除是否成功
        """
        try:
            memory = LongTermMemory.objects.get(id=memory_id)
            memory.delete()
            logger.info(f"Deleted long-term memory: {memory_id}")
            return True
        except Exception as e:
            logger.error(f"Error deleting memory: {str(e)}")
            return False

# 创建全局MemoryAgent实例
memory_agent = MemoryAgent()
