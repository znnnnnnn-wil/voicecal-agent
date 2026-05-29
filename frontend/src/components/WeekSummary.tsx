import type { CalendarEvent } from '../types/calendar'

type WeekSummaryProps = {
  events: CalendarEvent[]
  isLoading: boolean
  isUsingDemoEvents: boolean
}

function WeekSummary({ events, isLoading, isUsingDemoEvents }: WeekSummaryProps) {
  const weekStats = buildWeekStats(events)

  return (
    <section className="h-fit self-start rounded-[28px] border border-white/10 bg-white/[0.06] p-5 shadow-2xl shadow-black/25 backdrop-blur-xl">
      <div className="mb-5">
        <p className="text-sm font-semibold text-white">本周摘要</p>
        <p className="mt-1 text-xs text-slate-400">
          {isUsingDemoEvents ? '基于 demo fallback 数据' : '基于本周日程接口'}
        </p>
      </div>

      <div className="grid grid-cols-2 gap-3">
        {weekStats.map((stat) => (
          <div className="rounded-2xl border border-white/10 bg-[#0d131a]/70 p-4" key={stat.label}>
            <p className="text-2xl font-semibold text-white">{isLoading ? '...' : stat.value}</p>
            <p className="mt-1 text-xs text-slate-400">{stat.label}</p>
            <p className="mt-2 text-[11px] text-cyan-100/70">{stat.hint}</p>
          </div>
        ))}
      </div>
    </section>
  )
}

function buildWeekStats(events: CalendarEvent[]) {
  const focusHours = events
    .filter((event) => {
      const title = event.title.toLowerCase()
      return title.includes('focus') || title.includes('专注')
    })
    .reduce((total, event) => total + getDurationHours(event), 0)

  const meetingsNeedPreparation = events.filter((event) => {
    const title = event.title.toLowerCase()
    return title.includes('会议') || title.includes('评审') || title.includes('meeting') || title.includes('review')
  }).length

  const tomorrowEventCount = events.filter((event) => event.startTime.startsWith(getTomorrowKey())).length

  return [
    { label: '已加载日程', value: String(events.length), hint: '来自事件列表' },
    { label: '专注时间', value: `${formatNumber(focusHours)}h`, hint: '按标题估算' },
    { label: '需准备会议', value: String(meetingsNeedPreparation), hint: '会议/评审类' },
    { label: '明日空档', value: String(Math.max(0, 4 - tomorrowEventCount)), hint: '前端估算' },
  ]
}

function getDurationHours(event: CalendarEvent) {
  const start = new Date(event.startTime).getTime()
  const end = new Date(event.endTime).getTime()
  if (Number.isNaN(start) || Number.isNaN(end) || end <= start) {
    return 0
  }
  return (end - start) / 1000 / 60 / 60
}

function getTomorrowKey() {
  const tomorrow = new Date()
  tomorrow.setDate(tomorrow.getDate() + 1)
  return tomorrow.toISOString().slice(0, 10)
}

function formatNumber(value: number) {
  return Number.isInteger(value) ? String(value) : value.toFixed(1)
}

export default WeekSummary
