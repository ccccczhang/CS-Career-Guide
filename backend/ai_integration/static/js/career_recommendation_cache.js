/**
 * 职业推荐缓存工具
 * 使用 localStorage 实现本地缓存
 * 当用户填写相同自我介绍时，直接返回缓存结果
 */

const CAREER_RECOMMENDATION_CACHE_KEY = 'career_recommendation_cache_';
const CACHE_EXPIRE_HOURS = 24; // 缓存有效期24小时

/**
 * 生成缓存键（基于自我介绍内容的MD5哈希）
 */
function generateCacheKey(selfIntroduction) {
    // 简单的哈希函数
    let hash = 0;
    for (let i = 0; i < selfIntroduction.length; i++) {
        const char = selfIntroduction.charCodeAt(i);
        hash = ((hash << 5) - hash) + char;
        hash = hash & hash; // 转为32位整数
    }
    return CAREER_RECOMMENDATION_CACHE_KEY + Math.abs(hash).toString(36);
}

/**
 * 获取缓存数据
 */
function getCachedRecommendation(selfIntroduction) {
    try {
        const cacheKey = generateCacheKey(selfIntroduction);
        const cachedData = localStorage.getItem(cacheKey);
        
        if (cachedData) {
            const parsed = JSON.parse(cachedData);
            
            // 检查缓存是否过期
            const now = Date.now();
            if (parsed.expireTime && parsed.expireTime > now) {
                console.log('[Career Cache] 缓存命中');
                return parsed.data;
            } else {
                console.log('[Career Cache] 缓存已过期');
                localStorage.removeItem(cacheKey);
            }
        }
    } catch (error) {
        console.error('[Career Cache] 获取缓存失败:', error);
    }
    return null;
}

/**
 * 保存缓存数据
 */
function setCachedRecommendation(selfIntroduction, data) {
    try {
        const cacheKey = generateCacheKey(selfIntroduction);
        const expireTime = Date.now() + (CACHE_EXPIRE_HOURS * 60 * 60 * 1000);
        
        const cacheData = {
            data: data,
            expireTime: expireTime,
            cachedAt: Date.now()
        };
        
        localStorage.setItem(cacheKey, JSON.stringify(cacheData));
        console.log('[Career Cache] 缓存已保存');
    } catch (error) {
        console.error('[Career Cache] 保存缓存失败:', error);
    }
}

/**
 * 清除缓存
 */
function clearCareerRecommendationCache() {
    try {
        Object.keys(localStorage).forEach(key => {
            if (key.startsWith(CAREER_RECOMMENDATION_CACHE_KEY)) {
                localStorage.removeItem(key);
            }
        });
        console.log('[Career Cache] 所有缓存已清除');
    } catch (error) {
        console.error('[Career Cache] 清除缓存失败:', error);
    }
}

/**
 * 检查是否有缓存
 */
function hasCachedRecommendation(selfIntroduction) {
    return getCachedRecommendation(selfIntroduction) !== null;
}

// 暴露全局方法
window.CareerRecommendationCache = {
    get: getCachedRecommendation,
    set: setCachedRecommendation,
    clear: clearCareerRecommendationCache,
    has: hasCachedRecommendation
};
