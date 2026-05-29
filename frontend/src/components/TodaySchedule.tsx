import { todayEvents } from '../data/demoData'

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

function TodaySchedule() {
  return (
    <section className="rounded-[28px] border border-white/10 bg-white/[0.06] p-5 shadow-2xl shadow-black/25 backdrop-blur-xl">
      <div className="mb-5 flex items-center justify-between">
        <div>
          <p className="text-sm font-semibold text-white">今日日程</p>
          <p className="mt-1 text-xs text-slate-400">4 个待处理安排</p>
        </div>
        <span className="rounded-full bg-white/[0.08] px-3 py-1 text-xs text-slate-300">今天</span>
      </div>

      <div className="space-y-3">
        {todayEvents.map((event) => (
          <div
            className="rounded-2xl border border-white/10 bg-[#0d131a]/70 p-4"
            key={`${event.time}-${event.title}`}
          >
            <div className="flex items-start justify-between gap-3">
              <div>
                <p className="text-xs font-medium text-slate-400">{event.time}</p>
                <p className="mt-1 text-sm font-semibold text-white">{event.title}</p>
                <p className="mt-1 text-xs text-slate-500">{event.meta}</p>
              </div>
              <span className={`rounded-full border px-2.5 py-1 text-[11px] ${statusClass[event.status]}`}>
                {statusText[event.status]}
              </span>
            </div>
          </div>
        ))}
      </div>
    </section>
  )
}

export default TodaySchedule
