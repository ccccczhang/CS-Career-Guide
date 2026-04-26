<template>
  <div class="bg-white rounded-3xl p-6 shadow-lg">
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center gap-3">
        <div class="w-12 h-12 bg-gradient-to-br from-blue-500 to-cyan-500 rounded-xl flex items-center justify-center">
          <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path>
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path>
          </svg>
        </div>
        <div>
          <h3 class="text-lg font-bold text-gray-800">
            {{ currentCity ? `${currentCity}职位` : '地区招聘资讯' }}
          </h3>
          <p class="text-sm text-gray-500">{{ jobs.length }} 个相关职位</p>
        </div>
      </div>
      <button
        @click="$emit('change-city')"
        class="flex items-center gap-1 px-4 py-2 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors text-sm font-medium text-gray-700"
      >
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
        </svg>
        切换城市
      </button>
    </div>

    <div v-if="loading" class="flex items-center justify-center py-12">
      <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-500"></div>
    </div>

    <div v-else-if="jobs.length === 0" class="text-center py-12">
      <div class="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
        <svg class="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
        </svg>
      </div>
      <p class="text-gray-500">暂无{{ currentCity ? currentCity : '该地区' }}的职位信息</p>
      <button
        @click="$emit('change-city')"
        class="mt-4 px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
      >
        选择其他城市
      </button>
    </div>

    <div v-else class="space-y-4">
      <div
        v-for="job in jobs.slice(0, 5)"
        :key="job.id"
        class="p-4 bg-gray-50 rounded-xl hover:bg-gray-100 transition-colors cursor-pointer group"
      >
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <h4 class="font-semibold text-gray-800 group-hover:text-blue-600 transition-colors">{{ job.position_name }}</h4>
            <p class="text-sm text-gray-600">{{ job.company_name }}</p>
            <div class="flex items-center gap-3 mt-2">
              <span class="flex items-center gap-1 text-xs text-gray-500">
                <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path>
                </svg>
                {{ job.address }}
              </span>
              <span v-if="job.salary_range" class="text-xs font-medium text-blue-600">{{ job.salary_range }}</span>
            </div>
          </div>
          <svg class="w-5 h-5 text-gray-400 group-hover:text-blue-500 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
          </svg>
        </div>
      </div>

      <div v-if="jobs.length > 5" class="text-center pt-4">
        <button class="text-blue-600 hover:text-blue-700 font-medium text-sm">
          查看全部 {{ jobs.length }} 个职位
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { computerCareerAPI } from '@/api'

const props = defineProps({
  currentCity: String
})

defineEmits(['change-city'])

const jobs = ref([])
const loading = ref(false)

const loadJobs = async (city) => {
  if (!city) return
  
  loading.value = true
  try {
    const response = await computerCareerAPI.getComputerCareers(1, 20)
    if (response.results) {
      jobs.value = response.results.filter(job => 
        job.address && job.address.includes(city)
      )
    }
  } catch (error) {
    console.error('加载职位数据失败:', error)
    jobs.value = []
  } finally {
    loading.value = false
  }
}

watch(() => props.currentCity, (newCity) => {
  loadJobs(newCity)
})

onMounted(() => {
  loadJobs(props.currentCity)
})
</script>