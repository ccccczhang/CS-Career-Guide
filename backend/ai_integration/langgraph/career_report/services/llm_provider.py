import json
import os
import re
from typing import Any, Dict

from langchain_openai import ChatOpenAI

DEFAULT_MODEL = os.getenv("career_report_model", "qwen-turbo-2025-07-15")


def get_llm(temperature: float = 0.4) -> ChatOpenAI:
    api_key = os.getenv("api_key")
    base_url = os.getenv("base_url")
    if not api_key or not base_url:
        raise ValueError("Missing api_key/base_url in environment variables")
    return ChatOpenAI(
        model=DEFAULT_MODEL,
        temperature=temperature,
        api_key=api_key,
        base_url=base_url,
        timeout=90,
    )


def parse_json_response(raw_text: str) -> Dict[str, Any]:
    try:
        return json.loads(raw_text)
    except json.JSONDecodeError:
        pass

    fenced = re.search(r"```json\s*([\s\S]*?)\s*```", raw_text)
    if fenced:
        try:
            return json.loads(fenced.group(1).strip())
        except json.JSONDecodeError:
            pass

    start = raw_text.find("{")
    end = raw_text.rfind("}")
    if start != -1 and end != -1 and end > start:
        return json.loads(raw_text[start : end + 1])

    raise ValueError("Model response does not contain valid JSON")
