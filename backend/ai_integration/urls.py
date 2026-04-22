from django.urls import path
from . import langgraph_views
from . import optimized_views
from django.views.generic import TemplateView

urlpatterns = [
    # 职业推荐Demo页面（带localStorage缓存）
    path('demo/career-recommendation/', TemplateView.as_view(template_name='career_recommendation_demo.html'), name='career_recommendation_demo'),
    path('llm/chat/', langgraph_views.llm_chat, name='llm_chat'),
    # 使用优化后的职业推荐API
    path('llm/career/recommendation/', optimized_views.optimized_career_recommendation, name='career_recommendation'),
    path('llm/career/plan/', langgraph_views.career_plan_generation, name='career_plan_generation'),
    # Chunk Read API
    path('llm/career/chunk/<str:record_id>/', optimized_views.read_career_chunk, name='read_career_chunk'),
    path('llm/career/chunks/batch/', optimized_views.read_career_chunks_batch, name='read_career_chunks_batch'),
    # A-RAG职业推荐API
    path('llm/career/arag/', optimized_views.arag_career_recommendation, name='arag_career_recommendation'),
    path('llm/save-chat/', langgraph_views.save_chat_record, name='save_chat_record'),
    path('records/save-evaluation/', langgraph_views.save_career_evaluation, name='save_career_evaluation'),
    path('records/save-plan/', langgraph_views.save_career_plan, name='save_career_plan'),
    path('records/save-recommendation/', langgraph_views.save_career_recommendation, name='save_career_recommendation'),
    path('records/user/', langgraph_views.get_user_ai_records, name='get_user_ai_records'),
    path('records/chat/', langgraph_views.get_chat_records, name='get_chat_records'),
    path('records/evaluation/', langgraph_views.get_evaluation_records, name='get_evaluation_records'),
    path('records/plan/', langgraph_views.get_plan_reports, name='get_plan_reports'),
    path('records/add-tag/', langgraph_views.add_tag_to_record, name='add_tag_to_record'),
    path('records/delete/', langgraph_views.delete_record, name='delete_record'),
    # 记忆机制API接口
    path('llm/chat/history/', langgraph_views.get_chat_history, name='get_chat_history'),
    path('llm/memory/long-term/', langgraph_views.get_long_term_memory, name='get_long_term_memory'),
    path('llm/memory/update/', langgraph_views.update_memory, name='update_memory'),
    path('llm/memory/delete/', langgraph_views.delete_memory, name='delete_memory'),
    # 健康检查端点
    path('health-check/', optimized_views.health_check, name='health_check'),
    # 学长学姐建议API
    path('senior-advice/', optimized_views.senior_advice_list, name='senior_advice_list'),
    path('senior-advice/approve/<int:advice_id>/', optimized_views.approve_senior_advice, name='approve_senior_advice'),
    
    # 语音生成简历API
    path('voice-to-resume/', optimized_views.voice_to_resume, name='voice_to_resume'),
    
    # 面试复盘API
    path('records/interview-reviews/', langgraph_views.get_user_interview_reviews, name='get_user_interview_reviews'),
]
