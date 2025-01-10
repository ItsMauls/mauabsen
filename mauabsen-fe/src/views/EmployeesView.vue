<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const employees = ref([])
const loading = ref(false)
const error = ref(null)
const showAddModal = ref(false)
const showKycModal = ref(false)
const selectedEmployee = ref(null)

const newEmployee = ref({
  username: '',
  email: '',
  password: '',
  role: 'USER'
})

const kycData = ref({
  firstName: '',
  lastName: '',
  address: '',
  fingerprintId: '',
  photoUrl: ''
})

const loadEmployees = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/employees')
    employees.value = response.data
  } catch (err) {
    error.value = 'Failed to load employees'
    console.error('Error loading employees:', err)
  } finally {
    loading.value = false
  }
}

const createEmployee = async () => {
  try {
    await axios.post('/api/employees', newEmployee.value)
    showAddModal.value = false
    await loadEmployees()
    newEmployee.value = {
      username: '',
      email: '',
      password: '',
      role: 'USER'
    }
  } catch (err) {
    error.value = 'Failed to create employee'
    console.error('Error creating employee:', err)
  }
}

const openKycModal = (employee) => {
  selectedEmployee.value = employee
  showKycModal.value = true
  kycData.value = {
    firstName: '',
    lastName: '',
    address: '',
    fingerprintId: '',
    photoUrl: ''
  }
}

const registerKyc = async () => {
  try {
    await axios.post(`/api/employees/${selectedEmployee.value.id}/kyc`, {
      employeeId: selectedEmployee.value.id,
      ...kycData.value
    })
    showKycModal.value = false
    await loadEmployees()
  } catch (err) {
    error.value = 'Failed to register KYC'
    console.error('Error registering KYC:', err)
  }
}

onMounted(() => {
  loadEmployees()
})
</script>

<template>
  <div>
    <div class="sm:flex sm:items-center">
      <div class="sm:flex-auto">
        <h1 class="text-2xl font-semibold text-gray-900">Employees</h1>
        <p class="mt-2 text-sm text-gray-700">A list of all employees in the system.</p>
      </div>
      <div class="mt-4 sm:mt-0 sm:ml-16 sm:flex-none">
        <button
          @click="showAddModal = true"
          class="inline-flex items-center justify-center rounded-md border border-transparent bg-indigo-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 sm:w-auto"
        >
          Add Employee
        </button>
      </div>
    </div>

    <!-- Employee List -->
    <div class="mt-8 flex flex-col">
      <div class="-my-2 -mx-4 overflow-x-auto sm:-mx-6 lg:-mx-8">
        <div class="inline-block min-w-full py-2 align-middle md:px-6 lg:px-8">
          <div class="overflow-hidden shadow ring-1 ring-black ring-opacity-5 md:rounded-lg">
            <table class="min-w-full divide-y divide-gray-300">
              <thead class="bg-gray-50">
                <tr>
                  <th scope="col" class="py-3.5 pl-4 pr-3 text-left text-sm font-semibold text-gray-900">Name</th>
                  <th scope="col" class="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">Email</th>
                  <th scope="col" class="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">Role</th>
                  <th scope="col" class="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">Status</th>
                  <th scope="col" class="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">Actions</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-200 bg-white">
                <tr v-for="employee in employees" :key="employee.id">
                  <td class="whitespace-nowrap py-4 pl-4 pr-3 text-sm font-medium text-gray-900">
                    {{ employee.username }}
                  </td>
                  <td class="whitespace-nowrap px-3 py-4 text-sm text-gray-500">{{ employee.email }}</td>
                  <td class="whitespace-nowrap px-3 py-4 text-sm text-gray-500">{{ employee.role }}</td>
                  <td class="whitespace-nowrap px-3 py-4 text-sm text-gray-500">
                    {{ employee.employeeKyc?.status || 'Pending' }}
                  </td>
                  <td class="whitespace-nowrap px-3 py-4 text-sm text-gray-500">
                    <button
                      v-if="!employee.employeeKyc"
                      @click="openKycModal(employee)"
                      class="text-indigo-600 hover:text-indigo-900"
                    >
                      Register KYC
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- Add Employee Modal -->
    <div v-if="showAddModal" class="fixed inset-0 bg-gray-500 bg-opacity-75 flex items-center justify-center">
      <div class="bg-white p-6 rounded-lg max-w-md w-full">
        <h3 class="text-lg font-medium leading-6 text-gray-900 mb-4">Add New Employee</h3>
        <form @submit.prevent="createEmployee" class="space-y-4">
          <div>
            <label for="username" class="block text-sm font-medium text-gray-700">Username</label>
            <input
              type="text"
              id="username"
              v-model="newEmployee.username"
              class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
            >
          </div>
          <div>
            <label for="email" class="block text-sm font-medium text-gray-700">Email</label>
            <input
              type="email"
              id="email"
              v-model="newEmployee.email"
              class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
            >
          </div>
          <div>
            <label for="password" class="block text-sm font-medium text-gray-700">Password</label>
            <input
              type="password"
              id="password"
              v-model="newEmployee.password"
              class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
            >
          </div>
          <div class="flex justify-end space-x-3">
            <button
              type="button"
              @click="showAddModal = false"
              class="rounded-md border border-gray-300 bg-white py-2 px-4 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
            >
              Cancel
            </button>
            <button
              type="submit"
              class="rounded-md border border-transparent bg-indigo-600 py-2 px-4 text-sm font-medium text-white shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
            >
              Save
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- KYC Registration Modal -->
    <div v-if="showKycModal" class="fixed inset-0 bg-gray-500 bg-opacity-75 flex items-center justify-center">
      <div class="bg-white p-6 rounded-lg max-w-md w-full">
        <h3 class="text-lg font-medium leading-6 text-gray-900 mb-4">
          Register KYC for {{ selectedEmployee?.username }}
        </h3>
        <form @submit.prevent="registerKyc" class="space-y-4">
          <div>
            <label for="firstName" class="block text-sm font-medium text-gray-700">First Name</label>
            <input
              type="text"
              id="firstName"
              v-model="kycData.firstName"
              required
              class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
            >
          </div>
          <div>
            <label for="lastName" class="block text-sm font-medium text-gray-700">Last Name</label>
            <input
              type="text"
              id="lastName"
              v-model="kycData.lastName"
              required
              class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
            >
          </div>
          <div>
            <label for="address" class="block text-sm font-medium text-gray-700">Address</label>
            <textarea
              id="address"
              v-model="kycData.address"
              rows="3"
              required
              class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
            ></textarea>
          </div>
          <div>
            <label for="fingerprintId" class="block text-sm font-medium text-gray-700">Fingerprint ID</label>
            <input
              type="text"
              id="fingerprintId"
              v-model="kycData.fingerprintId"
              required
              class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
            >
          </div>
          <div>
            <label for="photoUrl" class="block text-sm font-medium text-gray-700">Photo URL</label>
            <input
              type="url"
              id="photoUrl"
              v-model="kycData.photoUrl"
              required
              class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
            >
          </div>
          <div class="flex justify-end space-x-3">
            <button
              type="button"
              @click="showKycModal = false"
              class="rounded-md border border-gray-300 bg-white py-2 px-4 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
            >
              Cancel
            </button>
            <button
              type="submit"
              class="rounded-md border border-transparent bg-indigo-600 py-2 px-4 text-sm font-medium text-white shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
            >
              Register
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template> 