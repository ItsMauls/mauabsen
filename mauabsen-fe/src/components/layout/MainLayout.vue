<script setup>
import { RouterLink, useRouter } from 'vue-router'
import { ref } from 'vue'

const router = useRouter()
const isMenuOpen = ref(false)

const logout = () => {
  localStorage.removeItem('token')
  router.push('/login')
}
</script>

<template>
  <div class="min-h-screen bg-gray-100 flex flex-col">
    <!-- Navigation -->
    <nav class="bg-white shadow-lg">
      <div class="max-w-7xl mx-auto px-4">
        <div class="flex justify-between h-16">
          <div class="flex">
            <div class="flex-shrink-0 flex items-center">
              <h1 class="text-xl font-bold text-gray-800">MauAbsen</h1>
            </div>
            <div class="hidden sm:ml-6 sm:flex sm:space-x-8">
              <RouterLink 
                to="/" 
                class="border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium"
              >
                Dashboard
              </RouterLink>
              <RouterLink 
                to="/attendance" 
                class="border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium"
              >
                Attendance
              </RouterLink>
              <RouterLink 
                to="/employees" 
                class="border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium"
              >
                Employees
              </RouterLink>
            </div>
          </div>
          <div class="flex items-center">
            <button
              @click="logout"
              class="bg-red-500 hover:bg-red-700 text-white font-bold py-2 px-4 rounded"
            >
              Logout
            </button>
          </div>
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <main class="flex-1">
      <div class="max-w-7xl mx-auto">
        <slot></slot>
      </div>
    </main>
  </div>
</template> 