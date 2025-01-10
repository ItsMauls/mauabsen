<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const stats = ref({
  totalEmployees: 0,
  todayAttendance: 0,
  onTimePercentage: 0,
  latePercentage: 0
})

const loadDashboardStats = async () => {
  try {
    // This is a placeholder - you'll need to implement the actual endpoint
    const response = await axios.get('/api/dashboard/stats')
    stats.value = response.data
  } catch (error) {
    console.error('Error loading dashboard stats:', error)
  }
}

onMounted(() => {
  loadDashboardStats()
})
</script>

<template>
  <div>
    <h1 class="text-2xl font-semibold text-gray-900">Dashboard</h1>
    
    <div class="mt-6 grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
      <!-- Total Employees Card -->
      <div class="bg-white overflow-hidden shadow rounded-lg">
        <div class="p-5">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <svg class="h-6 w-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
            </div>
            <div class="ml-5 w-0 flex-1">
              <dl>
                <dt class="text-sm font-medium text-gray-500 truncate">Total Employees</dt>
                <dd class="text-lg font-semibold text-gray-900">{{ stats.totalEmployees }}</dd>
              </dl>
            </div>
          </div>
        </div>
      </div>

      <!-- Today's Attendance Card -->
      <div class="bg-white overflow-hidden shadow rounded-lg">
        <div class="p-5">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <svg class="h-6 w-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
            <div class="ml-5 w-0 flex-1">
              <dl>
                <dt class="text-sm font-medium text-gray-500 truncate">Today's Attendance</dt>
                <dd class="text-lg font-semibold text-gray-900">{{ stats.todayAttendance }}</dd>
              </dl>
            </div>
          </div>
        </div>
      </div>

      <!-- On Time Percentage Card -->
      <div class="bg-white overflow-hidden shadow rounded-lg">
        <div class="p-5">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <svg class="h-6 w-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <div class="ml-5 w-0 flex-1">
              <dl>
                <dt class="text-sm font-medium text-gray-500 truncate">On Time Percentage</dt>
                <dd class="text-lg font-semibold text-gray-900">{{ stats.onTimePercentage }}%</dd>
              </dl>
            </div>
          </div>
        </div>
      </div>

      <!-- Late Percentage Card -->
      <div class="bg-white overflow-hidden shadow rounded-lg">
        <div class="p-5">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <svg class="h-6 w-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <div class="ml-5 w-0 flex-1">
              <dl>
                <dt class="text-sm font-medium text-gray-500 truncate">Late Percentage</dt>
                <dd class="text-lg font-semibold text-gray-900">{{ stats.latePercentage }}%</dd>
              </dl>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template> 