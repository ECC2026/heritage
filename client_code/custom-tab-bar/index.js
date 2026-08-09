Component({
  data: {
    selected: 0,
    list: [
      {
        pagePath: '/pages/index/index',
        text: '首页',
        iconClass: 'tn-icon-home',
        activeIconClass: 'tn-icon-home-fill'
      },
      {
        pagePath: '/pages/shop/list',
        text: '文创',
        iconClass: 'tn-icon-shop',
        activeIconClass: 'tn-icon-shop-fill'
      },
      {
        pagePath: '/pages/activity/list',
        text: '活动',
        iconClass: 'tn-icon-activity',
        activeIconClass: 'tn-icon-task-fill'
      },
      {
        pagePath: '/pages/community/index',
        text: '社区',
        iconClass: 'tn-icon-chat',
        activeIconClass: 'tn-icon-my-chat-fill'
      },
      {
        pagePath: '/pages/profile/index',
        text: '我的',
        iconClass: 'tn-icon-my',
        activeIconClass: 'tn-icon-my-fill'
      }
    ]
  },
  lifetimes: {
    attached() {
      this.syncSelected()
    }
  },
  pageLifetimes: {
    show() {
      this.syncSelected()
    }
  },
  methods: {
    syncSelected() {
      const pages = getCurrentPages()
      const currentPage = pages[pages.length - 1]
      const route = currentPage && currentPage.route ? `/${currentPage.route}` : ''
      const selected = this.data.list.findIndex((item) => item.pagePath === route)
      if (selected !== -1) {
        this.setData({ selected })
      }
    },
    switchTab(event) {
      const selected = Number(event.currentTarget.dataset.index)
      if (this.data.selected === selected) {
        return
      }
      this.setData({ selected })
      wx.switchTab({
        url: this.data.list[selected].pagePath
      })
    }
  }
})
