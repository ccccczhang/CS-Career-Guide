import json
import logging
import os
from typing import Any, Dict, Optional

from ai_integration.langgraph.career_report.agents.planner import create_planner_graph
from ai_integration.models import AIUsageRecord, CareerPlanReport

logger = logging.getLogger(__name__)


def _safe_session_id() -> str:
    return f"career_report_{os.urandom(8).hex()}"


def build_user_profile_from_user(user) -> Dict[str, Any]:
    skills = []
    if getattr(user, "skills", ""):
        skills.extend([s.strip() for s in str(user.skills).split(",") if s.strip()])
    if getattr(user, "other_skills", ""):
        skills.extend([s.strip() for s in str(user.other_skills).split(",") if s.strip()])

    return {
        "name": getattr(user, "name", "") or getattr(user, "username", ""),
        "major": getattr(user, "major", ""),
        "education": getattr(user, "education", ""),
        "skills": skills,
        "personality": getattr(user, "profile", ""),
        "graduation_year": "",
        "school": getattr(user, "school", ""),
        "grade": getattr(user, "grade", ""),
        "career_goal": getattr(user, "career_goal", ""),
        "self_introduction": getattr(user, "self_introduction", ""),
    }


def generate_career_report(user_profile: Dict[str, Any], target_job: str) -> Dict[str, Any]:
    workflow = create_planner_graph()
    state = {
        "user_profile": user_profile,
        "target_job": target_job,
        "partials": {},
        "report": {},
        "errors": {},
    }
    result = workflow.invoke(state)
    report = result.get("report", {})
    if result.get("errors"):
        report["debug_errors"] = result["errors"]
    return report


def save_career_report(report: Dict[str, Any], user=None) -> str:
    session_id = _safe_session_id()
    radar_data = {"source": "career_report_v2", "summary": report.get("summary", {})}
    CareerPlanReport.objects.create(
        user=user,
        session_id=session_id,
        report_content=json.dumps(report, ensure_ascii=False),
        radar_data=radar_data,
    )
    try:
        AIUsageRecord.objects.create(user=user, usage_type="plan", related_id=session_id)
    except Exception as exc:
        logger.warning("Failed to create usage record: %s", exc)
    return session_id


def resolve_profile_from_request(request, request_profile: Optional[Dict[str, Any]]) -> Dict[str, Any]:
    if request_profile:
        return request_profile
    if request.user and request.user.is_authenticated:
        return build_user_profile_from_user(request.user)
    return {}
