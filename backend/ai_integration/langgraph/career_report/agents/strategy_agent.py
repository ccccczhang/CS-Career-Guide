from langchain_core.messages import HumanMessage, SystemMessage

from ai_integration.langgraph.career_report.prompts.report_prompts import STRATEGY_AGENT_PROMPT
from ai_integration.langgraph.career_report.services.llm_provider import get_llm, parse_json_response


def run_strategy_agent(user_profile: dict, target_job: str) -> dict:
    llm = get_llm(temperature=0.4)
    payload = {
        "user_profile": user_profile,
        "target_job": target_job,
        "task": "output job strategy/risk/resume optimization/application rhythm",
    }
    response = llm.invoke([SystemMessage(content=STRATEGY_AGENT_PROMPT), HumanMessage(content=str(payload))])
    return parse_json_response(response.content)
