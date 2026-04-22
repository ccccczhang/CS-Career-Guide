<template>
  <div class="min-h-screen bg-gradient-to-br from-gray-900 via-blue-900 to-black relative">
    <!-- Starry Background -->
    <div class="absolute inset-0 z-0">
      <canvas ref="starCanvas" class="w-full h-full"></canvas>
    </div>
    
    <!-- Background Overlay -->
    <div class="absolute inset-0 bg-black/30 pointer-events-none"></div>
    
    <div ref="reportContent" class="relative z-10 space-y-12 py-8 p-4 lg:p-8">
      <div class="glass-card p-8 animate-fade-in-down">
        <h1 class="text-4xl font-bold mb-4 bg-clip-text text-transparent bg-gradient-to-r from-primary to-accent text-center">职业生涯规划报告</h1>
        <p class="text-blue-400 text-lg text-center">
          基于你的自我介绍和选择的职业，为你生成个性化的职业生涯规划方案
        </p>
      </div>
    
      <!-- 加载状态 -->
      <div v-if="isLoading" class="glass-card p-8 animate-fade-in-up">
        <div class="flex justify-center items-center">
          <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary"></div>
          <span class="ml-4 text-lg font-medium text-white">生成规划报告中...</span>
        </div>
      </div>
    
      <!-- 错误状态 -->
      <div v-else-if="error" class="glass-card p-8 animate-fade-in-up">
        <div class="text-center">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 text-error mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z" />
          </svg>
          <h3 class="text-xl font-bold mb-2 text-error">{{ error }}</h3>
          <p class="text-white/70 mb-4">请先完成自我介绍并选择至少一个职业小类</p>
          <div class="flex gap-4 justify-center">
            <button @click="goToSelfIntroduction" class="btn btn-primary">去完成自我介绍</button>
            <button @click="goToCareerEvaluation" class="btn btn-secondary">去选择职业</button>
          </div>
        </div>
      </div>
    
      <!-- 规划报告内容 -->
      <div v-else class="space-y-12">
        <!-- 顶部：个人信息概览 + 六维能力雷达图 -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <!-- 个人信息概览 -->
          <div class="rounded-2xl p-6 animate-fade-in-up backdrop-blur-lg bg-black/30 border border-white/10 shadow-lg h-full flex flex-col card-hover">
            <h2 class="text-xl font-bold mb-4 flex items-center gap-2 text-white">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
              个人信息概览
            </h2>
            <div class="flex-1 flex flex-col gap-4">
              <div>
                <h3 class="font-semibold text-base mb-2 text-white">自我介绍</h3>
                <p class="text-white/80 leading-relaxed text-sm line-clamp-3">
                  {{ selfIntroduction.selfDescription || '无' }}
                </p>
                <button v-if="selfIntroduction.selfDescription && selfIntroduction.selfDescription.length > 50" @click="toggleExpand('intro')" class="text-primary text-xs mt-1 hover:underline">
                  {{ expanded.intro ? '收起' : '展开' }}
                </button>
                <p v-if="expanded.intro && selfIntroduction.selfDescription" class="text-white/80 leading-relaxed text-sm mt-1">
                  {{ selfIntroduction.selfDescription }}
                </p>
              </div>
              <div>
                <h3 class="font-semibold text-base mb-2 text-white">技能优势</h3>
                <div class="flex flex-wrap gap-2">
                  <span v-for="(skill, index) in selfIntroduction.skills" :key="index" class="px-3 py-1 bg-primary/10 text-primary rounded-full text-xs shadow-sm">
                    {{ skill }}
                  </span>
                  <span v-if="selfIntroduction.otherSkills" class="px-3 py-1 bg-primary/10 text-primary rounded-full text-xs shadow-sm">
                    {{ selfIntroduction.otherSkills }}
                  </span>
                </div>
              </div>
              <div class="mt-auto">
                <h3 class="font-semibold text-base mb-2 text-white">选择的职业</h3>
                <div class="flex flex-wrap gap-2">
                  <span v-for="(career, index) in selectedCareers" :key="index" class="px-3 py-1 bg-accent/10 text-accent rounded-full text-xs shadow-sm">
                    {{ career }}
                  </span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 六维能力雷达图 -->
          <div class="rounded-2xl p-6 animate-fade-in-up backdrop-blur-lg bg-black/30 border border-white/10 shadow-lg h-full flex flex-col card-hover" style="animation-delay: 100ms;">
            <h2 class="text-xl font-bold mb-4 flex items-center gap-2 text-white">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
              能力评估雷达图
            </h2>
            <div class="flex-1 flex flex-col md:flex-row items-center gap-4">
              <!-- 左侧雷达图 -->
              <div class="w-full h-64 md:h-72">
                <v-chart :option="radarOption" @click="handleRadarClick" autoresize />
              </div>
              <!-- 右侧参数列表 -->
              <div class="w-full flex flex-col justify-center gap-2 border-t md:border-t-0 border-white/10 pt-4 md:pt-0">
                <div v-for="(item, index) in radarData" :key="index" class="flex justify-between items-center bg-white/5 rounded-lg p-2 hover:bg-white/10 transition-colors">
                  <div class="font-semibold text-white/80 text-xs flex items-center gap-2">
                    <span class="w-2 h-2 rounded-full bg-primary"></span>
                    {{ item.name }}
                  </div>
                  <div class="text-primary font-bold text-sm">{{ item.value }}%</div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 职业规划方案：时间轴 + 雷达图 -->
        <div class="rounded-2xl p-8 animate-fade-in-up backdrop-blur-lg bg-black/30 border border-white/10 shadow-lg card-hover">
          <h2 class="text-2xl font-bold mb-8 flex items-center justify-center gap-2 text-white">
            职业规划方案
          </h2>
          
          <!-- 时间轴 -->
          <div class="relative mb-12">
            <!-- 移动端垂直时间轴 -->
            <div class="md:hidden relative">
              <!-- 时间轴线 -->
              <div class="absolute left-6 top-0 bottom-0 w-0.5 bg-white/20 transform -translate-x-1/2"></div>
              
              <!-- 时间点 -->
              <div class="space-y-8">
                <div v-for="(plan, index) in careerPlan.filter(p => p.title.includes('短期') || p.title.includes('中期') || p.title.includes('长期'))" :key="index" class="relative pl-16">
                  <!-- 时间点圆圈 -->
                  <div @click="toggleExpand(`timeline-${index}`)" class="absolute left-0 top-0 w-12 h-12 rounded-full bg-primary/20 border-2 border-primary flex items-center justify-center text-primary font-bold z-10 transform transition-transform duration-300 hover:scale-110 cursor-pointer">
                    {{ index + 1 }}
                  </div>
                  
                  <!-- 时间点标签 -->
                  <div class="mt-1">
                    <h3 class="text-lg font-bold text-white mb-2">{{ plan.title.split('（')[0] }}</h3>
                    <div class="bg-white/10 backdrop-blur-sm p-4 rounded-lg border border-white/10 shadow-md hover:bg-white/15 transition-colors duration-300">
                      <span class="text-xl font-black text-white text-center block mb-1">
                        {{ plan.title.includes('短期') ? '1-2年' : plan.title.includes('中期') ? '3-5年' : '5-10年' }}
                      </span>
                      <span class="text-sm font-semibold text-primary text-center block">
                        {{ plan.title.includes('短期') ? '初级突破' : plan.title.includes('中期') ? '中级核心' : '高级/管理' }}
                      </span>
                    </div>
                    
                    <!-- 核心目标 -->
                    <p class="mt-4 text-white/80 text-sm line-clamp-2">
                      {{ plan.description }}
                    </p>
                    
                    <!-- 详细信息（点击时显示） -->
                    <div v-if="expanded[`timeline-${index}`]" class="mt-4 bg-black/80 backdrop-blur-md p-4 rounded-lg border border-white/10 shadow-lg transition-all duration-300">
                      <h4 class="font-semibold text-white mb-2">详细目标</h4>
                      <ul class="space-y-1 text-white/70 text-xs">
                        <li v-for="(detail, idx) in plan.details" :key="idx" class="flex items-start gap-2">
                          <span class="text-primary mt-1 text-xs">◆</span>
                          <span>{{ detail }}</span>
                        </li>
                      </ul>
                      <button @click="toggleExpand(`timeline-${index}`)" class="text-primary text-xs mt-3 hover:underline block mx-auto">
                        收起
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 桌面端横向时间轴 -->
            <div class="hidden md:block">
              <!-- 时间轴线 -->
              <div class="absolute top-1/2 left-0 right-0 h-1 bg-white/20 transform -translate-y-1/2"></div>
              
              <!-- 时间点 -->
              <div class="grid grid-cols-3 gap-4 relative">
                <div v-for="(plan, index) in careerPlan.filter(p => p.title.includes('短期') || p.title.includes('中期') || p.title.includes('长期'))" :key="index" class="flex flex-col items-center relative">
                  <!-- 时间点圆圈 -->
                  <div @click="toggleExpand(`timeline-${index}`)" class="w-12 h-12 rounded-full bg-primary/20 border-2 border-primary flex items-center justify-center text-primary font-bold z-10 transform transition-transform duration-300 hover:scale-110 cursor-pointer">
                    {{ index + 1 }}
                  </div>
                  
                  <!-- 时间点标签 -->
                  <div class="mt-4 text-center">
                    <h3 class="text-lg font-bold text-white mb-2">{{ plan.title.split('（')[0] }}</h3>
                    <div class="bg-white/10 backdrop-blur-sm p-4 rounded-lg border border-white/10 shadow-md hover:bg-white/15 transition-colors duration-300 w-full">
                      <span class="text-xl font-black text-white text-center block mb-1">
                        {{ plan.title.includes('短期') ? '1-2年' : plan.title.includes('中期') ? '3-5年' : '5-10年' }}
                      </span>
                      <span class="text-sm font-semibold text-primary text-center block">
                        {{ plan.title.includes('短期') ? '初级突破' : plan.title.includes('中期') ? '中级核心' : '高级/管理' }}
                      </span>
                    </div>
                    
                    <!-- 核心目标 -->
                    <p class="mt-4 text-white/80 text-sm text-center line-clamp-2 px-4">
                      {{ plan.description }}
                    </p>
                    
                    <!-- 详细信息（点击时显示） -->
                    <div v-if="expanded[`timeline-${index}`]" class="absolute top-full left-1/2 transform -translate-x-1/2 mt-4 w-64 bg-black/80 backdrop-blur-md p-4 rounded-lg border border-white/10 shadow-lg opacity-100 visible transition-all duration-300 z-20">
                      <h4 class="font-semibold text-white mb-2">详细目标</h4>
                      <ul class="space-y-1 text-white/70 text-xs">
                        <li v-for="(detail, idx) in plan.details" :key="idx" class="flex items-start gap-2">
                          <span class="text-primary mt-1 text-xs">◆</span>
                          <span>{{ detail }}</span>
                        </li>
                      </ul>
                      <button @click="toggleExpand(`timeline-${index}`)" class="text-primary text-xs mt-3 hover:underline block mx-auto">
                        收起
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 行业趋势 & 行动计划 -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
          <!-- 行业趋势：标签云 -->
          <div class="rounded-2xl p-6 animate-fade-in-up backdrop-blur-lg bg-black/30 border border-white/10 shadow-lg card-hover">
            <h2 class="text-xl font-bold mb-6 flex items-center gap-2 text-white">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
              行业趋势分析
            </h2>
            <div class="flex flex-wrap gap-3 justify-center">
              <div @click="showTrendDetail(trend)" v-for="(trend, index) in industryTrends" :key="index" class="flex items-center gap-2 bg-white/10 hover:bg-white/15 transition-colors duration-300 px-4 py-2 rounded-full border border-white/10 cursor-pointer transform hover:scale-105">
                <span class="text-lg">{{ trend.icon }}</span>
                <span class="text-white/80 text-sm">{{ trend.text }}</span>
              </div>
            </div>
            <div class="mt-6 text-white/70 text-sm text-center">
              <p>根据当前市场趋势和技术发展方向，{{ selectedCareers.length > 0 ? selectedCareers.join('、') : '相关' }}领域呈现以上发展趋势</p>
            </div>
          </div>
          
          <!-- 行业趋势详情模态框 -->
          <div v-if="selectedTrend" class="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50 transition-opacity duration-300">
            <div class="bg-gradient-to-br from-gray-900 to-black rounded-2xl p-8 max-w-md w-full mx-4 transform transition-transform duration-300 scale-100">
              <div class="flex justify-between items-center mb-6">
                <h3 class="text-xl font-bold text-white flex items-center gap-2">
                  <span class="text-2xl">{{ selectedTrend.icon }}</span>
                  {{ selectedTrend.text }}
                </h3>
                <button @click="selectedTrend = null" class="text-white/70 hover:text-white transition-colors">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
              <div class="text-white/80 text-sm space-y-3">
                <p>{{ selectedTrend.description }}</p>
                <div v-if="selectedTrend.impact" class="mt-4">
                  <h4 class="font-semibold text-white mb-2">对职业发展的影响</h4>
                  <ul class="space-y-1 text-white/70 text-xs">
                    <li v-for="(item, idx) in selectedTrend.impact" :key="idx" class="flex items-start gap-2">
                      <span class="text-primary mt-1 text-xs">◆</span>
                      <span>{{ item }}</span>
                    </li>
                  </ul>
                </div>
              </div>
              <div class="mt-6 flex justify-end">
                <button @click="selectedTrend = null" class="btn btn-primary">关闭</button>
              </div>
            </div>
          </div>
          
          <!-- 行动计划：步骤进度条 -->
          <div class="rounded-2xl p-6 animate-fade-in-up backdrop-blur-lg bg-black/30 border border-white/10 shadow-lg card-hover">
            <h2 class="text-xl font-bold mb-6 flex items-center gap-2 text-white">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
              </svg>
              行动计划
            </h2>
            <div class="space-y-4">
              <div v-for="(action, index) in actionPlan.slice(0, 3)" :key="index" class="relative">
                <!-- 步骤条 -->
                <div class="flex gap-4">
                  <div class="flex-shrink-0 w-8 h-8 rounded-full bg-primary/20 flex items-center justify-center text-primary font-bold shadow-md">
                    {{ index + 1 }}
                  </div>
                  <div class="flex-1">
                    <div class="flex justify-between items-center">
                      <h3 class="font-bold text-white text-sm">{{ action.title }}</h3>
                      <span class="text-xs text-primary">{{ action.timeframe }}</span>
                    </div>
                    <p class="text-white/70 text-xs mt-1 line-clamp-2">{{ action.description }}</p>
                    <button v-if="action.steps && action.steps.length > 0" @click="toggleExpand(`action-${index}`)" class="text-primary text-xs mt-1 hover:underline">
                      {{ expanded[`action-${index}`] ? '收起' : '查看步骤' }}
                    </button>
                    <ul v-if="expanded[`action-${index}`] && action.steps" class="mt-2 space-y-2 text-white/60 text-xs">
                      <li v-for="(step, idx) in action.steps" :key="idx" class="flex items-start gap-2">
                        <input 
                          type="checkbox" 
                          :checked="actionProgress[`action-${index}`]?.[idx] || false"
                          @change="toggleStepCompletion(index, idx)"
                          class="mt-1 w-4 h-4 rounded border-white/30 bg-black/50 text-primary focus:ring-primary"
                        />
                        <span :class="{ 'line-through text-white/40': actionProgress[`action-${index}`]?.[idx] }">
                          {{ step }}
                        </span>
                      </li>
                    </ul>
                    <div v-if="expanded[`action-${index}`] && action.steps" class="mt-3 flex justify-between items-center">
                      <span class="text-xs text-white/70">
                        {{ getCompletedStepsCount(index) }}/{{ action.steps.length }} 已完成
                      </span>
                      <div class="w-32 bg-white/10 rounded-full h-2">
                        <div 
                          class="bg-primary h-2 rounded-full transition-all duration-300"
                          :style="{ width: getCompletionPercentage(index) + '%' }"
                        ></div>
                      </div>
                    </div>
                  </div>
                </div>
                <!-- 连接线 -->
                <div v-if="index < actionPlan.slice(0, 3).length - 1" class="absolute left-4 top-8 bottom-0 w-0.5 bg-white/10 transform -translate-x-1/2"></div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 职业发展建议：2×3网格卡片 -->
        <div class="rounded-2xl p-8 animate-fade-in-up backdrop-blur-lg bg-black/30 border border-white/10 shadow-lg card-hover">
          <h2 class="text-2xl font-bold mb-8 flex items-center justify-center gap-2 text-white">
            职业发展建议
          </h2>
          <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
            <div @click="toggleAdviceDetail(index)" v-for="(advice, index) in careerAdvice" :key="index" class="backdrop-blur-md bg-white/5 p-5 rounded-xl border border-white/10 hover:bg-white/10 transition-all duration-300 cursor-pointer relative">
              <div class="text-3xl mb-3">{{ advice.icon }}</div>
              <h3 class="font-bold text-white mb-2">{{ advice.title }}</h3>
              <div class="flex flex-wrap gap-2 mb-3">
                <span v-for="(keyword, idx) in advice.keywords" :key="idx" class="px-2 py-1 bg-primary/10 text-primary rounded-full text-xs">
                  {{ keyword }}
                </span>
              </div>
              <!-- 详细建议（点击时显示） -->
              <div v-if="expandedAdvice === index" class="absolute inset-0 bg-black/80 backdrop-blur-md rounded-xl border border-white/10 p-5 opacity-100 visible transition-all duration-300 z-10">
                <div class="flex justify-between items-start mb-3">
                  <h4 class="font-bold text-white">{{ advice.title }}</h4>
                  <button @click.stop="toggleAdviceDetail(index)" class="text-white/70 hover:text-white transition-colors">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>
                </div>
                <ul class="space-y-2 text-white/70 text-xs">
                  <li v-for="(item, idx) in advice.details" :key="idx" class="flex items-start gap-2">
                    <span class="text-primary mt-1 text-xs">◆</span>
                    <span>{{ item }}</span>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 功能跳转按钮 -->
        <div class="p-8 animate-fade-in-up bg-transparent">
          <div class="flex flex-row justify-center items-center gap-6">
            <!-- 企业榜单按钮 -->
            <router-link 
              to="/company-reviews"
              class="group relative overflow-hidden bg-secondary text-on-secondary px-12 py-5 rounded-xl font-headline font-bold text-lg shadow-[0_10px_30px_rgba(70,72,212,0.3)] hover:shadow-[0_15px_40px_rgba(70,72,212,0.4)] transition-all active:scale-95 flex items-center gap-3 btn-hover"
            >
              <span class="relative z-10 flex items-center gap-3">
                跳转企业榜单
              </span>
              <div class="absolute inset-0 bg-gradient-to-r from-secondary to-secondary-container opacity-0 group-hover:opacity-100 transition-opacity"></div>
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style>
.text-shadow {
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

/* 卡片动画效果 */
.card-hover {
  transition: all 0.3s ease;
}

.card-hover:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

/* 按钮动画效果 */
.btn-hover {
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.btn-hover::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.6s ease;
}

.btn-hover:hover::before {
  left: 100%;
}

/* 展开/收起动画 */
.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* 模态框动画 */
.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-content,
.modal-leave-to .modal-content {
  transform: scale(0.9);
}

/* 进度条动画 */
.progress-animation {
  transition: width 0.5s ease-in-out;
}

/* 加载动画 */
.loading-spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 脉冲动画 */
.pulse {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(147, 51, 234, 0.4);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(147, 51, 234, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(147, 51, 234, 0);
  }
}
</style>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { use } from 'echarts/core'
import { RadarChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import VChart from 'vue-echarts'
import html2canvas from 'html2canvas'
import jsPDF from 'jspdf'
import api from '@/api'

const starCanvas = ref(null)
const reportContent = ref(null)
let starCtx = null
let stars = []
let meteors = []
let animationId = null

// 注册必要的组件
use([
  RadarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  CanvasRenderer
])

// 获取路由参数和本地存储数据
const router = useRouter()
const isLoading = ref(true)
const error = ref('')
const selfIntroduction = ref({
  skills: [],
  otherSkills: '',
  selfDescription: ''
})
const selectedCareers = ref([])
const careerPlan = ref([])
const actionPlan = ref([])
const radarData = ref([])

// 展开/收起状态管理
const expanded = ref({
  intro: false
})

// 选中的趋势
const selectedTrend = ref(null)

// 行业趋势数据
const industryTrends = ref([
  {
    icon: '🚀', 
    text: '数字化转型',
    description: '企业数字化转型已成为必然趋势，通过技术手段优化业务流程，提升运营效率。',
    impact: [
      '对技术人才需求大幅增加',
      '传统岗位面临转型挑战',
      '新兴技术岗位机会增多',
      '跨领域技能成为核心竞争力'
    ]
  },
  {
    icon: '🤖', 
    text: 'AI融合',
    description: '人工智能技术与各行业深度融合，改变传统工作方式和业务模式。',
    impact: [
      'AI相关技能成为职场必备',
      '部分重复性工作被自动化取代',
      '人机协作成为新工作模式',
      'AI伦理和治理成为重要议题'
    ]
  },
  {
    icon: '💻', 
    text: '远程办公',
    description: '远程办公和混合办公模式成为常态，工作方式更加灵活。',
    impact: [
      '沟通协作能力要求提高',
      '自主管理能力成为关键',
      '数字工具使用技能重要性提升',
      '工作与生活边界模糊化'
    ]
  },
  {
    icon: '🧠', 
    text: '复合型人才',
    description: '市场对具备跨领域知识和技能的复合型人才需求增加。',
    impact: [
      '单一技能人才面临挑战',
      '跨学科背景成为优势',
      '持续学习能力成为核心竞争力',
      '综合素质评价比重增加'
    ]
  },
  {
    icon: '📊', 
    text: '数据驱动',
    description: '数据成为企业决策的核心依据，数据分析能力成为职场必备技能。',
    impact: [
      '数据分析师需求持续增长',
      '业务决策更加依赖数据',
      '数据隐私和安全问题凸显',
      '数据可视化能力重要性提升'
    ]
  },
  {
    icon: '🔒', 
    text: '网络安全',
    description: '随着数字化程度加深，网络安全成为企业和个人的重要考量。',
    impact: [
      '网络安全人才缺口巨大',
      '安全意识成为全员必备',
      '安全合规要求日益严格',
      '安全技术不断迭代更新'
    ]
  },
  {
    icon: '🌐', 
    text: '云计算',
    description: '云计算技术广泛应用，改变IT基础设施和服务交付模式。',
    impact: [
      '云服务技能需求增加',
      '传统IT架构向云迁移',
      '云原生应用开发成为主流',
      'DevOps实践日益普及'
    ]
  },
  {
    icon: '📱', 
    text: '移动应用',
    description: '移动应用渗透到生活和工作的各个方面，成为主要交互方式。',
    impact: [
      '移动开发人才需求持续',
      '跨平台开发技术重要性提升',
      '用户体验设计成为核心竞争力',
      '移动安全问题日益突出'
    ]
  }
])

// 显示趋势详情
const showTrendDetail = (trend) => {
  selectedTrend.value = trend
}

// 展开的建议卡片索引
const expandedAdvice = ref(null)

// 切换建议详情显示
const toggleAdviceDetail = (index) => {
  expandedAdvice.value = expandedAdvice.value === index ? null : index
}

// 行动计划进度跟踪
const actionProgress = ref({})

// 切换步骤完成状态
const toggleStepCompletion = (actionIndex, stepIndex) => {
  const actionKey = `action-${actionIndex}`
  if (!actionProgress.value[actionKey]) {
    actionProgress.value[actionKey] = {}
  }
  actionProgress.value[actionKey][stepIndex] = !actionProgress.value[actionKey][stepIndex]
}

// 获取已完成步骤数
const getCompletedStepsCount = (actionIndex) => {
  const actionKey = `action-${actionIndex}`
  const progress = actionProgress.value[actionKey]
  if (!progress) return 0
  return Object.values(progress).filter(Boolean).length
}

// 获取完成百分比
const getCompletionPercentage = (actionIndex) => {
  const action = actionPlan.value[actionIndex]
  if (!action || !action.steps) return 0
  const completed = getCompletedStepsCount(actionIndex)
  return Math.round((completed / action.steps.length) * 100)
}

// 职业发展建议数据
const careerAdvice = ref([
  {
    icon: '📚',
    title: '技能提升',
    keywords: ['深耕核心', '认证加分'],
    details: [
      '制定分阶段的技能学习计划',
      '定期参加行业会议和培训',
      '获取相关领域的专业认证',
      '参与开源项目提升实战能力',
      '建立个人技术博客分享经验'
    ]
  },
  {
    icon: '🤝',
    title: '职业网络',
    keywords: ['社区活跃', '导师结对'],
    details: [
      '加入行业协会和专业社区',
      '积极参与线上线下技术交流',
      '在LinkedIn等平台建立专业形象',
      '寻找行业导师获取指导',
      '定期与同行交流分享经验'
    ]
  },
  {
    icon: '💼',
    title: '工作策略',
    keywords: ['主动挑战', '平衡生活'],
    details: [
      '设定明确的职业目标和里程碑',
      '主动承担有挑战性的项目',
      '定期与上级沟通职业发展',
      '建立有效的时间管理能力',
      '保持工作与生活的平衡'
    ]
  },
  {
    icon: '✨',
    title: '持续创新',
    keywords: ['跨域学习', '保持好奇'],
    details: [
      '关注行业最新动态和技术趋势',
      '学习跨领域知识拓宽视野',
      '培养创新思维和解决问题能力',
      '保持好奇心探索新领域',
      '定期参加在线课程更新知识'
    ]
  },
  {
    icon: '🌟',
    title: '个人品牌',
    keywords: ['开源分享', '技术曝光'],
    details: [
      '创建个人技术博客或公众号',
      '在技术社区积极回答问题',
      '参与技术会议或沙龙演讲',
      '贡献开源项目展示技术能力',
      '定期更新个人简历和作品集'
    ]
  },
  {
    icon: '🔄',
    title: '适应变化',
    keywords: ['情报追踪', '拥抱挑战'],
    details: [
      '关注数字化转型趋势',
      '了解人工智能等新兴技术应用',
      '适应远程工作和灵活办公模式',
      '培养跨领域协作能力',
      '保持开放心态接受新挑战'
    ]
  }
])

// 切换展开/收起状态
const toggleExpand = (key) => {
  expanded.value[key] = !expanded.value[key]
}

// 跳转到自我介绍页面
const goToSelfIntroduction = () => {
  router.push('/career-paths/employment/self-introduction')
}

// 跳转到职业测评页面
const goToCareerEvaluation = () => {
  router.push('/career-evaluation')
}

// 生成雷达图数据
const generateRadarData = () => {
  // 六维能力评估
  const data = [
    { name: '专业技能', value: 70 },
    { name: '沟通能力', value: 60 },
    { name: '团队协作', value: 65 },
    { name: '问题解决', value: 75 },
    { name: '学习能力', value: 80 },
    { name: '创新思维', value: 55 }
  ]
  
  // 基于用户技能和职业方向调整数据
  if (selfIntroduction.value.skills && selfIntroduction.value.skills.length > 0) {
    data[0].value = Math.min(90, data[0].value + selfIntroduction.value.skills.length * 5)
  }
  
  radarData.value = data
  return data
}

// 雷达图配置
const radarOption = computed(() => {
  // 确保当雷达图数据还没加载或者加载失败时有一个默认展示
  const data = radarData.value.length > 0 ? radarData.value : [
    { name: '专业技能', value: 0 },
    { name: '沟通能力', value: 0 },
    { name: '团队协作', value: 0 },
    { name: '问题解决', value: 0 },
    { name: '学习能力', value: 0 },
    { name: '创新思维', value: 0 }
  ]
  
  // 能力等级描述
  const getLevelDescription = (value) => {
    if (value >= 80) return '优秀 - 行业领先水平'
    if (value >= 60) return '良好 - 符合岗位要求'
    if (value >= 40) return '一般 - 需要提升'
    return '待发展 - 重点提升领域'
  }
  
  // 能力提升建议
  const getImprovementSuggestion = (name) => {
    const suggestions = {
      '专业技能': '建议通过项目实践和专业培训提升核心技能，获取相关认证。',
      '沟通能力': '建议多参与团队讨论和演讲，提高表达能力和倾听技巧。',
      '团队协作': '建议主动参与团队项目，学习团队管理和协作工具。',
      '问题解决': '建议多挑战复杂问题，学习系统思维和分析方法。',
      '学习能力': '建议建立持续学习习惯，关注行业动态和新技术。',
      '创新思维': '建议培养批判性思维，尝试跨领域学习和创新实践。'
    }
    return suggestions[name] || '建议制定个性化提升计划。'
  }
  
  return {
    tooltip: {
      trigger: 'item',
      formatter: function(params) {
        const value = params.value
        const level = getLevelDescription(value)
        const suggestion = getImprovementSuggestion(params.name)
        return `
          <div style="padding: 8px;">
            <div style="font-weight: bold; color: #fff; margin-bottom: 4px;">${params.name}</div>
            <div style="color: #9333ea; font-size: 18px; font-weight: bold; margin-bottom: 4px;">${value}%</div>
            <div style="color: #fff; margin-bottom: 4px;">${level}</div>
            <div style="color: #ccc; font-size: 12px;">${suggestion}</div>
          </div>
        `
      },
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      borderColor: 'rgba(147, 51, 234, 0.5)',
      borderWidth: 1,
      padding: 10,
      textStyle: {
        color: '#fff'
      }
    },
    radar: {
      indicator: data.map(item => ({
        name: item.name,
        max: 100
      })),
      radius: '60%', // 减小半径以确保标签完全显示
      center: ['50%', '50%'],
      splitNumber: 5,
      axisName: {
        color: '#ffffff',
        fontSize: 12,
        padding: [3, 5] // 增加标签内边距
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(255, 255, 255, 0.2)'
        }
      },
      splitArea: {
        show: true,
        areaStyle: {
          color: ['rgba(147, 51, 234, 0.05)', 'rgba(147, 51, 234, 0.1)', 'rgba(147, 51, 234, 0.15)', 'rgba(147, 51, 234, 0.2)', 'rgba(147, 51, 234, 0.25)']
        }
      },
      axisLine: {
        lineStyle: {
          color: 'rgba(255, 255, 255, 0.3)'
        }
      }
    },
    series: [{
      type: 'radar',
      data: [{
        value: data.map(item => item.value),
        name: '能力评估',
        areaStyle: {
          color: 'rgba(147, 51, 234, 0.3)'
        },
        lineStyle: {
          color: '#9333ea',
          width: 2
        },
        itemStyle: {
          color: '#9333ea',
          borderColor: '#ffffff',
          borderWidth: 2
        },
        emphasis: {
          lineStyle: {
            width: 4
          },
          itemStyle: {
            borderWidth: 4
          }
        }
      }]
    }]
  }
})

// 处理雷达图点击事件
const handleRadarClick = (params) => {
  if (params.componentType === 'series') {
    const dataIndex = params.dataIndex
    const indicatorIndex = params.seriesIndex
    const indicatorName = params.name
    const value = params.value
    
    // 显示详细信息
    console.log('点击了雷达图:', {
      indicatorName,
      value,
      level: value >= 80 ? '优秀' : value >= 60 ? '良好' : value >= 40 ? '一般' : '待发展'
    })
    
    // 可以在这里添加更复杂的交互逻辑，比如显示详细的能力分析模态框
  }
}

// 生成职业规划方案
const generateCareerPlan = () => {
  // 基于用户选择的职业和自我介绍生成规划方案
  const plans = []
  
  if (selectedCareers.value.length > 0) {
    // 短期职业目标
    plans.push({
      title: '短期职业目标（1-2年）',
      description: `专注于${selectedCareers.value.join('、')}相关技能的学习和积累，通过项目实践提升专业能力，争取在相关领域获得初级职位。`,
      details: [
        `掌握${selectedCareers.value.join('、')}领域的核心技术栈`,
        '完成2-3个实际项目，积累实战经验',
        '建立专业网络，参与行业社区活动',
        '获取相关领域的入门级认证',
        '制定详细的学习计划，定期评估学习进度'
      ]
    })
    
    // 中期职业目标
    plans.push({
      title: '中期职业目标（3-5年）',
      description: `在${selectedCareers.value.join('、')}领域积累工作经验，提升技术能力和项目管理能力，争取晋升到中级职位，成为团队的核心成员。`,
      details: [
        '晋升到中级工程师或相关管理职位',
        '主导1-2个中型项目的开发和实施',
        '培养团队协作和领导力',
        '深入研究行业前沿技术，发表技术分享',
        '建立个人专业品牌，在行业内获得认可'
      ]
    })
    
    // 长期职业目标
    plans.push({
      title: '长期职业目标（5-10年）',
      description: `在${selectedCareers.value.join('、')}领域成为专家，具备独立负责大型项目的能力，争取晋升到高级职位或技术管理职位。`,
      details: [
        '晋升到高级工程师或技术管理职位',
        '主导大型项目的架构设计和实施',
        '培养和指导初级工程师',
        '在行业内建立个人品牌和影响力',
        '参与行业标准制定或技术创新'
      ]
    })
  }
  
  // 基于用户技能优势添加个性化规划
  if (selfIntroduction.value.skills && selfIntroduction.value.skills.length > 0) {
    plans.push({
      title: '技能发展规划',
      description: `充分发挥你在${selfIntroduction.value.skills.join('、')}等方面的优势，同时有针对性地提升相关技术栈，形成独特的技术竞争力。`,
      details: [
        `深化${selfIntroduction.value.skills.join('、')}等核心技能，达到专家水平`,
        '学习与核心技能相关的互补技术，拓展技术广度',
        '参与技术社区，分享专业知识，建立技术影响力',
        '持续关注技术发展趋势，保持技术敏感度',
        '通过项目实践和技术挑战不断提升技能水平'
      ]
    })
  }
  
  // 添加行业趋势适应规划
  if (selectedCareers.value.length > 0) {
    plans.push({
      title: '行业趋势适应规划',
      description: `根据${selectedCareers.value.join('、')}领域的发展趋势，制定相应的适应策略，确保职业发展与行业需求同步。`,
      details: [
        '持续关注行业最新技术趋势和发展方向',
        '学习人工智能、大数据等新兴技术在本领域的应用',
        '适应数字化转型带来的工作模式变化',
        '培养跨领域协作能力，适应复合型人才需求',
        '建立行业情报收集机制，及时调整职业发展方向'
      ]
    })
  }
  
  return plans
}

// 生成行动计划
const generateActionPlan = () => {
  const actions = []
  
  // 学习计划
  actions.push({
    title: '制定个性化学习计划',
    description: '根据选择的职业方向和当前技能水平，制定详细的学习计划，包括技术栈学习、项目实践和认证考试等。',
    timeframe: '1个月内完成计划制定，持续执行',
    steps: [
      '使用技能评估工具全面评估当前技能水平',
      '识别与目标职业相关的关键技能和知识 gaps',
      '确定学习目标和优先级，制定阶段性学习计划',
      '选择高质量的学习资源，包括在线课程、书籍和实践项目',
      '建立学习进度跟踪机制，每周评估学习效果',
      '根据学习效果及时调整学习计划'
    ]
  })
  
  // 项目实践
  actions.push({
    title: '积极参与项目实践',
    description: '通过参与开源项目、个人项目或实习等方式，积累实际项目经验，提升动手能力和解决问题的能力。',
    timeframe: '持续进行，每月至少完成1个小项目',
    steps: [
      '根据目标职业选择相关的开源项目或创建个人项目',
      '设定明确的项目目标和范围，确保项目具有实际应用价值',
      '按照行业标准和最佳实践进行项目开发',
      '寻求同行的反馈和代码审查，不断改进代码质量',
      '将项目成果部署到公共平台，建立个人作品集',
      '定期更新和维护项目，展示持续学习能力'
    ]
  })
  
  // 建立专业网络
  actions.push({
    title: '建立和拓展专业网络',
    description: '通过技术社区、行业会议、社交媒体等渠道，建立专业网络，了解行业动态和机会，获取职业发展支持。',
    timeframe: '持续进行，每周至少参与1次专业活动',
    steps: [
      '加入相关技术社区和行业协会，如GitHub、Stack Overflow、技术沙龙等',
      '积极参与线上线下技术交流活动，分享知识和经验',
      '在LinkedIn、Twitter等平台建立专业形象，分享专业内容',
      '主动与行业专家和同行建立联系，寻求指导和合作机会',
      '定期参加行业会议和研讨会，了解最新技术趋势',
      '建立和维护专业联系人数据库，保持定期沟通'
    ]
  })
  
  // 职业品牌建设
  actions.push({
    title: '打造个人职业品牌',
    description: '通过内容创作、技术分享和专业贡献，在行业内建立个人品牌，提升职业竞争力。',
    timeframe: '持续进行，每季度至少发布2-3篇专业内容',
    steps: [
      '创建个人技术博客或公众号，分享技术心得和项目经验',
      '参与技术社区问答，帮助他人解决问题',
      '在技术会议或沙龙中进行演讲，分享专业知识',
      '参与开源项目贡献，提升技术影响力',
      '获得相关领域的专业认证，增强专业 credibility',
      '定期更新个人简历和LinkedIn资料，展示最新成就'
    ]
  })
  
  // 定期评估调整
  actions.push({
    title: '定期评估和调整职业规划',
    description: '每3-6个月对职业规划进行全面评估和调整，根据实际情况和行业变化，及时修正计划。',
    timeframe: '每3-6个月进行一次全面评估',
    steps: [
      '回顾职业目标和当前进度，评估目标完成情况',
      '评估技能提升和项目经验积累情况',
      '分析行业趋势和就业市场变化',
      '收集和分析职业发展机会和挑战',
      '调整职业规划和行动计划，设定新的目标和里程碑',
      '制定下一阶段的具体行动步骤和时间框架'
    ]
  })
  
  return actions
}

// 保存职业生涯规划报告到后端
const saveCareerPlanReport = async () => {
  try {
    const sessionId = 'plan_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
    const reportContent = JSON.stringify({
      careerPlan: careerPlan.value,
      actionPlan: actionPlan.value,
      selfIntroduction: selfIntroduction.value,
      selectedCareers: selectedCareers.value
    })
    
    const response = await aiAPI.saveCareerPlan(sessionId, reportContent, radarData.value)
    
    console.log('职业生涯规划报告保存成功', response)
  } catch (error) {
    console.error('保存职业生涯规划报告失败:', error)
  }
}

const fetchCareerPlanFromAI = async (selfIntroStr, careers) => {
  try {
    const response = await aiAPI.careerPlanGeneration(selfIntroStr, careers)
    
    if (response && response.success && response.data) {
      const data = response.data
      
      // 更新职业规划
      if (data.careerPlan && Array.isArray(data.careerPlan)) {
        careerPlan.value = data.careerPlan
      }
      
      // 更新雷达图数据
      if (data.radarData && Array.isArray(data.radarData)) {
        radarData.value = data.radarData
      }
      
      // 更新行动计划
      if (data.actionPlan && Array.isArray(data.actionPlan)) {
        actionPlan.value = data.actionPlan
      }
      
      return true
    }
    return false
  } catch (err) {
    console.error('获取AI职业规划失败:', err)
    return false
  }
}

// 加载数据并生成规划报告
const loadData = async () => {
  try {
    // 从localStorage加载自我介绍数据
    const selfIntroData = localStorage.getItem('selfIntroduction')
    if (selfIntroData) {
      selfIntroduction.value = JSON.parse(selfIntroData)
    } else {
      error.value = '未找到自我介绍数据'
      isLoading.value = false
      return
    }
    
    // 从localStorage加载选择的职业数据
    const selectedCareersData = localStorage.getItem('selectedCareers')
    if (selectedCareersData) {
      selectedCareers.value = JSON.parse(selectedCareersData)
    } else {
      error.value = '未找到选择的职业数据'
      isLoading.value = false
      return
    }
    
    // 构建传给AI的字符串
    let selfIntroStr = selfIntroduction.value.selfDescription || ''
    if (selfIntroduction.value.skills && selfIntroduction.value.skills.length > 0) {
      selfIntroStr += `。我的核心技能包括：${selfIntroduction.value.skills.join('、')}`
    }
    if (selfIntroduction.value.otherSkills) {
      selfIntroStr += `。其他技能：${selfIntroduction.value.otherSkills}`
    }
    
    // 尝试从AI获取个性化规划
    const aiSuccess = await fetchCareerPlanFromAI(selfIntroStr, selectedCareers.value)
    
    // 如果AI请求失败或没有数据，回退到本地生成逻辑
    if (!aiSuccess) {
      console.log('AI生成失败或返回为空，使用本地兜底逻辑生成规划')
      // 生成职业规划方案
      careerPlan.value = generateCareerPlan()
      
      // 生成行动计划
      actionPlan.value = generateActionPlan()
      
      // 生成雷达图数据
      generateRadarData()
    }
    
    isLoading.value = false
    
    // 保存职业生涯规划报告
    saveCareerPlanReport()
  } catch (err) {
    console.error('加载数据失败:', err)
    error.value = '加载数据失败，请重试'
    isLoading.value = false
  }
}

onMounted(() => {
  loadData()
  
  // Initialize starry background
  if (starCanvas.value) {
    starCtx = starCanvas.value.getContext('2d')
    resizeCanvas()
    animateStars()
  }
  
  // Add event listeners
  window.addEventListener('resize', resizeCanvas)
})

// 导出PDF报告
const isExporting = ref(false)
const exportMessage = ref('')

const exportToPDF = async () => {
  try {
    if (!reportContent.value) {
      console.error('报告内容未找到')
      exportMessage.value = '报告内容未找到'
      setTimeout(() => {
        exportMessage.value = ''
      }, 3000)
      return
    }
    
    // 显示加载状态
    isExporting.value = true
    exportMessage.value = '正在生成PDF报告，请稍候...'
    
    // 捕获报告内容
    const canvas = await html2canvas(reportContent.value, {
      scale: 1, // 进一步降低分辨率以提高性能
      useCORS: true, // 允许跨域图片
      logging: true, // 启用日志以便调试
      backgroundColor: '#111827', // 匹配背景色
      scrollY: 0, // 从顶部开始捕获
      allowTaint: true, // 允许跨域图片
      removeContainer: true, // 完成后移除临时容器
      timeout: 60000, // 设置60秒超时
      ignoreElements: (element) => {
        // 忽略可能导致问题的元素
        return element.classList.contains('modal-overlay') || 
               element.classList.contains('fixed') || 
               element.classList.contains('sticky')
      },
      onclone: (clonedDoc) => {
        // 处理不支持的颜色函数
        const elements = clonedDoc.querySelectorAll('*');
        elements.forEach(element => {
          const style = clonedDoc.defaultView.getComputedStyle(element);
          const backgroundColor = style.backgroundColor;
          const color = style.color;
          
          // 替换不支持的颜色函数
          if (backgroundColor.includes('oklch') || backgroundColor.includes('oklab')) {
            element.style.backgroundColor = '#111827'; // 使用默认背景色
          }
          if (color.includes('oklch') || color.includes('oklab')) {
            element.style.color = '#ffffff'; // 使用默认文本颜色
          }
        });
      }
    })
    
    // 创建PDF
    const pdf = new jsPDF({
      orientation: 'portrait',
      unit: 'mm',
      format: 'a4'
    })
    
    // 计算页面大小和图片位置
    const imgWidth = 210 // A4宽度
    const imgHeight = (canvas.height * imgWidth) / canvas.width
    let heightLeft = imgHeight
    
    let position = 0
    
    // 添加第一张图片
    pdf.addImage(canvas.toDataURL('image/png'), 'PNG', 0, position, imgWidth, imgHeight)
    heightLeft -= 297 // A4高度
    
    // 处理分页
    while (heightLeft > 0) {
      position = heightLeft - imgHeight
      pdf.addPage()
      pdf.addImage(canvas.toDataURL('image/png'), 'PNG', 0, position, imgWidth, imgHeight)
      heightLeft -= 297
    }
    
    // 保存PDF
    const fileName = `职业生涯规划报告_${new Date().toISOString().slice(0, 10)}.pdf`
    pdf.save(fileName)
    
    // 显示成功信息
    exportMessage.value = 'PDF报告导出成功！'
    setTimeout(() => {
      exportMessage.value = ''
      isExporting.value = false
    }, 3000)
  } catch (error) {
    console.error('导出PDF失败:', error)
    console.error('错误堆栈:', error.stack)
    exportMessage.value = `导出PDF失败: ${error.message}`
    setTimeout(() => {
      exportMessage.value = ''
      isExporting.value = false
    }, 5000)
  }
}

onUnmounted(() => {
  // Clean up
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  
  window.removeEventListener('resize', resizeCanvas)
})

// Starry background functions
function createMeteor() {
  if (!starCanvas.value) return
  
  meteors.push({
    x: Math.random() * starCanvas.value.width,
    y: -50,
    size: Math.random() * 1.5 + 0.5,
    speed: Math.random() * 10 + 5,
    length: Math.random() * 100 + 50,
    opacity: Math.random() * 0.8 + 0.3,
    angle: Math.random() * Math.PI / 6 + Math.PI / 4 // 45-75度角
  })
}

function createStars() {
  stars = []
  for (let i = 0; i < 500; i++) {
    stars.push({
      x: Math.random() * starCanvas.value.width,
      y: Math.random() * starCanvas.value.height,
      size: Math.random() * 2 + 1,
      speed: Math.random() * 0.5 + 0.1,
      opacity: Math.random() * 0.8 + 0.2
    })
  }
  
  // 初始化流星
  meteors = []
  for (let i = 0; i < 5; i++) {
    createMeteor()
  }
}

function animateStars() {
  if (!starCtx || !starCanvas.value) return
  
  starCtx.clearRect(0, 0, starCanvas.value.width, starCanvas.value.height)
  
  // 绘制星星
  stars.forEach(star => {
    starCtx.beginPath()
    starCtx.arc(star.x, star.y, star.size, 0, Math.PI * 2)
    starCtx.fillStyle = `rgba(255, 255, 255, ${star.opacity})`
    starCtx.fill()
    
    star.y -= star.speed
    if (star.y < 0) {
      star.y = starCanvas.value.height
    }
  })
  
  // 绘制流星
  meteors.forEach((meteor, index) => {
    // 计算流星轨迹
    const dx = Math.cos(meteor.angle) * meteor.speed
    const dy = Math.sin(meteor.angle) * meteor.speed
    
    // 绘制流星尾巴
    starCtx.beginPath()
    starCtx.moveTo(meteor.x, meteor.y)
    starCtx.lineTo(
      meteor.x - Math.cos(meteor.angle) * meteor.length,
      meteor.y - Math.sin(meteor.angle) * meteor.length
    )
    starCtx.strokeStyle = `rgba(255, 255, 255, ${meteor.opacity})`
    starCtx.lineWidth = meteor.size
    starCtx.stroke()
    
    // 绘制流星头部
    starCtx.beginPath()
    starCtx.arc(meteor.x, meteor.y, meteor.size * 1.5, 0, Math.PI * 2)
    starCtx.fillStyle = `rgba(255, 255, 255, ${meteor.opacity})`
    starCtx.fill()
    
    // 更新流星位置
    meteor.x += dx
    meteor.y += dy
    
    // 流星消失后重新创建
    if (meteor.x > starCanvas.value.width || meteor.y > starCanvas.value.height) {
      meteors.splice(index, 1)
      createMeteor()
    }
  })
  
  animationId = requestAnimationFrame(animateStars)
}

function resizeCanvas() {
  if (starCanvas.value) {
    starCanvas.value.width = starCanvas.value.offsetWidth
    starCanvas.value.height = starCanvas.value.offsetHeight
    createStars()
  }
}
</script>