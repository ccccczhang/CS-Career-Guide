<template>
  <canvas ref="canvas" class="w-full h-full"></canvas>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'

const props = defineProps({
  quantity: {
    type: Number,
    default: 100
  },
  ease: {
    type: Number,
    default: 100
  },
  color: {
    type: String,
    default: '#000'
  },
  staticity: {
    type: Number,
    default: 10
  },
  refresh: {
    type: Boolean,
    default: false
  }
})

const canvas = ref(null)
let ctx = null
let particles = []
let animationId = null

function createParticle() {
  return {
    x: Math.random() * canvas.value.width,
    y: Math.random() * canvas.value.height,
    size: Math.random() * 3 + 1,
    speedX: (Math.random() - 0.5) * 2,
    speedY: (Math.random() - 0.5) * 2,
    color: props.color
  }
}

function initParticles() {
  particles = []
  for (let i = 0; i < props.quantity; i++) {
    particles.push(createParticle())
  }
}

function animate() {
  ctx.clearRect(0, 0, canvas.value.width, canvas.value.height)
  
  particles.forEach(particle => {
    ctx.beginPath()
    ctx.arc(particle.x, particle.y, particle.size, 0, Math.PI * 2)
    ctx.fillStyle = particle.color
    ctx.fill()
    
    particle.x += particle.speedX
    particle.y += particle.speedY
    
    if (particle.x < 0 || particle.x > canvas.value.width) {
      particle.speedX *= -1
    }
    if (particle.y < 0 || particle.y > canvas.value.height) {
      particle.speedY *= -1
    }
  })
  
  animationId = requestAnimationFrame(animate)
}

function resizeCanvas() {
  if (canvas.value) {
    canvas.value.width = canvas.value.offsetWidth
    canvas.value.height = canvas.value.offsetHeight
  }
}

onMounted(() => {
  if (canvas.value) {
    ctx = canvas.value.getContext('2d')
    resizeCanvas()
    initParticles()
    animate()
    window.addEventListener('resize', resizeCanvas)
  }
})

onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  window.removeEventListener('resize', resizeCanvas)
})

watch(() => props.refresh, () => {
  initParticles()
})

watch(() => props.color, () => {
  particles.forEach(particle => {
    particle.color = props.color
  })
})
</script>

<style scoped>
canvas {
  display: block;
}
</style>