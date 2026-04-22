import os
import sys
from typing import TypedDict, Any, Optional, List, Dict, Sequence, Annotated

# 修复 langgraph 导入问题
# 在 langgraph 1.x 中，StateGraph、START、END 需要从 langgraph.graph 导入
# 通过临时修改 sys.path 来确保导入的是安装的 langgraph 库而不是项目中的目录

# 保存原始路径
original_path = sys.path.copy()
try:
    # 移除包含 ai_integration/langgraph 的路径
    sys.path = [p for p in sys.path if p and 'ai_integration/langgraph' not in p.replace('/', os.sep)]
    
    # 从 langgraph.graph 导入（langgraph 1.x 的正确导入方式）
    from langgraph.graph import StateGraph, START, END, add_messages
    
finally:
    # 恢复路径
    sys.path = original_path

from langchain_core.messages import BaseMessage
from ai_integration.langgraph.agents import create_career_agent, create_chat_agent, create_interview_agent, create_recommendation_agent
from ai_integration.langgraph.tools.time_tool import get_time
from ai_integration.langgraph.tools import keyword_search_tool, semantic_search_tool, read_chunk_tool
import logging

logger = logging.getLogger(__name__)

# 工具调用最大次数限制
MAX_TOOL_CALLS = 15  # 增加限制以支持完整的推荐流程

class AgentState(TypedDict):
    messages: Annotated[Sequence[BaseMessage], add_messages]
    mode: str
    response: str
    tool_calls: Optional[List[Dict]] = None
    tool_results: Optional[List[Dict]] = None
    last_agent: Optional[str] = None
    tool_call_count: int = 0  # 新增：工具调用计数器

def create_chat_workflow():
    """创建聊天工作流"""
    graph = StateGraph(AgentState)
    
    def chat_node(state: AgentState) -> dict[str, Any]:
        try:
            logger.info("Entering chat_node for routing")
            return state
        except Exception as e:
            logger.error(f"Error in chat_node: {str(e)}")
            return state
    
    def career_node(state: AgentState) -> dict[str, Any]:
        try:
            logger.info("Entering career_node")
            agent_chain = create_career_agent()
            logger.info("Created career agent chain")
            
            result = agent_chain.invoke({"messages": state["messages"]})
            logger.info(f"Career agent result: {result}")
            
            # 检查工具调用次数限制
            tool_call_count = state.get("tool_call_count", 0)
            logger.info(f"Current tool call count: {tool_call_count}/{MAX_TOOL_CALLS}")
            
            if hasattr(result, "tool_calls") and result.tool_calls:
                # 检查是否超过工具调用次数限制
                if tool_call_count >= MAX_TOOL_CALLS:
                    logger.warning(f"Tool call limit reached ({MAX_TOOL_CALLS}), ignoring tool calls")
                    # 超过限制，直接返回响应而不调用工具
                    if hasattr(result, "content") and result.content:
                        response = result.content
                    else:
                        response = "已达到最大工具调用次数限制，将直接为您总结。"
                    logger.info(f"Career node response (limited): {response[:50]}...")
                    return {"response": response}
                
                logger.info(f"Tool calls detected in career mode: {result.tool_calls}")
                tool_calls = []
                for tool_call in result.tool_calls:
                    if isinstance(tool_call, dict):
                        tool_calls.append({
                            "name": tool_call.get("name"),
                            "args": tool_call.get("args", {}),
                            "id": tool_call.get("id")
                        })
                    elif hasattr(tool_call, "name"):
                        tool_calls.append({
                            "name": tool_call.name,
                            "args": tool_call.args,
                            "id": tool_call.id
                        })
                logger.info(f"Extracted tool calls: {tool_calls}")
                return {"tool_calls": tool_calls, "last_agent": "career"}
            else:
                if hasattr(result, "content") and result.content:
                    response = result.content
                elif isinstance(result, dict) and "content" in result:
                    response = result["content"]
                else:
                    response = "我是您的专属职业规划师，专注于帮助您制定职业发展计划。"
                logger.info(f"Career node response: {response}")
                return {"response": response}
        except Exception as e:
            logger.error(f"Error in career_node: {str(e)}")
            return {"response": "抱歉，我暂时无法回答你的问题，请稍后再试。"}
    
    def interview_node(state: AgentState) -> dict[str, Any]:
        try:
            logger.info("Entering interview_node")
            agent_chain = create_interview_agent()
            logger.info("Created interview agent chain")
            
            result = agent_chain.invoke({"messages": state["messages"]})
            logger.info(f"Interview agent result: {result}")
            
            # 检查工具调用次数限制
            tool_call_count = state.get("tool_call_count", 0)
            logger.info(f"Current tool call count: {tool_call_count}/{MAX_TOOL_CALLS}")
            
            if hasattr(result, "tool_calls") and result.tool_calls:
                # 检查是否超过工具调用次数限制
                if tool_call_count >= MAX_TOOL_CALLS:
                    logger.warning(f"Tool call limit reached ({MAX_TOOL_CALLS}), ignoring tool calls")
                    if hasattr(result, "content") and result.content:
                        response = result.content
                    else:
                        response = "已达到最大工具调用次数限制，将直接为您总结。"
                    logger.info(f"Interview node response (limited): {response[:50]}...")
                    return {"response": response}
                
                logger.info(f"Tool calls detected in interview mode: {result.tool_calls}")
                tool_calls = []
                for tool_call in result.tool_calls:
                    if isinstance(tool_call, dict):
                        tool_calls.append({
                            "name": tool_call.get("name"),
                            "args": tool_call.get("args", {}),
                            "id": tool_call.get("id")
                        })
                    elif hasattr(tool_call, "name"):
                        tool_calls.append({
                            "name": tool_call.name,
                            "args": tool_call.args,
                            "id": tool_call.id
                        })
                logger.info(f"Extracted tool calls: {tool_calls}")
                return {"tool_calls": tool_calls, "last_agent": "interview"}
            else:
                if hasattr(result, "content") and result.content:
                    response = result.content
                elif isinstance(result, dict) and "content" in result:
                    response = result["content"]
                else:
                    response = "您好！我是您的面试官，今天我们将进行一场面试。"
                logger.info(f"Interview node response: {response}")
                return {"response": response}
        except Exception as e:
            logger.error(f"Error in interview_node: {str(e)}")
            return {"response": "抱歉，我暂时无法回答你的问题，请稍后再试。"}
    
    def recommendation_node(state: AgentState) -> dict[str, Any]:
        try:
            logger.info("Entering recommendation_node")
            agent_chain = create_recommendation_agent()
            logger.info("Created recommendation agent chain")
            
            result = agent_chain.invoke({"messages": state["messages"]})
            logger.info(f"Recommendation agent result: {result}")
            
            # 检查工具调用次数限制
            tool_call_count = state.get("tool_call_count", 0)
            logger.info(f"Current tool call count: {tool_call_count}/{MAX_TOOL_CALLS}")
            
            if hasattr(result, "tool_calls") and result.tool_calls:
                # 检查是否超过工具调用次数限制
                if tool_call_count >= MAX_TOOL_CALLS:
                    logger.warning(f"Tool call limit reached ({MAX_TOOL_CALLS}), ignoring tool calls")
                    if hasattr(result, "content") and result.content:
                        response = result.content
                    else:
                        response = "已达到最大工具调用次数限制，将直接为您总结。"
                    logger.info(f"Recommendation node response (limited): {response[:50]}...")
                    return {"response": response}
                
                logger.info(f"Tool calls detected in recommendation mode: {result.tool_calls}")
                tool_calls = []
                for tool_call in result.tool_calls:
                    if isinstance(tool_call, dict):
                        tool_calls.append({
                            "name": tool_call.get("name"),
                            "args": tool_call.get("args", {}),
                            "id": tool_call.get("id")
                        })
                    elif hasattr(tool_call, "name"):
                        tool_calls.append({
                            "name": tool_call.name,
                            "args": tool_call.args,
                            "id": tool_call.id
                        })
                logger.info(f"Extracted tool calls: {tool_calls}")
                return {"tool_calls": tool_calls, "last_agent": "recommendation"}
            else:
                if hasattr(result, "content") and result.content:
                    response = result.content
                elif isinstance(result, dict) and "content" in result:
                    response = result["content"]
                else:
                    response = "我是您的职业推荐专家，专注于根据您的背景和需求推荐合适的职业。"
                logger.info(f"Recommendation node response: {response}")
                return {"response": response}
        except Exception as e:
            logger.error(f"Error in recommendation_node: {str(e)}")
            import traceback
            logger.error(traceback.format_exc())
            return {"response": f"抱歉，我暂时无法回答你的问题，请稍后再试。详细错误: {str(e)}"}
    
    def llm_node(state: AgentState) -> dict[str, Any]:
        try:
            logger.info("Entering llm_node")
            agent_chain = create_chat_agent()
            logger.info("Created chat agent chain")
            
            result = agent_chain.invoke({"messages": state["messages"]})
            logger.info(f"LLM result: {result}")
            
            # 检查工具调用次数限制
            tool_call_count = state.get("tool_call_count", 0)
            logger.info(f"Current tool call count: {tool_call_count}/{MAX_TOOL_CALLS}")
            
            if hasattr(result, "tool_calls") and result.tool_calls:
                # 检查是否超过工具调用次数限制
                if tool_call_count >= MAX_TOOL_CALLS:
                    logger.warning(f"Tool call limit reached ({MAX_TOOL_CALLS}), ignoring tool calls")
                    if hasattr(result, "content") and result.content:
                        response = result.content
                    else:
                        response = "已达到最大工具调用次数限制，将直接为您总结。"
                    logger.info(f"LLM node response (limited): {response[:50]}...")
                    return {"response": response}
                
                logger.info(f"Tool calls detected: {result.tool_calls}")
                tool_calls = []
                for tool_call in result.tool_calls:
                    logger.info(f"Tool call type: {type(tool_call)}")
                    if isinstance(tool_call, dict):
                        tool_calls.append({
                            "name": tool_call.get("name"),
                            "args": tool_call.get("args", {}),
                            "id": tool_call.get("id")
                        })
                    elif hasattr(tool_call, "name"):
                        tool_calls.append({
                            "name": tool_call.name,
                            "args": tool_call.args,
                            "id": tool_call.id
                        })
                logger.info(f"Extracted tool calls: {tool_calls}")
                return {"tool_calls": tool_calls, "last_agent": "llm"}
            else:
                if hasattr(result, "content") and result.content:
                    response = result.content
                elif isinstance(result, dict) and "content" in result:
                    response = result["content"]
                else:
                    response = "我是AI智能助手，专注于帮助你解决职业发展相关的问题。"
                logger.info(f"LLM node response: {response}")
                return {"response": response}
        except Exception as e:
            logger.error(f"Error in llm_node: {str(e)}")
            return {"response": "抱歉，我暂时无法回答你的问题，请稍后再试。"}
    
    def streaming_llm_node(state: AgentState) -> dict[str, Any]:
        try:
            logger.info("Entering streaming_llm_node")
            agent_chain = create_chat_agent()
            logger.info("Created chat agent chain for streaming")
            
            chunks = []
            for chunk in agent_chain.stream({"messages": state["messages"]}):
                logger.info(f"Received chunk: {chunk}")
                chunks.append(chunk)
            
            response = ""
            for chunk in chunks:
                if hasattr(chunk, "content") and chunk.content:
                    response += chunk.content
                elif isinstance(chunk, dict) and "content" in chunk:
                    response += chunk["content"]
            
            logger.info(f"Streaming LLM response: {response}")
            return {"response": response}
        except Exception as e:
            logger.error(f"Error in streaming_llm_node: {str(e)}")
            return {"response": "抱歉，我暂时无法回答你的问题，请稍后再试。"}
    
    def tool_node(state: AgentState) -> dict[str, Any]:
        try:
            logger.info("Entering tool_node")
            tool_calls = state.get("tool_calls", [])
            logger.info(f"Processing tool calls: {tool_calls}")
            
            # 调试输出：打印工具调用信息
            print("\n" + "="*60)
            print("🔧 [TOOL CALL DEBUG] 工具调用信息")
            print("="*60)
            print(f"工具调用数量: {len(tool_calls)}")
            
            tool_results = []
            for i, tool_call in enumerate(tool_calls):
                tool_name = tool_call.get("name")
                tool_args = tool_call.get("args", {})
                tool_id = tool_call.get("id")
                
                logger.info(f"Processing tool: {tool_name} with args: {tool_args}")
                
                # 调试输出：打印每个工具调用
                print(f"\n工具 {i+1}: {tool_name}")
                print(f"工具ID: {tool_id}")
                print(f"调用参数: {tool_args}")
                print("-" * 40)
                
                try:
                    if tool_name == "get_time":
                        from datetime import datetime
                        tool_result = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
                        tool_results.append({
                            "tool_call_id": tool_id,
                            "output": tool_result
                        })
                        logger.info(f"Tool get_time executed, result: {tool_result}")
                        print(f"✅ 执行成功")
                        print(f"结果: {tool_result}")
                    
                    elif tool_name == "keyword_search_tool":
                        query = tool_args.get("query", "")
                        threshold = tool_args.get("threshold", 0.7)
                        top_k = tool_args.get("top_k", 5)
                        print(f"🔍 正在执行关键词搜索...")
                        print(f"   查询词: {query}")
                        print(f"   阈值: {threshold}")
                        print(f"   返回数量: {top_k}")
                        result = keyword_search_tool.invoke({"query": query, "threshold": threshold, "top_k": top_k})
                        tool_results.append({
                            "tool_call_id": tool_id,
                            "output": result
                        })
                        logger.info(f"Tool keyword_search_tool executed, result: {result}")
                        print(f"✅ 关键词搜索完成")
                        if result.get("success"):
                            print(f"   找到 {result.get('count', 0)} 条匹配记录")
                        else:
                            print(f"   搜索失败: {result.get('error', '未知错误')}")
                    
                    elif tool_name == "semantic_search_tool":
                        query = tool_args.get("query", "")
                        threshold = tool_args.get("threshold", 0.5)
                        top_k = tool_args.get("top_k", 5)
                        print(f"🔍 正在执行语义搜索...")
                        print(f"   查询词: {query}")
                        print(f"   阈值: {threshold}")
                        print(f"   返回数量: {top_k}")
                        result = semantic_search_tool.invoke({"query": query, "threshold": threshold, "top_k": top_k})
                        tool_results.append({
                            "tool_call_id": tool_id,
                            "output": result
                        })
                        logger.info(f"Tool semantic_search_tool executed, result: {result}")
                        print(f"✅ 语义搜索完成")
                        if result.get("success"):
                            print(f"   找到 {result.get('count', 0)} 条匹配记录")
                        else:
                            print(f"   搜索失败: {result.get('error', '未知错误')}")
                    
                    elif tool_name == "read_chunk_tool":
                        chunk_id = tool_args.get("chunk_id", "")
                        print(f"📖 正在读取块数据...")
                        print(f"   块ID: {chunk_id}")
                        result = read_chunk_tool.invoke({"chunk_id": chunk_id})
                        tool_results.append({
                            "tool_call_id": tool_id,
                            "output": result
                        })
                        logger.info(f"Tool read_chunk_tool executed, result: {result}")
                        print(f"✅ 块数据读取完成")
                    
                    else:
                        logger.warning(f"Unknown tool: {tool_name}")
                        tool_results.append({
                            "tool_call_id": tool_id,
                            "output": f"未知工具：{tool_name}"
                        })
                        print(f"❌ 未知工具: {tool_name}")
                
                except Exception as e:
                    logger.error(f"Error executing tool {tool_name}: {str(e)}")
                    tool_results.append({
                        "tool_call_id": tool_id,
                        "output": f"工具执行失败：{str(e)}"
                    })
                    print(f"❌ 工具执行失败: {str(e)}")
            
            print("\n" + "="*60)
            print("🔧 [TOOL CALL DEBUG] 工具调用完成")
            print("="*60)
            logger.info(f"Tool execution completed, results: {tool_results}")
            
            # 增加工具调用计数器
            tool_call_count = state.get("tool_call_count", 0)
            new_count = tool_call_count + len(tool_calls)
            logger.info(f"Tool call count updated: {tool_call_count} -> {new_count}")
            
            # 将工具结果添加到消息历史中，让代理能够看到工具执行结果
            from langchain_core.messages import ToolMessage
            
            new_messages = []
            for result in tool_results:
                tool_call_id = result.get("tool_call_id", "")
                output = result.get("output", "")
                
                # 将结果转换为字符串
                if isinstance(output, dict):
                    output_str = str(output)
                else:
                    output_str = str(output)
                
                # 创建 ToolMessage
                tool_msg = ToolMessage(
                    content=output_str,
                    tool_call_id=tool_call_id
                )
                new_messages.append(tool_msg)
            
            logger.info(f"Added {len(new_messages)} tool messages to history")
            
            return {"tool_results": tool_results, "tool_calls": None, "tool_call_count": new_count, "messages": new_messages}
        except Exception as e:
            logger.error(f"Error in tool_node: {str(e)}")
            return {"response": "工具调用失败"}
    
    graph.add_node("chat", chat_node)
    graph.add_node("career", career_node)
    graph.add_node("interview", interview_node)
    graph.add_node("recommendation", recommendation_node)
    graph.add_node("llm", llm_node)
    graph.add_node("tool", tool_node)
    
    graph.set_entry_point("chat")
    
    def route_message(state: AgentState) -> str:
        if state.get("messages") and len(state["messages"]) > 0:
            last_message = state["messages"][-1]
            message = getattr(last_message, "content", "").lower()
        else:
            message = ""
        mode = state["mode"].lower()
        
        if mode == "recommendation" or "推荐" in message:
            return "recommendation"
        elif mode == "career" or mode == "career_consultant" or "职业" in message or "规划" in message:
            return "career"
        elif mode == "interview" or "面试" in message:
            return "interview"
        else:
            return "llm"
    
    def route_llm_result(state: AgentState) -> str:
        if state.get("tool_calls"):
            return "tool"
        else:
            return END
    
    def route_career_result(state: AgentState) -> str:
        if state.get("tool_calls"):
            return "tool"
        else:
            return END
    
    def route_interview_result(state: AgentState) -> str:
        if state.get("tool_calls"):
            return "tool"
        else:
            return END
    
    def route_recommendation_result(state: AgentState) -> str:
        if state.get("tool_calls"):
            return "tool"
        else:
            return END
    
    def route_tool_result(state: AgentState) -> str:
        last_agent = state.get("last_agent", "llm")
        logger.info(f"Routing tool result back to: {last_agent}")
        return last_agent
    
    graph.add_conditional_edges("chat", route_message)
    graph.add_conditional_edges("llm", route_llm_result)
    graph.add_conditional_edges("career", route_career_result)
    graph.add_conditional_edges("interview", route_interview_result)
    graph.add_conditional_edges("recommendation", route_recommendation_result)
    graph.add_conditional_edges("tool", route_tool_result)
    
    return graph.compile()