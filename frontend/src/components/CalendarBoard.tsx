import { calendarDays } from '../data/demoData'

const toneClass = {
  cyan: 'border-cyan-300/20 bg-cyan-300/10 text-cyan-50',
  violet: 'border-violet-300/20 bg-violet-300/10 text-violet-50',
  emerald: 'border-emerald-300/20 bg-emerald-300/10 text-emerald-50',
  amber: 'border-amber-200/20 bg-amber-200/10 text-amber-50',
}

function CalendarBoard() {
  return (
    <section className="rounded-[32px] border border-white/10 bg-white/[0.07] p-5 shadow-2xl shadow-black/25 backdrop-blur-xl sm:p-6">
      <div className="mb-5 flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="text-sm font-semibold text-white">日历视图</p>
          <p className="mt-1 text-xs text-slate-400">本周简化排期</p>
        </div>
        <span className="w-fit rounded-full bg-white/[0.08] px-3 py-1 text-xs text-slate-300">
          5 月最后一周
        </span>
      </div>

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
    </section>
  )
}

export default CalendarBoard
