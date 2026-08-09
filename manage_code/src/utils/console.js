export function firstFilled(...values) {
  for (const value of values) {
    if (value !== null && value !== undefined && String(value).trim() !== '') {
      return value
    }
  }
  return ''
}

export function stripHtml(value) {
  if (!value) return ''
  return String(value).replace(/<[^>]+>/g, '').replace(/&nbsp;/g, ' ').trim()
}

export function readImageAsDataUrl(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result)
    reader.onerror = () => reject(new Error('图片读取失败'))
    reader.readAsDataURL(file)
  })
}

export function resolveAssetUrl(value) {
  if (!value) return ''
  const text = String(value).trim()
  if (!text) return ''
  if (text.startsWith('data:') || text.startsWith('blob:')) return text
  if (/^https?:\/\//i.test(text)) return text

  const apiBase = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
  const origin = apiBase.replace(/\/api\/?$/, '')
  if (text.startsWith('/')) {
    return `${origin}${text}`
  }
  return `${origin}/${text}`
}

export function downloadBlobFile(blob, fileName) {
  const url = window.URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = fileName
  anchor.click()
  window.URL.revokeObjectURL(url)
}

export function toCountMap(list, key) {
  return list.reduce((accumulator, item) => {
    const currentKey = item?.[key] ?? 'unknown'
    accumulator[currentKey] = (accumulator[currentKey] || 0) + 1
    return accumulator
  }, {})
}
