<template>
  <div class="career-grid-container">
    <div class="career-grid">
      <!-- 第一行 -->
      <div class="grid-item small">
        <div class="grid-item-content">
          <h3 class="grid-item-title">{{ careers[0]?.name }}</h3>
          <div class="market-demand">
            <span class="demand-label">市场需求:</span>
            <span class="demand-value">{{ careers[0]?.market_demand }}%</span>
          </div>
          <p class="grid-item-subtitle">{{ careers[0]?.description }}</p>
          <!-- 悬停显示的小类 -->
          <div class="subcategories-tooltip">
            <div class="subcategories-content">
              <h4 class="subcategories-title">职业小类</h4>
              <div class="subcategories-list">
                <div 
                  v-for="(subcategory, index) in careers[0]?.subcategories" 
                  :key="index" 
                  class="subcategory-item" 
                  :class="{ 'selected': isSubcategorySelected(careers[0]?.id, subcategory.id) }"
                  @click="toggleSubcategory(careers[0]?.id, subcategory.id, subcategory.name)"
                >
                  <span class="subcategory-name">{{ subcategory.name }}</span>
                  <span class="subcategory-demand">{{ subcategory.market_demand }}%</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="grid-item small">
        <div class="grid-item-content">
          <h3 class="grid-item-title">{{ careers[1]?.name }}</h3>
          <div class="market-demand">
            <span class="demand-label">市场需求:</span>
            <span class="demand-value">{{ careers[1]?.market_demand }}%</span>
          </div>
          <p class="grid-item-subtitle">{{ careers[1]?.description }}</p>
          <!-- 悬停显示的小类 -->
          <div class="subcategories-tooltip">
            <div class="subcategories-content">
              <h4 class="subcategories-title">职业小类</h4>
              <div class="subcategories-list">
                <div 
                  v-for="(subcategory, index) in careers[1]?.subcategories" 
                  :key="index" 
                  class="subcategory-item" 
                  :class="{ 'selected': isSubcategorySelected(careers[1]?.id, subcategory.id) }"
                  @click="toggleSubcategory(careers[1]?.id, subcategory.id, subcategory.name)"
                >
                  <span class="subcategory-name">{{ subcategory.name }}</span>
                  <span class="subcategory-demand">{{ subcategory.market_demand }}%</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="grid-item small">
        <div class="grid-item-content">
          <h3 class="grid-item-title">{{ careers[2]?.name }}</h3>
          <div class="market-demand">
            <span class="demand-label">市场需求:</span>
            <span class="demand-value">{{ careers[2]?.market_demand }}%</span>
          </div>
          <p class="grid-item-subtitle">{{ careers[2]?.description }}</p>
          <!-- 悬停显示的小类 -->
          <div class="subcategories-tooltip">
            <div class="subcategories-content">
              <h4 class="subcategories-title">职业小类</h4>
              <div class="subcategories-list">
                <div 
                  v-for="(subcategory, index) in careers[2]?.subcategories" 
                  :key="index" 
                  class="subcategory-item" 
                  :class="{ 'selected': isSubcategorySelected(careers[2]?.id, subcategory.id) }"
                  @click="toggleSubcategory(careers[2]?.id, subcategory.id, subcategory.name)"
                >
                  <span class="subcategory-name">{{ subcategory.name }}</span>
                  <span class="subcategory-demand">{{ subcategory.market_demand }}%</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 第二行 -->
      <div class="grid-item small">
        <div class="grid-item-content">
          <h3 class="grid-item-title">{{ careers[6]?.name }}</h3>
          <div class="market-demand">
            <span class="demand-label">市场需求:</span>
            <span class="demand-value">{{ careers[6]?.market_demand }}%</span>
          </div>
          <p class="grid-item-subtitle">{{ careers[6]?.description }}</p>
          <!-- 悬停显示的小类 -->
          <div class="subcategories-tooltip">
            <div class="subcategories-content">
              <h4 class="subcategories-title">职业小类</h4>
              <div class="subcategories-list">
                <div 
                  v-for="(subcategory, index) in careers[6]?.subcategories" 
                  :key="index" 
                  class="subcategory-item" 
                  :class="{ 'selected': isSubcategorySelected(careers[6]?.id, subcategory.id) }"
                  @click="toggleSubcategory(careers[6]?.id, subcategory.id, subcategory.name)"
                >
                  <span class="subcategory-name">{{ subcategory.name }}</span>
                  <span class="subcategory-demand">{{ subcategory.market_demand }}%</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="grid-item small">
        <div class="grid-item-content">
          <h3 class="grid-item-title">{{ careers[3]?.name }}</h3>
          <div class="market-demand">
            <span class="demand-label">市场需求:</span>
            <span class="demand-value">{{ careers[3]?.market_demand }}%</span>
          </div>
          <p class="grid-item-subtitle">{{ careers[3]?.description }}</p>
          <!-- 悬停显示的小类 -->
          <div class="subcategories-tooltip">
            <div class="subcategories-content">
              <h4 class="subcategories-title">职业小类</h4>
              <div class="subcategories-list">
                <div 
                  v-for="(subcategory, index) in careers[3]?.subcategories" 
                  :key="index" 
                  class="subcategory-item" 
                  :class="{ 'selected': isSubcategorySelected(careers[3]?.id, subcategory.id) }"
                  @click="toggleSubcategory(careers[3]?.id, subcategory.id, subcategory.name)"
                >
                  <span class="subcategory-name">{{ subcategory.name }}</span>
                  <span class="subcategory-demand">{{ subcategory.market_demand }}%</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="grid-item small">
        <div class="grid-item-content">
          <h3 class="grid-item-title">{{ careers[4]?.name }}</h3>
          <div class="market-demand">
            <span class="demand-label">市场需求:</span>
            <span class="demand-value">{{ careers[4]?.market_demand }}%</span>
          </div>
          <p class="grid-item-subtitle">{{ careers[4]?.description }}</p>
          <!-- 悬停显示的小类 -->
          <div class="subcategories-tooltip">
            <div class="subcategories-content">
              <h4 class="subcategories-title">职业小类</h4>
              <div class="subcategories-list">
                <div 
                  v-for="(subcategory, index) in careers[4]?.subcategories" 
                  :key="index" 
                  class="subcategory-item" 
                  :class="{ 'selected': isSubcategorySelected(careers[4]?.id, subcategory.id) }"
                  @click="toggleSubcategory(careers[4]?.id, subcategory.id, subcategory.name)"
                >
                  <span class="subcategory-name">{{ subcategory.name }}</span>
                  <span class="subcategory-demand">{{ subcategory.market_demand }}%</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 第三行 -->
      <div class="grid-item small">
        <div class="grid-item-content">
          <h3 class="grid-item-title">{{ careers[5]?.name }}</h3>
          <div class="market-demand">
            <span class="demand-label">市场需求:</span>
            <span class="demand-value">{{ careers[5]?.market_demand }}%</span>
          </div>
          <p class="grid-item-subtitle">{{ careers[5]?.description }}</p>
          <!-- 悬停显示的小类 -->
          <div class="subcategories-tooltip">
            <div class="subcategories-content">
              <h4 class="subcategories-title">职业小类</h4>
              <div class="subcategories-list">
                <div 
                  v-for="(subcategory, index) in careers[5]?.subcategories" 
                  :key="index" 
                  class="subcategory-item" 
                  :class="{ 'selected': isSubcategorySelected(careers[5]?.id, subcategory.id) }"
                  @click="toggleSubcategory(careers[5]?.id, subcategory.id, subcategory.name)"
                >
                  <span class="subcategory-name">{{ subcategory.name }}</span>
                  <span class="subcategory-demand">{{ subcategory.market_demand }}%</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="grid-item small">
        <div class="grid-item-content">
          <h3 class="grid-item-title">{{ careers[7]?.name }}</h3>
          <div class="market-demand">
            <span class="demand-label">市场需求:</span>
            <span class="demand-value">{{ careers[7]?.market_demand }}%</span>
          </div>
          <p class="grid-item-subtitle">{{ careers[7]?.description }}</p>
          <!-- 悬停显示的小类 -->
          <div class="subcategories-tooltip">
            <div class="subcategories-content">
              <h4 class="subcategories-title">职业小类</h4>
              <div class="subcategories-list">
                <div 
                  v-for="(subcategory, index) in careers[7]?.subcategories" 
                  :key="index" 
                  class="subcategory-item" 
                  :class="{ 'selected': isSubcategorySelected(careers[7]?.id, subcategory.id) }"
                  @click="toggleSubcategory(careers[7]?.id, subcategory.id, subcategory.name)"
                >
                  <span class="subcategory-name">{{ subcategory.name }}</span>
                  <span class="subcategory-demand">{{ subcategory.market_demand }}%</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="grid-item small">
        <div class="grid-item-content">
          <h3 class="grid-item-title">{{ careers[8]?.name }}</h3>
          <div class="market-demand">
            <span class="demand-label">市场需求:</span>
            <span class="demand-value">{{ careers[8]?.market_demand }}%</span>
          </div>
          <p class="grid-item-subtitle">{{ careers[8]?.description }}</p>
          <!-- 悬停显示的小类 -->
          <div class="subcategories-tooltip">
            <div class="subcategories-content">
              <h4 class="subcategories-title">职业小类</h4>
              <div class="subcategories-list">
                <div 
                  v-for="(subcategory, index) in careers[8]?.subcategories" 
                  :key="index" 
                  class="subcategory-item" 
                  :class="{ 'selected': isSubcategorySelected(careers[8]?.id, subcategory.id) }"
                  @click="toggleSubcategory(careers[8]?.id, subcategory.id, subcategory.name)"
                >
                  <span class="subcategory-name">{{ subcategory.name }}</span>
                  <span class="subcategory-demand">{{ subcategory.market_demand }}%</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 选择的小类显示 -->
    <div v-if="selectedSubcategoryNames.length > 0" class="selected-subcategories">
      <h4 class="selected-title">已选择的职业小类:</h4>
      <div class="selected-list">
        <span 
          v-for="(name, index) in selectedSubcategoryNames" 
          :key="index" 
          class="selected-item"
        >
          {{ name }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits } from 'vue'
import api from '@/api'

// 接收职业数据作为 props
const props = defineProps({
  careers: {
    type: Array,
    default: () => []
  }
})

// 定义事件
const emit = defineEmits(['subcategory-selected'])

// 选中的小类ID，格式为：categoryId_subcategoryId
const selectedSubcategories = ref([])
// 选中的小类名称
const selectedSubcategoryNames = ref([])

// 判断小类是否被选中
const isSubcategorySelected = (categoryId, subcategoryId) => {
  if (!categoryId || !subcategoryId) return false;
  return selectedSubcategories.value.includes(`${categoryId}_${subcategoryId}`);
}

// 切换小类选择状态
const toggleSubcategory = (categoryId, subcategoryId, subcategoryName) => {
  if (!categoryId || !subcategoryId) return;
  
  const idStr = `${categoryId}_${subcategoryId}`;
  const index = selectedSubcategories.value.indexOf(idStr)
  
  if (index > -1) {
    // 取消选择
    selectedSubcategories.value.splice(index, 1)
    selectedSubcategoryNames.value.splice(index, 1)
  } else {
    // 选择
    selectedSubcategories.value.push(idStr)
    selectedSubcategoryNames.value.push(subcategoryName)
  }
  
  // 保存选择的职业到localStorage
  localStorage.setItem('selectedCareers', JSON.stringify(selectedSubcategoryNames.value))
  
  // 保存职业评测记录到后端
  saveCareerEvaluation()
  
  // 触发事件，通知父组件
  emit('subcategory-selected', {
    ids: selectedSubcategories.value,
    names: selectedSubcategoryNames.value
  })
}

// 保存职业评测记录到后端
const saveCareerEvaluation = async () => {
  try {
    const sessionId = 'eval_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
    
    const response = await api.post('/ai/records/save-evaluation/', {
      session_id: sessionId,
      selected_careers: selectedSubcategoryNames.value,
      evaluation_result: {
        selected_subcategories: selectedSubcategories.value,
        selected_careers: selectedSubcategoryNames.value,
        timestamp: new Date().toISOString()
      }
    })
    
    console.log('职业评测记录保存成功', response)
  } catch (error) {
    console.error('保存职业评测记录失败:', error)
  }
}
</script>

<style scoped>
.career-grid-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background-color: transparent;
}

.career-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: repeat(3, auto);
  gap: 16px;
  grid-template-areas:
    "small1 small2 small3"
    "small4 small5 small6"
    "small7 small8 small9";
}

.grid-item {
  background-color: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(147, 51, 234, 0.2);
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s ease;
  display: flex;
  align-items: flex-start;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.grid-item:hover {
  border-color: rgba(147, 51, 234, 0.4);
  box-shadow: 0 10px 15px -3px rgba(147, 51, 234, 0.2), 0 4px 6px -2px rgba(147, 51, 234, 0.1);
  transform: translateY(-2px);
  background-color: rgba(255, 255, 255, 0.4);
}

.grid-item-content {
  width: 100%;
}

.grid-item-title {
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 8px;
  text-align: left;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.grid-item-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.5;
  text-align: left;
  text-shadow: 0 1px 1px rgba(0, 0, 0, 0.3);
}

/* 网格布局区域分配 */
.small:nth-child(1) {
  grid-area: small1;
}

.small:nth-child(2) {
  grid-area: small2;
}

.small:nth-child(3) {
  grid-area: small3;
}

.small:nth-child(4) {
  grid-area: small4;
}

.small:nth-child(5) {
  grid-area: small5;
}

.small:nth-child(6) {
  grid-area: small6;
}

.small:nth-child(7) {
  grid-area: small7;
}

.small:nth-child(8) {
  grid-area: small8;
}

.small:nth-child(9) {
  grid-area: small9;
}

.subcategories-tooltip {
  position: relative;
  display: inline-block;
  width: 100%;
}

.subcategories-content {
  visibility: hidden;
  position: absolute;
  bottom: 100%;
  left: 0;
  right: 0;
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(147, 51, 234, 0.2);
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.2), 0 4px 6px -2px rgba(0, 0, 0, 0.1);
  z-index: 10;
  max-height: 300px;
  overflow-y: auto;
  margin-bottom: 8px;
  transition: visibility 0.3s ease, opacity 0.3s ease;
  opacity: 0;
}

.grid-item:hover .subcategories-content {
  visibility: visible;
  opacity: 1;
}

.subcategories-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 12px;
  text-align: left;
}

.subcategories-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.subcategory-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
  background-color: rgba(248, 250, 252, 0.8);
  border-radius: 6px;
  transition: all 0.2s ease;
}

.subcategory-item:hover {
  background-color: rgba(241, 245, 249, 0.9);
  transform: translateX(4px);
}

.subcategory-name {
  font-size: 13px;
  color: #1e293b;
  font-weight: 500;
}

.subcategory-demand {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
  background-color: #e2e8f0;
  padding: 2px 8px;
  border-radius: 10px;
}

/* 市场需求样式 */
.market-demand {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 8px 0;
  font-size: 14px;
}

.demand-label {
  color: rgba(255, 255, 255, 0.7);
  font-weight: 500;
  text-shadow: 0 1px 1px rgba(0, 0, 0, 0.3);
}

.demand-value {
  color: #ffffff;
  font-weight: 600;
  background-color: rgba(147, 51, 234, 0.2);
  padding: 2px 8px;
  border-radius: 10px;
  text-shadow: 0 1px 1px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(147, 51, 234, 0.3);
}

/* 小类选择样式 */
.subcategory-item {
  cursor: pointer;
  transition: all 0.2s ease;
}

.subcategory-item:hover {
  background-color: rgba(241, 245, 249, 0.9);
  transform: translateX(4px);
}

.subcategory-item.selected {
  background-color: rgba(237, 233, 254, 0.9);
  border-left: 3px solid #9333ea;
}

/* 已选择小类显示样式 */
.selected-subcategories {
  margin-top: 32px;
  padding: 20px;
  background-color: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(12px);
  border-radius: 10px;
  border: 1px solid rgba(147, 51, 234, 0.2);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.selected-title {
  font-size: 16px;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 12px;
  text-align: left;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.selected-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.selected-item {
  background-color: rgba(237, 233, 254, 0.8);
  color: #7e22ce;
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.selected-item:hover {
  background-color: rgba(221, 214, 254, 0.9);
  transform: translateY(-2px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

/* 移动端响应式 */
@media (max-width: 768px) {
  .career-grid {
    grid-template-columns: 1fr;
    grid-template-areas:
      "small1"
      "small2"
      "small3"
      "small4"
      "small5"
      "small6"
      "small7"
      "small8"
      "small9";
  }
  
  .subcategories-content {
    position: static;
    visibility: visible;
    opacity: 1;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    border: 1px solid rgba(147, 51, 234, 0.2);
    margin-top: 12px;
    max-height: 200px;
  }
}
</style>