from django.apps import AppConfig


class AiIntegrationConfig(AppConfig):
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'ai_integration'
    
    def ready(self):
        from ai_integration.langgraph.utils.prompt_manager import initialize_prompts
        initialize_prompts()
