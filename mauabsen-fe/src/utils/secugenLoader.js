export const loadSecugenSDK = () => {
  return new Promise((resolve, reject) => {
    console.log(window.SecuGen);
    
    if (window.SecuGen?.FDxSDKPro) {
      resolve(window.SecuGen.FDxSDKPro)
      return
    }

    const loadScript = (src) => {
      return new Promise((resolve, reject) => {
        const script = document.createElement('script')
        script.src = src
        script.onload = resolve
        script.onerror = reject
        document.head.appendChild(script)
      })
    }

    Promise.all([
      loadScript('/secugen/sgfplib.js'),
      loadScript('/secugen/FDxSDKPro.js')
    ])
      .then(() => {
        if (window.SecuGen?.FDxSDKPro) {
          resolve(window.SecuGen.FDxSDKPro)
        } else {
          reject(new Error('Failed to load SecuGen SDK'))
        }
      })
      .catch(reject)
  })
} 