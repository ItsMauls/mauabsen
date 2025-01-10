<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const attendances = ref([])
const loading = ref(false)
const error = ref(null)
const showFingerprintModal = ref(false)
const fingerprintId = ref('')
const actionType = ref('') // 'in' atau 'out'

// Initialize date range with empty values
const dateRange = ref({
  startDate: '',
  endDate: ''
})

const loadAllAttendances = async () => {
  loading.value = true
  error.value = null
  
  try {
    const response = await axios.get('/api/attendances')
    attendances.value = response.data
  } catch (err) {
    error.value = 'Failed to load attendance data: ' + (err.response?.data?.message || err.message)
    console.error('Error loading attendances:', err)
  } finally {
    loading.value = false
  }
}

const loadAttendancesByRange = async () => {
  loading.value = true
  error.value = null
  
  try {
    const response = await axios.get('/api/attendances/range', {
      params: {
        startDate: `${dateRange.value.startDate}T00:00:00`,
        endDate: `${dateRange.value.endDate}T23:59:59`
      }
    })
    attendances.value = response.data
  } catch (err) {
    error.value = 'Failed to load attendance data: ' + (err.response?.data?.message || err.message)
    console.error('Error loading attendances:', err)
  } finally {
    loading.value = false
  }
}

const handleDateRangeChange = () => {
  if (dateRange.value.startDate && dateRange.value.endDate) {
    loadAttendancesByRange()
  } else {
    loadAllAttendances()
  }
}

const openFingerprintModal = (type) => {
  actionType.value = type
  fingerprintId.value = ''
  showFingerprintModal.value = true
}

const handleAttendance = async () => {
  try {
    loading.value = true
    error.value = null

    if (actionType.value === 'in') {
      await axios.post('/api/attendances/clock-in', null, {
        params: { fingerprintId: fingerprintId.value }
      })
    } else {
      await axios.post('/api/attendances/clock-out', null, {
        params: { fingerprintId: fingerprintId.value }
      })
    }
    
    showFingerprintModal.value = false
    // Reload based on current filter state
    if (dateRange.value.startDate && dateRange.value.endDate) {
      await loadAttendancesByRange()
    } else {
      await loadAllAttendances()
    }
  } catch (err) {
    error.value = `Failed to clock ${actionType.value}: ` + (err.response?.data?.message || err.message)
    console.error(`Error clocking ${actionType.value}:`, err)
  } finally {
    loading.value = false
  }
}

// Load all attendances on mount
onMounted(() => {
  loadAllAttendances()
})
</script>

<template>
  <div class="min-h-screen p-4 sm:p-6 lg:p-8">
    <!-- Header Section -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-semibold text-gray-900">Attendance</h1>
        <p class="mt-1 text-sm text-gray-700">A list of all attendance records.</p>
      </div>
      <div class="flex flex-col sm:flex-row gap-2">
        <button
          @click="openFingerprintModal('in')"
          class="inline-flex items-center justify-center rounded-md border border-transparent bg-green-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-8.707l-3-3a1 1 0 00-1.414 0l-3 3a1 1 0 001.414 1.414L9 9.414V13a1 1 0 102 0V9.414l1.293 1.293a1 1 0 001.414-1.414z" clip-rule="evenodd" />
          </svg>
          Clock In
        </button>
        <button
          @click="openFingerprintModal('out')"
          class="inline-flex items-center justify-center rounded-md border border-transparent bg-red-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
          </svg>
          Clock Out
        </button>
      </div>
    </div>

    <!-- Date Range Filter -->
    <div class="mt-6 grid grid-cols-1 sm:grid-cols-2 gap-4">
      <div>
        <label for="startDate" class="block text-sm font-medium text-gray-700">Start Date</label>
        <input
          type="date"
          id="startDate"
          v-model="dateRange.startDate"
          @change="handleDateRangeChange"
          class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
        >
      </div>
      <div>
        <label for="endDate" class="block text-sm font-medium text-gray-700">End Date</label>
        <input
          type="date"
          id="endDate"
          v-model="dateRange.endDate"
          @change="handleDateRangeChange"
          :min="dateRange.startDate"
          class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
        >
      </div>
    </div>

    <!-- Info Message -->
    <div v-if="!dateRange.startDate || !dateRange.endDate" class="mt-4 p-4 bg-blue-50 rounded-md">
      <div class="flex items-start">
        <div class="flex-shrink-0">
          <svg class="h-5 w-5 text-blue-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd" />
          </svg>
        </div>
        <div class="ml-3">
          <h3 class="text-sm font-medium text-blue-800">
            Showing all attendance records. Select date range to filter.
          </h3>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="mt-8 flex justify-center">
      <div class="inline-flex items-center px-4 py-2 font-semibold leading-6 text-sm shadow rounded-md text-white bg-indigo-500">
        <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
        Loading...
      </div>
    </div>

    <!-- Attendance Table -->
    <div v-else class="mt-8 -mx-4 sm:-mx-6 lg:-mx-8">
      <div class="inline-block min-w-full align-middle">
        <div class="overflow-x-auto shadow-sm ring-1 ring-black ring-opacity-5">
          <table class="min-w-full divide-y divide-gray-300">
            <thead class="bg-gray-50">
              <tr>
                <th scope="col" class="py-3.5 pl-4 pr-3 text-left text-sm font-semibold text-gray-900 sm:pl-6 lg:pl-8">Employee</th>
                <th scope="col" class="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">Time In</th>
                <th scope="col" class="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">Time Out</th>
                <th scope="col" class="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">Status</th>
                <th scope="col" class="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">Date</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-200 bg-white">
              <tr v-for="attendance in attendances" :key="attendance.id" class="hover:bg-gray-50">
                <td class="whitespace-nowrap py-4 pl-4 pr-3 text-sm font-medium text-gray-900 sm:pl-6 lg:pl-8">
                  {{ attendance.employeeId }}
                </td>
                <td class="whitespace-nowrap px-3 py-4 text-sm text-gray-500">
                  {{ new Date(attendance.timeIn).toLocaleTimeString() }}
                </td>
                <td class="whitespace-nowrap px-3 py-4 text-sm text-gray-500">
                  {{ attendance.timeOut ? new Date(attendance.timeOut).toLocaleTimeString() : '-' }}
                </td>
                <td class="whitespace-nowrap px-3 py-4 text-sm">
                  <span :class="{
                    'px-2 py-1 rounded-full text-xs font-medium': true,
                    'bg-green-100 text-green-800': attendance.status === 'PRESENT',
                    'bg-red-100 text-red-800': attendance.status === 'LATE',
                    'bg-yellow-100 text-yellow-800': attendance.status === 'PENDING'
                  }">
                    {{ attendance.status }}
                  </span>
                </td>
                <td class="whitespace-nowrap px-3 py-4 text-sm text-gray-500">
                  {{ new Date(attendance.date).toLocaleDateString() }}
                </td>
              </tr>
              <tr v-if="attendances.length === 0">
                <td colspan="5" class="px-3 py-8 text-sm text-gray-500 text-center">
                  No attendance records found
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Fingerprint Modal -->
    <div v-if="showFingerprintModal" 
         class="fixed inset-0 bg-gray-500 bg-opacity-75 flex items-center justify-center p-4 z-50">
      <div class="bg-white rounded-lg shadow-xl max-w-md w-full mx-auto p-6">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-lg font-medium text-gray-900">
            Clock {{ actionType === 'in' ? 'In' : 'Out' }}
          </h3>
          <button @click="showFingerprintModal = false" class="text-gray-400 hover:text-gray-500">
            <span class="sr-only">Close</span>
            <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
        <form @submit.prevent="handleAttendance" class="space-y-4">
          <div>
            <label for="fingerprintId" class="block text-sm font-medium text-gray-700">
              Fingerprint ID
            </label>
            <input
              type="text"
              id="fingerprintId"
              v-model="fingerprintId"
              required
              class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
              placeholder="Enter your fingerprint ID"
            >
          </div>
          <div class="flex justify-end space-x-3">
            <button
              type="button"
              @click="showFingerprintModal = false"
              class="rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
            >
              Cancel
            </button>
            <button
              type="submit"
              class="rounded-md border border-transparent bg-indigo-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
            >
              Submit
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Error Alert -->
    <div 
      v-if="error" 
      class="fixed bottom-4 right-4 bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded-lg shadow-lg z-50"
      role="alert"
      @click="error = null"
    >
      <div class="flex">
        <div class="flex-shrink-0">
          <svg class="h-5 w-5 text-red-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
          </svg>
        </div>
        <div class="ml-3">
          <p class="text-sm">{{ error }}</p>
          <p class="text-xs mt-1">(Click to dismiss)</p>
        </div>
      </div>
    </div>
  </div>
</template> 