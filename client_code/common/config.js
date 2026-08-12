//
// API 地址优先读取 VITE_API_BASE_URL，方便不同开发机和部署环境覆盖。
// 8080 只作为当前电脑微信开发者工具的本地兜底地址；真机调试时需要在
// 本地环境变量中换成电脑的局域网 IP，不能直接使用 localhost。
//
const config = {
  baseUrl: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  appName: '非遗文化互动平台',
  themeColor: '#a6472d'
}

export default config
