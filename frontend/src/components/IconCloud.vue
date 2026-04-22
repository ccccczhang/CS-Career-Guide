<template>
  <div class="relative w-full h-full">
    <div ref="container" class="relative w-full h-full">
      <img
        v-for="(image, index) in images"
        :key="index"
        :src="image"
        :alt="`Icon ${index}`"
        class="absolute w-8 h-8 object-contain transition-transform duration-300 ease-out"
        :style="getIconStyle(index)"
        @mouseenter="hoveredIndex = index"
        @mouseleave="hoveredIndex = null"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  images: {
    type: Array,
    required: true
  }
})

const container = ref(null)
const hoveredIndex = ref(null)
const iconPositions = ref([])
let animationFrameId = null

function getIconStyle(index) {
  const position = iconPositions.value[index] || { x: 0, y: 0, z: 0, rotation: 0 }
  const scale = hoveredIndex.value === index ? 1.2 : 1
  return {
    left: `${position.x}%`,
    top: `${position.y}%`,
    transform: `translate(-50%, -50%) rotate(${position.rotation}deg) scale(${scale})`,
    zIndex: position.z
  }
}

function generatePositions() {
  if (!container.value) return
  
  const centerX = 50
  const centerY = 50
  const radius = 40
  const positions = []
  
  props.images.forEach((_, index) => {
    const angle = (index / props.images.length) * Math.PI * 2
    const distance = radius * (0.7 + Math.random() * 0.3)
    const x = centerX + Math.cos(angle) * distance
    const y = centerY + Math.sin(angle) * distance
    const z = Math.floor(Math.random() * 10)
    const rotation = Math.random() * 360
    
    positions.push({ x, y, z, rotation })
  })
  
  iconPositions.value = positions
}

function animate() {
  iconPositions.value = iconPositions.value.map(position => ({
    ...position,
    rotation: position.rotation + 0.5,
    y: position.y + Math.sin(Date.now() * 0.001 + position.x) * 0.1
  }))
  
  animationFrameId = requestAnimationFrame(animate)
}

onMounted(() => {
  generatePositions()
  animate()
  window.addEventListener('resize', generatePositions)
})

onUnmounted(() => {
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
  }
  window.removeEventListener('resize', generatePositions)
})
</script>

<style scoped>
img {
  filter: grayscale(0.3);
  transition: all 0.3s ease;
}

img:hover {
  filter: grayscale(0);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
</style>