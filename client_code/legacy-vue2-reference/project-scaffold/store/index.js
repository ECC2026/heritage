import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

let lifeData = {}

try {
  lifeData = uni.getStorageSync('lifeData')
} catch(e) {}

let saveStateKeys = ['vuex_user']

const saveLifeData = function(key, value) {
  if (saveStateKeys.indexOf(key) != -1) {
    let tmpLifeData = uni.getStorageSync('lifeData')
    tmpLifeData = tmpLifeData ? tmpLifeData : {}
    tmpLifeData[key] = value
    uni.setStorageSync('lifeData', tmpLifeData)
  }
}

const store = new Vuex.Store({
  state: {
    vuex_user: lifeData.vuex_user ? lifeData.vuex_user : { name: '未登录' },
    vuex_version: "1.0.0",
    vuex_custom_nav_bar: true,
    vuex_status_bar_height: 0,
    vuex_custom_bar_height: 0
  },
  getters: {
    isLoggedIn(state) {
      return state.vuex_user && state.vuex_user.id
    },
    userId(state) {
      return state.vuex_user ? state.vuex_user.id : null
    },
    userName(state) {
      return state.vuex_user ? state.vuex_user.name : '未登录'
    }
  },
  mutations: {
    SET_USER_INFO(state, payload) {
      state.vuex_user = payload
      saveLifeData('vuex_user', payload)
    },
    $tStore(state, payload) {
      let nameArr = payload.name.split('.')
      let saveKey = ''
      let len = nameArr.length
      if (len >= 2) {
        let obj = state[nameArr[0]]
        for (let i = 1; i < len - 1; i++) {
          obj = obj[nameArr[i]]
        }
        obj[nameArr[len - 1]] = payload.value
        saveKey = nameArr[0]
      } else {
        state[payload.name] = payload.value
        saveKey = payload.name
      }
      saveLifeData(saveKey, state[saveKey])
    }
  },
  actions: {}
})

export default store
