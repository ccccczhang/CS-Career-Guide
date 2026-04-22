/**
 * 职业推荐主逻辑
 * 集成 localStorage 缓存功能
 */

// 等待DOM加载完成
document.addEventListener('DOMContentLoaded', function() {
    // 获取DOM元素
    const selfIntroductionInput = document.getElementById('self_introduction');
    const submitBtn = document.getElementById('submit_recommendation');
    const resultContainer = document.getElementById('recommendation_result');
    const loadingIndicator = document.getElementById('loading_indicator');
    const cacheHint = document.getElementById('cache_hint');

    // 监听输入变化，检查缓存
    selfIntroductionInput.addEventListener('input', function() {
        const content = this.value.trim();
        if (content.length > 0) {
            checkCache(content);
        } else {
            hideCacheHint();
        }
    });

    // 提交按钮点击事件
    submitBtn.addEventListener('click', async function() {
        const selfIntroduction = selfIntroductionInput.value.trim();
        
        if (!selfIntroduction) {
            alert('请填写自我介绍');
            return;
        }

        // 检查缓存
        const cachedData = CareerRecommendationCache.get(selfIntroduction);
        
        if (cachedData) {
            // 使用缓存结果（瞬间显示）
            console.log('[Career Recommendation] 使用缓存结果');
            showResult(cachedData);
            showCacheHint(true);
            return;
        }

        // 没有缓存，调用API
        await fetchRecommendation(selfIntroduction);
    });

    /**
     * 检查缓存状态
     */
    function checkCache(content) {
        const hasCache = CareerRecommendationCache.has(content);
        showCacheHint(hasCache);
    }

    /**
     * 显示缓存提示
     */
    function showCacheHint(hasCache) {
        if (cacheHint) {
            if (hasCache) {
                cacheHint.textContent = '💡 检测到相同内容，再次提交将使用缓存';
                cacheHint.style.display = 'block';
            } else {
                cacheHint.style.display = 'none';
            }
        }
    }

    /**
     * 隐藏缓存提示
     */
    function hideCacheHint() {
        if (cacheHint) {
            cacheHint.style.display = 'none';
        }
    }

    /**
     * 调用职业推荐API
     */
    async function fetchRecommendation(selfIntroduction) {
        // 显示加载状态
        showLoading(true);
        
        try {
            const response = await fetch('/api/ai/llm/career/recommendation/', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRFToken': getCSRFToken()
                },
                body: JSON.stringify({
                    self_introduction: selfIntroduction
                })
            });

            const result = await response.json();
            
            if (result.success) {
                // 保存到缓存
                CareerRecommendationCache.set(selfIntroduction, result);
                
                // 显示结果
                showResult(result);
                hideCacheHint();
            } else {
                showError(result.error || '获取推荐失败');
            }
        } catch (error) {
            console.error('[Career Recommendation] API调用失败:', error);
            showError('网络请求失败，请稍后重试');
        } finally {
            showLoading(false);
        }
    }

    /**
     * 显示推荐结果
     */
    function showResult(data) {
        if (!resultContainer) return;

        const recommendations = data.recommendations || [];
        const rawAnalysis = data.raw_analysis || '';

        let html = `
            <h3>🎯 职业推荐结果</h3>
            <div class="recommendations-list">
        `;

        recommendations.forEach((rec, index) => {
            html += `
                <div class="recommendation-item">
                    <div class="career-name">${index + 1}. ${rec.career}</div>
                    <div class="match-score">匹配度: ${rec.matchScore}%</div>
                    <div class="reason">推荐理由: ${rec.reason}</div>
                    ${rec.skillsMatch && rec.skillsMatch.length > 0 ? `
                    <div class="skills-match">
                        <strong>匹配技能:</strong> ${rec.skillsMatch.join(', ')}
                    </div>
                    ` : ''}
                    ${rec.missingSkills && rec.missingSkills.length > 0 ? `
                    <div class="missing-skills">
                        <strong>待提升技能:</strong> ${rec.missingSkills.join(', ')}
                    </div>
                    ` : ''}
                    ${rec.improvement ? `
                    <div class="improvement">
                        <strong>发展建议:</strong> ${rec.improvement}
                    </div>
                    ` : ''}
                </div>
            `;
        });

        html += `
            </div>
        `;

        if (rawAnalysis) {
            html += `
                <div class="analysis-section">
                    <h4>📝 详细分析</h4>
                    <div class="analysis-content">${rawAnalysis}</div>
                </div>
            `;
        }

        resultContainer.innerHTML = html;
        resultContainer.style.display = 'block';
    }

    /**
     * 显示加载状态
     */
    function showLoading(loading) {
        if (loadingIndicator) {
            loadingIndicator.style.display = loading ? 'block' : 'none';
        }
        if (submitBtn) {
            submitBtn.disabled = loading;
            submitBtn.textContent = loading ? '测评中...' : '开始测评';
        }
    }

    /**
     * 显示错误信息
     */
    function showError(message) {
        if (resultContainer) {
            resultContainer.innerHTML = `
                <div class="error-message">
                    ❌ ${message}
                </div>
            `;
            resultContainer.style.display = 'block';
        }
    }

    /**
     * 获取CSRF Token
     */
    function getCSRFToken() {
        const token = document.querySelector('meta[name="csrf-token"]');
        return token ? token.getAttribute('content') : '';
    }
});
