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
    },

    // 消费记录模态弹窗相关数据
    showConsumeModal: false,
    consumeType: '', // 'recharge' 或 'consume'
    consumeItemIndex: 0,
    consumeTypeIndex: 0,
    consumeItems: ['会员充值', '美睫项目', '美甲项目'],
    consumeTypes: ['充值', '支出'],
    consumeForm: {
      phone: '',
      lastName: '',
      balance: 0,
      consumeAmount: '',
      consumeItem: '',
      consumeType: '',
      consumeDate: ''
    }
  }
}
