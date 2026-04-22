from langchain_core.tools import tool
from datetime import datetime

@tool
def get_time() -> str:
    """当需要查询时间时，调用此函数。返回格式为: [年-月-日 时:分:秒]"""
    return datetime.now().strftime('%Y-%m-%d %H:%M:%S')
