import type { CalendarEvent } from '../types/calendar'

const statusClass = {
  confirmed: 'bg-emerald-300/10 text-emerald-100 border-emerald-300/20',
  draft: 'bg-amber-200/10 text-amber-100 border-amber-200/20',
  focus: 'bg-violet-300/10 text-violet-100 border-violet-300/20',
}

const statusText = {
  confirmed: '已确认',
  draft: '草稿',
  focus: '专注',
}

type TodayScheduleProps = {
  events: CalendarEvent[]
  error: string | null
  isLoading: boolean
  isUsingDemoEvents: boolean
  onRetry: () => void
}

function TodaySchedule({
  events,
  error,
  isLoading,
  isUsingDemoEvents,
  onRetry,
}: TodayScheduleProps) {
  const sortedEvents = [...events].sort((left, right) =>
    left.startTime.localeCompare(right.startTime),
  )

  return (
    <section className="h-fit self-start rounded-[28px] border border-white/10 bg-white/[0.06] p-5 shadow-2xl shadow-black/25 backdrop-blur-xl">
      <div className="mb-5 flex items-center justify-between">
        <div>
          <p className="text-sm font-semibold text-white">今日日程</p>
          <p className="mt-1 text-xs text-slate-400">
            {isUsingDemoEvents ? 'Demo fallback data' : `${sortedEvents.length} 个后端日程`}
          </p>
        </div>
        <span className="rounded-full bg-white/[0.08] px-3 py-1 text-xs text-slate-300">今天</span>
      </div>

      {error && (
        <div className="mb-3 rounded-2xl border border-amber-200/20 bg-amber-200/10 p-3">
          <p className="text-xs leading-5 text-amber-50">
            Showing demo data because the backend is unavailable.
          </p>
          <button
            className="mt-2 text-xs font-semibold text-amber-100 underline underline-offset-4"
            onClick={onRetry}
            type="button"
          >
            重试加载
          </button>
        </div>
      )}

      {isLoading && (
        <div className="space-y-3">
          {Array.from({ length: 4 }).map((_, index) => (
            <div className="h-20 animate-pulse rounded-2xl bg-white/10" key={index} />
          ))}
        </div>
      )}

      {!isLoading && sortedEvents.length === 0 && (
        <div className="rounded-2xl border border-dashed border-white/15 bg-[#0d131a]/70 p-5">
          <p className="text-sm font-semibold text-white">今天暂无日程</p>
          <p className="mt-2 text-xs leading-5 text-slate-400">
            后端当前没有返回今天的事件。你可以通过后端 API 创建日程，或让 VoiceCal 先准备草稿。
          </p>
        </div>
      )}

      {!isLoading && sortedEvents.length > 0 && (
        <div className="space-y-3">
          {sortedEvents.map((event) => (
            <div
              className="rounded-2xl border border-white/10 bg-[#0d131a]/70 p-4"
              key={event.id}
            >
              <div className="flex items-start justify-between gap-3">
                <div className="min-w-0">
                  <p className="text-xs font-medium text-slate-400">{formatTime(event.startTime)}</p>
                  <p className="mt-1 break-words text-sm font-semibold text-white">{event.title}</p>
                  <p className="mt-1 text-xs text-slate-500">{event.location || '未设置地点'}</p>
                  <p className="mt-2 text-[11px] text-slate-500">{formatReminder(event)}</p>
                </div>
                <span className={`shrink-0 rounded-full border px-2.5 py-1 text-[11px] ${statusClass[getEventStatus(event)]}`}>
                  {statusText[getEventStatus(event)]}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}
    </section>
  )
}

function getEventStatus(event: CalendarEvent): keyof typeof statusText {
  const title = event.title.toLowerCase()
  if (title.includes('focus') || title.includes('专注')) {
    return 'focus'
  }
  if (event.description?.toLowerCase().includes('draft')) {
    return 'draft'
  }
  return 'confirmed'
}

function formatReminder(event: CalendarEvent) {
  if (event.reminderMinutes === null || event.reminderMinutes === undefined) {
    return '无提醒'
  }
  return `提醒：提前 ${event.reminderMinutes} 分钟，${event.reminderTriggered ? '已提醒' : '未提醒'}`
}

function formatTime(value: string) {
  return value.slice(11, 16)
}

export default TodaySchedule
