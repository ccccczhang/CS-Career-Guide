<template>
  <button
    class="relative group"
    :class="buttonClasses"
    @click="$emit('click')"
  >
    <span class="relative z-10 flex items-center justify-center gap-2">
      <slot></slot>
    </span>
    <span class="absolute inset-0 rounded-full bg-white opacity-0 group-hover:opacity-20 transition-opacity duration-300"></span>
    <span class="absolute -inset-0.5 rounded-full bg-gradient-to-r from-white to-gray-200 blur opacity-0 group-hover:opacity-100 transition-all duration-500"></span>
  </button>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  variant: {
    type: String,
    default: 'primary',
    validator: (value) => ['primary', 'secondary', 'outline', 'black'].includes(value)
  },
  size: {
    type: String,
    default: 'md',
    validator: (value) => ['sm', 'md', 'lg'].includes(value)
  },
  fullWidth: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click'])

const buttonClasses = computed(() => {
  const baseClasses = [
    'px-6 py-3 rounded-full font-medium transition-all duration-300 relative overflow-hidden',
    'transform group-hover:scale-105 group-hover:shadow-lg',
    'focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2'
  ]
  
  // Variant classes
  if (props.variant === 'primary') {
    baseClasses.push('bg-primary text-white group-hover:bg-white group-hover:text-black')
  } else if (props.variant === 'secondary') {
    baseClasses.push('bg-secondary text-white group-hover:bg-white group-hover:text-black')
  } else if (props.variant === 'outline') {
    baseClasses.push('bg-transparent border border-primary text-primary group-hover:bg-white group-hover:text-black')
  } else if (props.variant === 'black') {
    baseClasses.push('bg-black text-white group-hover:bg-white group-hover:text-black')
  }
  
  // Size classes
  if (props.size === 'sm') {
    baseClasses.push('px-4 py-2 text-sm')
  } else if (props.size === 'lg') {
    baseClasses.push('px-8 py-4 text-lg')
  }
  
  // Full width
  if (props.fullWidth) {
    baseClasses.push('w-full')
  }
  
  return baseClasses
})
</script>

<style scoped>
/* 可以添加额外的样式 */
</style>