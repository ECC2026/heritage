import config from '@/common/config.js'

export function formatDateTime(value) {
  if (!value) return ''
  const text = String(value).replace('T', ' ')
  return text.length > 16 ? text.slice(0, 16) : text
}

export function formatPrice(value) {
  const number = Number(value || 0)
  return Number.isNaN(number) ? '0.00' : number.toFixed(2)
}

export function shortText(value, max = 48) {
  if (!value) return ''
  const text = String(value).replace(/<[^>]+>/g, '').replace(/&nbsp;/g, ' ').trim()
  return text.length > max ? `${text.slice(0, max)}...` : text
}

export function normalizeImage(value, fallback = '/static/img/logo.png') {
  if (!value) return fallback
  if (/^https?:\/\//i.test(value)) return value
  if (String(value).startsWith('/static/')) return value
  if (String(value).startsWith('/uploads/')) {
    return `${config.baseUrl.replace(/\/api$/, '')}${value}`
  }
  return value
}

export function genderText(value) {
  if (value === 1 || value === '1') return '男'
  if (value === 2 || value === '2') return '女'
  return '保密'
}
