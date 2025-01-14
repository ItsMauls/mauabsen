<script setup>
import { ref } from 'vue'
import axios from 'axios'

const status = ref('Ready')
const error = ref(null)
const fingerprintImage = ref('./Images/PlaceFinger.bmp')
const template = ref('')
const matchScore = ref(null)
const registeredTemplate = ref('') // Untuk menyimpan template yang sudah terdaftar

const callSGIFPGetData = () => {
  return new Promise((resolve, reject) => {
    const uri = "https://localhost:8443/SGIFPCapture"
    const xmlhttp = new XMLHttpRequest()
    
    xmlhttp.onreadystatechange = function () {
      if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
        const fpobject = JSON.parse(xmlhttp.responseText)
        resolve(fpobject)
      } else if (xmlhttp.status == 404) {
        reject(new Error('Service not found'))
      }
    }

    const params = "Timeout=10000" +
      "&Quality=50" +
      "&templateFormat=ISO"

    xmlhttp.open("POST", uri, true)
    xmlhttp.send(params)
  })
}

const matchFingerprints = (template1, template2) => {
  return new Promise((resolve, reject) => {
    if (!template1 || !template2) {
      reject(new Error("Please scan finger first!"))
      return
    }

    const uri = "https://localhost:8443/SGIMatchScore"
    const xmlhttp = new XMLHttpRequest()
    
    xmlhttp.onreadystatechange = function () {
      if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
        const result = JSON.parse(xmlhttp.responseText)
        console.log('Match Score Result:', result)
        resolve(result)
      } else if (xmlhttp.status == 404) {
        reject(new Error('Service not found'))
      }
    }

    const params = "template1=" + encodeURIComponent(template1) +
      "&template2=" + encodeURIComponent(template2) +
      "&templateFormat=ISO"

    xmlhttp.open("POST", uri, true)
    xmlhttp.send(params)
  })
}

const clockIn = async () => {
  try {
    status.value = 'Scanning...'
    
    // 1. Dapatkan template yang sudah terdaftar
    const userResponse = await axios.get('/api/employees/current/fingerprint')
    registeredTemplate.value = userResponse.data.fingerprintTemplate

    if (!registeredTemplate.value) {
      throw new Error('No registered fingerprint found')
    }

    // 2. Scan fingerprint baru
    const result = await callSGIFPGetData()
    console.log('Scan Result:', result)
    
    if (result.ErrorCode === 0) {
      if (result.BMPBase64) {
        fingerprintImage.value = `data:image/bmp;base64,${result.BMPBase64}`
      }
      template.value = result.TemplateBase64

      // 3. Match fingerprint menggunakan SecuGen SDK
      const matchResult = await matchFingerprints(template.value, registeredTemplate.value)
      console.log('Match Result:', matchResult)

      if (matchResult.ErrorCode === 0) {
        matchScore.value = matchResult.MatchingScore

        if (matchResult.MatchingScore >= 100) {
          // 4. Jika match, kirim ke backend untuk clock in
          const response = await axios.post('/api/attendances/clock-in', {
            fingerprintTemplate: template.value,
            matchScore: matchResult.MatchingScore
          })

          status.value = 'Clock in successful'
          return response.data
        } else {
          status.value = 'Fingerprint does not match'
          throw new Error(`Fingerprint match score too low: ${matchResult.MatchingScore}`)
        }
      } else {
        throw new Error(`Match Error: ${matchResult.ErrorCode}`)
      }
    } else {
      throw new Error(`Capture Error: ${result.ErrorCode}`)
    }
  } catch (err) {
    error.value = err.message
    status.value = 'Clock in failed'
    console.error('Clock in error:', err)
  }
}

const clockOut = async () => {
  try {
    status.value = 'Scanning...'
    
    // 1. Dapatkan template yang sudah terdaftar
    const userResponse = await axios.get('/api/employees/current/fingerprint')
    registeredTemplate.value = userResponse.data.fingerprintTemplate

    if (!registeredTemplate.value) {
      throw new Error('No registered fingerprint found')
    }

    // 2. Scan fingerprint baru
    const result = await callSGIFPGetData()
    console.log('Scan Result:', result)
    
    if (result.ErrorCode === 0) {
      if (result.BMPBase64) {
        fingerprintImage.value = `data:image/bmp;base64,${result.BMPBase64}`
      }
      template.value = result.TemplateBase64

      // 3. Match fingerprint menggunakan SecuGen SDK
      const matchResult = await matchFingerprints(template.value, registeredTemplate.value)
      console.log('Match Result:', matchResult)

      if (matchResult.ErrorCode === 0) {
        matchScore.value = matchResult.MatchingScore

        if (matchResult.MatchingScore >= 100) {
          // 4. Jika match, kirim ke backend untuk clock out
          const response = await axios.post('/api/attendances/clock-out', {
            fingerprintTemplate: template.value,
            matchScore: matchResult.MatchingScore
          })

          status.value = 'Clock out successful'
          return response.data
        } else {
          status.value = 'Fingerprint does not match'
          throw new Error(`Fingerprint match score too low: ${matchResult.MatchingScore}`)
        }
      } else {
        throw new Error(`Match Error: ${matchResult.ErrorCode}`)
      }
    } else {
      throw new Error(`Capture Error: ${result.ErrorCode}`)
    }
  } catch (err) {
    error.value = err.message
    status.value = 'Clock out failed'
    console.error('Clock out error:', err)
  }
}
</script>

<template>
  <div class="attendance-scanner">
    <div class="scan-container">
      <img 
        :src="fingerprintImage" 
        alt="Fingerprint Image" 
        class="fingerprint-image"
      >
      <div class="button-group">
        <button 
          @click="clockIn"
          class="scan-button clock-in"
          :disabled="status === 'Scanning...'"
        >
          Clock In
        </button>
        <button 
          @click="clockOut"
          class="scan-button clock-out"
          :disabled="status === 'Scanning...'"
        >
          Clock Out
        </button>
      </div>
    </div>
    <div class="status-container">
      <p class="status">Status: {{ status }}</p>
      <p v-if="matchScore" class="match-score">Match Score: {{ matchScore }}</p>
      <p v-if="error" class="error">Error: {{ error }}</p>
    </div>
  </div>
</template>

<style scoped>
.attendance-scanner {
  padding: 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 0.5rem;
  background-color: #f8fafc;
}

.scan-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.fingerprint-image {
  width: 210px;
  height: 300px;
  border: 1px solid #cbd5e1;
  border-radius: 0.25rem;
}

.button-group {
  display: flex;
  gap: 1rem;
}

.scan-button {
  padding: 0.5rem 1rem;
  border-radius: 0.375rem;
  font-weight: 500;
  cursor: pointer;
}

.clock-in {
  background-color: #22c55e;
  color: white;
}

.clock-out {
  background-color: #ef4444;
  color: white;
}

.scan-button:disabled {
  background-color: #94a3b8;
  cursor: not-allowed;
}

.scan-button.clock-in:hover:not(:disabled) {
  background-color: #16a34a;
}

.scan-button.clock-out:hover:not(:disabled) {
  background-color: #dc2626;
}

.status-container {
  margin-top: 1rem;
}

.status {
  font-size: 0.875rem;
  color: #64748b;
}

.error {
  color: #ef4444;
  margin-top: 0.5rem;
}

.match-score {
  font-size: 0.875rem;
  color: #2563eb;
  margin-top: 0.5rem;
}
</style> 