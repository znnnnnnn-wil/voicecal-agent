import type { CalendarEvent } from '../types/calendar'

const toneClass = {
  cyan: 'border-cyan-300/20 bg-cyan-300/10 text-cyan-50',
  violet: 'border-violet-300/20 bg-violet-300/10 text-violet-50',
  emerald: 'border-emerald-300/20 bg-emerald-300/10 text-emerald-50',
  amber: 'border-amber-200/20 bg-amber-200/10 text-amber-50',
}

const toneCycle = ['cyan', 'violet', 'emerald', 'amber'] as const

type CalendarBoardProps = {
  events: CalendarEvent[]
  error: string | null
  isLoading: boolean
  isUsingDemoEvents: boolean
  onRetry: () => void
}

function CalendarBoard({
  events,
  error,
  isLoading,
  isUsingDemoEvents,
  onRetry,
}: CalendarBoardProps) {
  const calendarDays = buildCalendarDays(events)

  return (
    <section className="h-fit self-start rounded-[32px] border border-white/10 bg-white/[0.07] p-5 shadow-2xl shadow-black/25 backdrop-blur-xl sm:p-6">
      <div className="mb-5 flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="text-sm font-semibold text-white">日历视图</p>
          <p className="mt-1 text-xs text-slate-400">
            {isLoading ? '正在加载本周日程...' : '基于本周日程接口'}
          </p>
        </div>
        <span className="w-fit rounded-full bg-white/[0.08] px-3 py-1 text-xs text-slate-300">
          {isUsingDemoEvents ? 'Demo fallback' : `${events.length} 个事件`}
        </span>
      </div>

      {error && (
        <div className="mb-4 rounded-2xl border border-amber-200/20 bg-amber-200/10 p-4">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <p className="text-sm leading-6 text-amber-50">
              Showing demo data because the backend is unavailable. {error}
            </p>
            <button
              className="w-fit rounded-full border border-amber-100/30 bg-amber-100/10 px-3 py-1.5 text-xs font-semibold text-amber-50 transition hover:bg-amber-100/15"
              onClick={onRetry}
              type="button"
            >
              重新加载
            </button>
          </div>
        </div>
      )}

      {isLoading && (
        <div className="grid gap-3 md:grid-cols-5">
          {Array.from({ length: 5 }).map((_, index) => (
            <div
              className="min-h-56 rounded-3xl border border-white/10 bg-[#0d131a]/70 p-3"
              key={index}
            >
              <div className="mb-4 h-4 w-16 animate-pulse rounded-full bg-white/15" />
              <div className="space-y-2">
                <div className="h-16 animate-pulse rounded-2xl bg-white/10" />
                <div className="h-12 animate-pulse rounded-2xl bg-white/5" />
              </div>
            </div>
          ))}
        </div>
      )}

      {!isLoading && events.length === 0 && (
        <div className="rounded-3xl border border-dashed border-white/15 bg-[#0d131a]/70 p-8 text-center">
          <p className="text-sm font-semibold text-white">暂无日程事件</p>
          <p className="mt-2 text-sm leading-6 text-slate-400">
            No calendar events yet. Try creating one from the backend API or ask VoiceCal to prepare a
            draft.
          </p>
        </div>
      )}

      {!isLoading && events.length > 0 && (
      <div className="grid gap-3 md:grid-cols-5">
        {calendarDays.map((day) => (
          <div
            className="min-h-56 rounded-3xl border border-white/10 bg-[#0d131a]/70 p-3"
            key={day.day}
          >
            <div className="mb-4 flex items-center justify-between">
              <div>
                <p className="text-sm font-semibold text-white">{day.day}</p>
                <p className="text-xs text-slate-500">{day.date}</p>
              </div>
              <span className="size-2 rounded-full bg-cyan-200" />
            </div>

            <div className="space-y-2">
              {day.events.map((event) => (
                <div
                  className={`rounded-2xl border p-3 ${toneClass[event.tone]}`}
                  key={`${day.day}-${event.time}-${event.title}`}
                >
                  <p className="text-xs opacity-80">{event.time}</p>
                  <p className="mt-1 text-sm font-semibold">{event.title}</p>
                </div>
              ))}
              <div className="rounded-2xl border border-dashed border-white/10 px-3 py-4 text-xs text-slate-500">
                可预约空档
              </div>
            </div>
          </div>
        ))}
      </div>
      )}
    </section>
  )
}

function buildCalendarDays(events: CalendarEvent[]) {
  const groupedEvents = [...events]
    .sort((left, right) => left.startTime.localeCompare(right.startTime))
    .reduce<Record<string, CalendarEvent[]>>((groups, event) => {
      const key = getDateKey(event.startTime)
      groups[key] = [...(groups[key] ?? []), event]
      return groups
    }, {})

  return Object.entries(groupedEvents)
    .slice(0, 5)
    .map(([dateKey, dayEvents]) => ({
      day: formatWeekday(dateKey),
      date: formatDate(dateKey),
      events: dayEvents.slice(0, 3).map((event, index) => ({
        time: formatTime(event.startTime),
        title: event.title,
        tone: toneCycle[index % toneCycle.length],
      })),
    }))
}

function getDateKey(value: string) {
  return value.slice(0, 10)
}

function formatDate(value: string) {
  return value.slice(5).replace('-', '/')
}

function formatTime(value: string) {
  return value.slice(11, 16)
}

function formatWeekday(value: string) {
  return new Intl.DateTimeFormat('zh-CN', { weekday: 'short' }).format(new Date(`${value}T00:00:00`))
}

export default CalendarBoard
