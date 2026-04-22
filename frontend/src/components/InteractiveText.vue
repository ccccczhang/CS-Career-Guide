<template>
  <div class="interactive-text-container" ref="containerRef">
    <span 
      v-for="(char, index) in text.split('')" 
      :key="index"
      class="interactive-char"
      :style="getCharStyle(index)"
    >
      {{ char }}
    </span>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'

// 组件属性
const props = defineProps({
  text: {
    type: String,
    default: 'galaxy'
  },
  minFontSize: {
    type: Number,
    default: 96
  },
  color: {
    type: String,
    default: '#ffffff'
  }
})

// 响应式数据
const containerRef = ref(null)
const mousePos = ref({ x: 0, y: 0 })
const targetMousePos = ref({ x: 0, y: 0 })
const charPositions = ref([])
const animationId = ref(null)

// 计算每个字符的样式
const getCharStyle = (index) => {
  if (!charPositions.value[index]) return {}
  
  const charPos = charPositions.value[index]
  const distance = Math.sqrt(
    Math.pow(mousePos.value.x - charPos.x, 2) + Math.pow(mousePos.value.y - charPos.y, 2)
  )
  
  // 计算影响范围
  const maxDistance = 500
  const influence = Math.max(0, 1 - distance / maxDistance)
  
  // 计算字体变化
  const weight = Math.round(100 + influence * 900) // 100-1000
  const width = Math.round(200 - influence * 130) // 200-70
  const italic = influence // 0-1
  const opacity = 1 - influence * 0.9 // 1-0.1
  
  return {
    color: props.color,
    opacity: opacity,
    fontWeight: weight,
    fontVariationSettings: `'wght' ${weight}, 'wdth' ${width}, 'ital' ${italic}`
  }
}

// 更新字符位置
const updateCharPositions = () => {
  if (!containerRef.value) return
  
  const chars = containerRef.value.querySelectorAll('.interactive-char')
  const newPositions = []
  
  chars.forEach(char => {
    const rect = char.getBoundingClientRect()
    const containerRect = containerRef.value.getBoundingClientRect()
    newPositions.push({
      x: rect.left - containerRect.left + rect.width / 2,
      y: rect.top - containerRect.top + rect.height / 2
    })
  })
  
  charPositions.value = newPositions
}

// 动画循环
const animate = () => {
  // 更平滑的缓动跟随
  const easingFactor = 0.05
  mousePos.value.x += (targetMousePos.value.x - mousePos.value.x) * easingFactor
  mousePos.value.y += (targetMousePos.value.y - mousePos.value.y) * easingFactor
  
  animationId.value = requestAnimationFrame(animate)
}

// 鼠标移动处理
const handleMouseMove = (e) => {
  if (!containerRef.value) return
  
  const rect = containerRef.value.getBoundingClientRect()
  targetMousePos.value = {
    x: e.clientX - rect.left,
    y: e.clientY - rect.top
  }
}

// 防抖函数
const debounce = (func, delay) => {
  let timeoutId
  return (...args) => {
    clearTimeout(timeoutId)
    timeoutId = setTimeout(() => func.apply(null, args), delay)
  }
}

// 窗口大小变化处理（防抖）
const handleResize = debounce(() => {
  updateCharPositions()
}, 100)

// 生命周期钩子
onMounted(() => {
  // 初始化字符位置
  updateCharPositions()
  
  // 开始动画
  animate()
  
  // 添加事件监听
  window.addEventListener('mousemove', handleMouseMove)
  window.addEventListener('resize', handleResize)
  
  // 定期更新字符位置（处理字体大小变化等情况）
  setInterval(updateCharPositions, 1000)
})

onUnmounted(() => {
  // 清理
  if (animationId.value) {
    cancelAnimationFrame(animationId.value)
  }
  window.removeEventListener('mousemove', handleMouseMove)
  window.removeEventListener('resize', handleResize)
})

// 监听文字变化
watch(() => props.text, () => {
  // 延迟更新字符位置，确保DOM已经更新
  setTimeout(updateCharPositions, 0)
})
</script>

<style scoped>
/* 确保可变字体可用 */
@import url('https://fonts.googleapis.com/css2?family=Inter:ital,wght@0,100..900;1,100..900&display=swap');

.interactive-text-container {
  position: relative;
  display: inline-block;
  font-family: 'Inter Variable', 'Inter', sans-serif;
  font-size: v-bind('minFontSize + "px"');
  line-height: 1.2;
  white-space: nowrap;
}

.interactive-char {
  display: inline-block;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  font-variation-settings: 'wght' 400, 'wdth' 100, 'ital' 0;
}

/* 确保字体正确加载 */

/* 响应式调整 */
@media (max-width: 768px) {
  .interactive-text-container {
    font-size: calc(v-bind('minFontSize') * 0.8px);
  }
}

@media (max-width: 480px) {
  .interactive-text-container {
    font-size: calc(v-bind('minFontSize') * 0.6px);
  }
}
</style>