<template>
  <div class="relative w-full h-full perspective-1000">
    <div 
      class="relative h-full w-full transition-transform duration-700 ease-in-out transform-style-preserve-3d cursor-pointer"
      :class="{ 'rotate-y-180': isFlipped }"
      @click="handleClick"
      @transitionend="handleTransitionEnd"
    >
      <!-- Front -->
      <div class="absolute inset-0 backface-hidden">
        <slot></slot>
      </div>
      <!-- Back -->
      <div class="absolute inset-0 backface-hidden rotate-y-180">
        <slot name="back"></slot>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const isFlipped = ref(false)
const isAnimating = ref(false)
const cooldown = ref(false)

function handleClick() {
  if (!isAnimating.value && !cooldown.value) {
    isAnimating.value = true
    isFlipped.value = !isFlipped.value
  }
}

function handleTransitionEnd() {
  isAnimating.value = false
  
  // 添加冷却时间，防止频繁触发
  cooldown.value = true
  setTimeout(() => {
    cooldown.value = false
  }, 300) // 300ms 冷却时间
}
</script>

<style scoped>
.perspective-1000 {
  perspective: 1000px;
}

.transform-style-preserve-3d {
  transform-style: preserve-3d;
}

.backface-hidden {
  backface-visibility: hidden;
}

.rotate-y-180 {
  transform: rotateY(180deg);
}
</style>