import requests
import time
import json

BASE_URL = "http://localhost:8000/api"

def test_auth_login_success():
    """TC-AUTH-001: 正确用户登录"""
    print("🔹 TC-AUTH-001: 正确用户登录")
    start = time.time()
    try:
        response = requests.post(
            f"{BASE_URL}/users/auth/login/",
            json={"username": "admin", "password": "123456"},
            timeout=30
        )
        elapsed = time.time() - start
        if response.status_code == 200 and "token" in response.json():
            print(f"   ✅ 通过 - 响应时间: {elapsed:.2f}秒")
            return {"status": "PASS", "response_time": elapsed, "token": response.json().get("token")}
        else:
            print(f"   ❌ 失败 - 状态码: {response.status_code}, 响应: {response.text[:100]}")
            return {"status": "FAIL", "response_time": elapsed, "error": response.text}
    except Exception as e:
        elapsed = time.time() - start
        print(f"   ❌ 失败 - 异常: {e}")
        return {"status": "FAIL", "response_time": elapsed, "error": str(e)}

def test_auth_login_failure():
    """TC-AUTH-002: 错误密码登录"""
    print("🔹 TC-AUTH-002: 错误密码登录")
    start = time.time()
    try:
        response = requests.post(
            f"{BASE_URL}/users/auth/login/",
            json={"username": "admin", "password": "wrong"},
            timeout=30
        )
        elapsed = time.time() - start
        if response.status_code == 400:
            print(f"   ✅ 通过 - 响应时间: {elapsed:.2f}秒")
            return {"status": "PASS", "response_time": elapsed}
        else:
            print(f"   ❌ 失败 - 状态码: {response.status_code}")
            return {"status": "FAIL", "response_time": elapsed}
    except Exception as e:
        elapsed = time.time() - start
        print(f"   ❌ 失败 - 异常: {e}")
        return {"status": "FAIL", "response_time": elapsed, "error": str(e)}

def test_career_recommendation():
    """TC-REC-001: 完整自我介绍推荐"""
    print("🔹 TC-REC-001: 完整自我介绍推荐")
    start = time.time()
    try:
        self_intro = """姓名: 张三
学校: 长沙理工大学
专业: 计算机科学
年级: 大三
性别: 男
技能: Python开发、数据结构、数据库设计
职业期望: 软件工程师"""
        
        response = requests.post(
            f"{BASE_URL}/ai/llm/career/recommendation/",
            json={"self_introduction": self_intro},
            timeout=120
        )
        elapsed = time.time() - start
        data = response.json()
        
        if response.status_code == 200 and data.get("success") and len(data.get("recommendations", [])) >= 1:
            print(f"   ✅ 通过 - 响应时间: {elapsed:.2f}秒")
            print(f"   推荐数量: {len(data.get('recommendations', []))}")
            return {"status": "PASS", "response_time": elapsed, "recommendations": len(data.get("recommendations", []))}
        else:
            print(f"   ❌ 失败 - 状态码: {response.status_code}, 响应: {data}")
            return {"status": "FAIL", "response_time": elapsed, "error": str(data)}
    except Exception as e:
        elapsed = time.time() - start
        print(f"   ❌ 失败 - 异常: {e}")
        return {"status": "FAIL", "response_time": elapsed, "error": str(e)}

def test_career_recommendation_empty():
    """TC-REC-002: 空自我介绍"""
    print("🔹 TC-REC-002: 空自我介绍")
    start = time.time()
    try:
        response = requests.post(
            f"{BASE_URL}/ai/llm/career/recommendation/",
            json={"self_introduction": ""},
            timeout=30
        )
        elapsed = time.time() - start
        if response.status_code == 400:
            print(f"   ✅ 通过 - 响应时间: {elapsed:.2f}秒")
            return {"status": "PASS", "response_time": elapsed}
        else:
            print(f"   ❌ 失败 - 状态码: {response.status_code}")
            return {"status": "FAIL", "response_time": elapsed}
    except Exception as e:
        elapsed = time.time() - start
        print(f"   ❌ 失败 - 异常: {e}")
        return {"status": "FAIL", "response_time": elapsed, "error": str(e)}

def test_health_check():
    """TC-SYS-001: 健康检查"""
    print("🔹 TC-SYS-001: 健康检查")
    start = time.time()
    try:
        response = requests.get(f"{BASE_URL}/ai/health-check/", timeout=30)
        elapsed = time.time() - start
        if response.status_code == 200 and response.json().get("status") == "healthy":
            print(f"   ✅ 通过 - 响应时间: {elapsed:.2f}秒")
            return {"status": "PASS", "response_time": elapsed}
        else:
            print(f"   ❌ 失败 - 状态码: {response.status_code}")
            return {"status": "FAIL", "response_time": elapsed}
    except Exception as e:
        elapsed = time.time() - start
        print(f"   ❌ 失败 - 异常: {e}")
        return {"status": "FAIL", "response_time": elapsed, "error": str(e)}

def run_all_tests():
    print("="*70)
    print("🧪 CS职业指南系统 - 功能测试报告")
    print("="*70)
    
    results = []
    
    print("\n📋 模块1: 用户认证模块")
    print("-"*40)
    results.append({"test": "TC-AUTH-001", "name": "正确用户登录", **test_auth_login_success()})
    results.append({"test": "TC-AUTH-002", "name": "错误密码登录", **test_auth_login_failure()})
    
    print("\n📋 模块2: 职业推荐模块")
    print("-"*40)
    results.append({"test": "TC-REC-001", "name": "完整自我介绍推荐", **test_career_recommendation()})
    results.append({"test": "TC-REC-002", "name": "空自我介绍", **test_career_recommendation_empty()})
    
    print("\n📋 模块3: 系统健康检查")
    print("-"*40)
    results.append({"test": "TC-SYS-001", "name": "健康检查", **test_health_check()})
    
    print("\n" + "="*70)
    print("📊 测试结果汇总")
    print("="*70)
    
    passed = sum(1 for r in results if r["status"] == "PASS")
    failed = sum(1 for r in results if r["status"] == "FAIL")
    total = len(results)
    
    print(f"\n测试总数: {total}")
    print(f"通过: {passed}")
    print(f"失败: {failed}")
    print(f"通过率: {(passed/total)*100:.1f}%")
    
    print("\n详细结果:")
    for r in results:
        status = "✅" if r["status"] == "PASS" else "❌"
        print(f"  {status} {r['test']}: {r['name']} - {r['status']} (响应时间: {r.get('response_time', 0):.2f}秒)")
        if "error" in r:
            print(f"     错误信息: {r['error'][:50]}...")
    
    print("\n" + "="*70)
    print("📈 性能指标")
    print("="*70)
    
    avg_time = sum(r.get("response_time", 0) for r in results) / total
    max_time = max(r.get("response_time", 0) for r in results)
    min_time = min(r.get("response_time", 0) for r in results)
    
    print(f"\n平均响应时间: {avg_time:.2f}秒")
    print(f"最大响应时间: {max_time:.2f}秒")
    print(f"最小响应时间: {min_time:.2f}秒")
    
    return results

if __name__ == "__main__":
    run_all_tests()