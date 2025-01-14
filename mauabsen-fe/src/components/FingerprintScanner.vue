<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  onCapture: {
    type: Function,
    required: true
  }
})

const status = ref('Ready')
const error = ref(null)
const fingerprintImage = ref('./Images/PlaceFinger.bmp')

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

    const params = new URLSearchParams({
      Timeout: "10000",
      Quality: "50",
      templateFormat: "ISO",
      imageWSQRate: "0.75"
    }).toString()

    xmlhttp.open("POST", uri, true)
    xmlhttp.send(params)

    xmlhttp.onerror = function () {
      reject(new Error(xmlhttp.statusText))
    }
  })
}

const captureFP = async () => {
  try {
    status.value = 'Scanning...'
    const result = await callSGIFPGetData()
    
    if (result.ErrorCode === 0) {
      // Update fingerprint image
      if (result.BMPBase64) {
        fingerprintImage.value = `data:image/bmp;base64,${result.BMPBase64}`
      }

      // Send template to parent component
      props.onCapture(result.TemplateBase64)
      
      status.value = 'Scan successful'
      
      // Log additional info
      console.log('Scan Info:', {
        serialNumber: result.SerialNumber,
        imageHeight: result.ImageHeight,
        imageWidth: result.ImageWidth,
        imageDPI: result.ImageDPI,
        imageQuality: result.ImageQuality,
        NFIQ: result.NFIQ
      })
    } else {
      throw new Error(`Capture Error: ${result.ErrorCode}`)
    }
  } catch (err) {
    error.value = err.message
    status.value = 'Scan failed'
    console.error('Capture error:', err)
  }
}

onMounted(() => {
  // Check if service is running on mount
  callSGIFPGetData().catch(err => {
    error.value = 'SecuGen service not running. Please start SGIBIOSRV.'
    console.error('Service check error:', err)
  })
})
</script>

<template>
  <div class="fingerprint-scanner">
    <div class="scan-container">
      <img 
        :src="fingerprintImage" 
        alt="Fingerprint Image" 
        class="fingerprint-image"
      >
      <button 
        @click="captureFP"
        class="scan-button"
        :disabled="status === 'Scanning...'"
      >
        Click to Scan
      </button>
    </div>
    <div class="status-container">
      <p class="status">Status: {{ status }}</p>
      <p v-if="error" class="error">Error: {{ error }}</p>
    </div>
  </div>
</template>

<style scoped>
.fingerprint-scanner {
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

.scan-button {
  background-color: #3b82f6;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 0.375rem;
  font-weight: 500;
  cursor: pointer;
}

.scan-button:disabled {
  background-color: #94a3b8;
  cursor: not-allowed;
}

.scan-button:hover:not(:disabled) {
  background-color: #2563eb;
}
</style> 