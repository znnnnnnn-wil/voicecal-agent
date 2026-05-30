import { getCategoryBadgeClass, getCategoryLabel } from '../lib/categoryUtils'
import type { CalendarEvent } from '../types/calendar'

type TodayScheduleProps = {
  events: CalendarEvent[]
  error: string | null
  isLoading: boolean
  isUsingDemoEvents: boolean
  onRetry: () => void
}

function TodaySchedule({ events, error, isLoading, isUsingDemoEvents, onRetry }: TodayScheduleProps) {
  const sortedEvents = [...events].sort((left, right) => left.startTime.localeCompare(right.startTime))
  const totalMinutes = sortedEvents.reduce((total, event) => total + getDurationMinutes(event), 0)
  const reminderCount = sortedEvents.filter((event) => event.reminderMinutes !== null && event.reminderMinutes !== undefined).length

  return (
    <section className="rounded-2xl border border-[#dadce0] bg-white p-4 shadow-sm">
      <div className="mb-4 flex items-center justify-between">
        <div>
          <p className="text-sm font-semibold text-[#202124]">今日概览</p>
          <p className="mt-1 text-xs text-[#5f6368]">
            {isUsingDemoEvents ? 'Demo fallback data' : `${sortedEvents.length} 个后端日程`}
          </p>
        </div>
        <span className="rounded-full bg-blue-50 px-2.5 py-1 text-xs font-medium text-[#1a73e8]">今天</span>
      </div>

      <div className="mb-4 grid grid-cols-3 gap-2">
        <Metric label="日程" value={String(sortedEvents.length)} />
        <Metric label="总时长" value={formatMinutes(totalMinutes)} />
        <Metric label="提醒" value={String(reminderCount)} />
      </div>

      {error && (
        <div className="mb-3 rounded-lg border border-amber-200 bg-amber-50 p-3">
          <p className="text-xs leading-5 text-amber-800">后端暂不可用，当前展示 demo fallback 数据。</p>
          <button className="mt-2 text-xs font-semibold text-amber-700 underline underline-offset-4" onClick={onRetry} type="button">
            重试加载
          </button>
        </div>
      )}

      {isLoading && <div className="space-y-2">{Array.from({ length: 3 }).map((_, index) => <div className="h-14 animate-pulse rounded-xl bg-slate-100" key={index} />)}</div>}

      {!isLoading && sortedEvents.length === 0 && (
        <div className="rounded-xl border border-dashed border-[#dadce0] bg-[#f8fafc] p-4">
          <p className="text-sm font-semibold text-[#202124]">今天暂无日程</p>
          <p className="mt-1 text-xs leading-5 text-[#5f6368]">可以通过语音或文本创建一个新的安排。</p>
        </div>
      )}

      {!isLoading && sortedEvents.length > 0 && (
        <div className="space-y-2">
          {sortedEvents.slice(0, 4).map((event) => (
            <div className="rounded-xl border border-[#e5e7eb] bg-[#f8fafc] p-3" key={event.id}>
              <div className="flex items-start justify-between gap-2">
                <div className="min-w-0">
                  <p className="text-xs font-medium text-[#5f6368]">{formatTime(event.startTime)}</p>
                  <p className="mt-1 truncate text-sm font-semibold text-[#202124]">{event.title}</p>
                  <p className="mt-1 truncate text-xs text-[#5f6368]">{event.location || '未设置地点'}</p>
                </div>
                <span className={`shrink-0 rounded-full border px-2 py-0.5 text-[11px] ${getCategoryBadgeClass(event.category)}`}>
                  {getCategoryLabel(event.category)}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}
    </section>
  )
}

function Metric({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-xl bg-[#f8fafc] p-3">
      <p className="text-base font-semibold text-[#202124]">{value}</p>
      <p className="mt-1 text-[11px] text-[#5f6368]">{label}</p>
    </div>
  )
}

function getDurationMinutes(event: CalendarEvent) {
  const start = new Date(event.startTime).getTime()
  const end = new Date(event.endTime).getTime()
  if (Number.isNaN(start) || Number.isNaN(end) || end <= start) return 0
  return Math.round((end - start) / 1000 / 60)
}

function formatMinutes(minutes: number) {
  if (minutes < 60) return `${minutes}m`
  const hours = minutes / 60
  return Number.isInteger(hours) ? `${hours}h` : `${hours.toFixed(1)}h`
}

function formatTime(value: string) {
  return value.slice(11, 16)
}

export default TodaySchedule
