import { getCategoryBadgeClass, getCategoryLabel, normalizeCategory } from '../lib/categoryUtils'
import type { CalendarEvent } from '../types/calendar'

type WeekSummaryProps = {
  events: CalendarEvent[]
  isLoading: boolean
  isUsingDemoEvents: boolean
}

function WeekSummary({ events, isLoading, isUsingDemoEvents }: WeekSummaryProps) {
  const weekStats = buildWeekStats(events)
  const categoryStats = buildCategoryStats(events)

  return (
    <section className="rounded-2xl border border-[#dadce0] bg-white p-4 shadow-sm">
      <div className="mb-4">
        <p className="text-sm font-semibold text-[#202124]">本周摘要</p>
        <p className="mt-1 text-xs text-[#5f6368]">
          {isUsingDemoEvents ? '基于 demo fallback 数据' : '基于本周日程接口'}
        </p>
      </div>

      <div className="grid grid-cols-2 gap-2">
        {weekStats.map((stat) => (
          <div className="rounded-xl bg-[#f8fafc] p-3" key={stat.label}>
            <p className="text-lg font-semibold text-[#202124]">{isLoading ? '...' : stat.value}</p>
            <p className="mt-1 text-[11px] text-[#5f6368]">{stat.label}</p>
          </div>
        ))}
      </div>

      <div className="mt-3 flex flex-wrap gap-2">
        {categoryStats.map((stat) => (
          <span className={`rounded-full border px-2.5 py-1 text-[11px] ${getCategoryBadgeClass(stat.category)}`} key={stat.category}>
            {getCategoryLabel(stat.category)} · {stat.count}
          </span>
        ))}
      </div>
    </section>
  )
}

function buildWeekStats(events: CalendarEvent[]) {
  const busyHours = events.reduce((total, event) => total + getDurationHours(event), 0)
  const meetingsNeedPreparation = events.filter((event) => {
    const title = event.title.toLowerCase()
    return title.includes('会议') || title.includes('评审') || title.includes('meeting') || title.includes('review')
  }).length
  const reminderCount = events.filter((event) => event.reminderMinutes !== null && event.reminderMinutes !== undefined).length

  return [
    { label: '日程数', value: String(events.length) },
    { label: '忙碌时间', value: `${formatNumber(busyHours)}h` },
    { label: '会议', value: String(meetingsNeedPreparation) },
    { label: '提醒', value: String(reminderCount) },
  ]
}

function getDurationHours(event: CalendarEvent) {
  const start = new Date(event.startTime).getTime()
  const end = new Date(event.endTime).getTime()
  if (Number.isNaN(start) || Number.isNaN(end) || end <= start) return 0
  return (end - start) / 1000 / 60 / 60
}

function formatNumber(value: number) {
  return Number.isInteger(value) ? String(value) : value.toFixed(1)
}

function buildCategoryStats(events: CalendarEvent[]) {
  const counts = events.reduce<Record<string, number>>((accumulator, event) => {
    const category = normalizeCategory(event.category)
    accumulator[category] = (accumulator[category] ?? 0) + 1
    return accumulator
  }, {})

  return Object.entries(counts).map(([category, count]) => ({ category, count }))
}

export default WeekSummary
