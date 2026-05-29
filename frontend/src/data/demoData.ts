export type TodayEvent = {
  time: string
  title: string
  meta: string
  status: 'confirmed' | 'draft' | 'focus'
}

export type CalendarDay = {
  day: string
  date: string
  events: {
    time: string
    title: string
    tone: 'cyan' | 'violet' | 'emerald' | 'amber'
  }[]
}

export type OperationLogItem = {
  time: string
  label: string
  detail: string
  status: 'success' | 'info' | 'pending'
}

export const todayEvents: TodayEvent[] = [
  {
    time: '09:00',
    title: '产品晨会',
    meta: '线上会议',
    status: 'confirmed',
  },
  {
    time: '11:30',
    title: '设计评审',
    meta: '会议室 A',
    status: 'draft',
  },
  {
    time: '14:00',
    title: '客户跟进',
    meta: '销售线索',
    status: 'confirmed',
  },
  {
    time: '16:00',
    title: '专注时间',
    meta: '勿扰模式',
    status: 'focus',
  },
]

export const calendarDays: CalendarDay[] = [
  {
    day: '周一',
    date: '05/25',
    events: [
      { time: '09:00', title: '周计划', tone: 'cyan' },
      { time: '15:00', title: '研发同步', tone: 'violet' },
    ],
  },
  {
    day: '周二',
    date: '05/26',
    events: [
      { time: '10:30', title: '用户访谈', tone: 'emerald' },
      { time: '14:00', title: '方案整理', tone: 'amber' },
    ],
  },
  {
    day: '周三',
    date: '05/27',
    events: [{ time: '11:00', title: '路线评审', tone: 'violet' }],
  },
  {
    day: '周四',
    date: '05/28',
    events: [
      { time: '09:30', title: '版本规划', tone: 'cyan' },
      { time: '16:00', title: '专注块', tone: 'emerald' },
    ],
  },
  {
    day: '周五',
    date: '05/29',
    events: [
      { time: '09:00', title: '产品晨会', tone: 'cyan' },
      { time: '11:30', title: '设计评审', tone: 'violet' },
      { time: '16:00', title: '专注时间', tone: 'emerald' },
    ],
  },
]

export const weekStats = [
  { label: '已安排日程', value: '12', hint: '本周' },
  { label: '专注时间', value: '6.5h', hint: '已保留' },
  { label: '需准备会议', value: '3', hint: '待跟进' },
  { label: '明日空档', value: '2', hint: '可预约' },
]

export const operationLogs: OperationLogItem[] = [
  {
    time: '刚刚',
    label: '解析自然语言指令',
    detail: '识别到会议主题、日期和时间偏好',
    status: 'success',
  },
  {
    time: '1 分钟前',
    label: '检查日历可用性',
    detail: '找到明天 10:00 的可用时间段',
    status: 'info',
  },
  {
    time: '2 分钟前',
    label: '准备日程草稿',
    detail: '等待后续 PR 接入真实后端执行',
    status: 'pending',
  },
]
