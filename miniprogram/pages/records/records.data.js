module.exports = {
  data: {
    isLoggedIn: false,
    userInfo: null,
    records: [],
    statusText: '未加载',
    searchPhone: '',
    searchLastName: '',
    startDate: '',
    endDate: '',
    consumeRecords: [],
    consumeStatusText: '未加载',
    currentTab: 'eyelash',
    prefillData: null, // 预填的用户信息

    showDeleteModal: false,
    deleteRecord: null,
    showBackToTop: false,

    // 睫毛记录模态弹窗相关数据
    showEyelashModal: false,
    isEdit: false,
    currentEditId: null,
    isFromMember: false, // 是否从成员列表打开
    eyelashForm: {
      phone: '',
      lastName: '',
      style: '',
      modelNumber: '',
      length: '',
      curl: '',
      recordDate: ''
    }
  }
}
