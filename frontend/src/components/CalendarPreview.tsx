const events = [
  {
    time: '09:00',
    title: 'Product standup',
    tone: 'bg-cyan-300',
  },
  {
    time: '11:30',
    title: 'Design review',
    tone: 'bg-violet-300',
  },
  {
    time: '15:00',
    title: 'Focus time',
    tone: 'bg-amber-200',
  },
]

function CalendarPreview() {
  return (
    <section className="rounded-[28px] border border-white/10 bg-white/[0.06] p-5 shadow-2xl shadow-black/25 backdrop-blur-xl">
      <div className="mb-5 flex items-center justify-between">
        <div>
          <p className="text-sm font-semibold text-white">Today</p>
          <p className="mt-1 text-xs text-slate-400">Static calendar preview</p>
        </div>
        <span className="rounded-full bg-white/[0.08] px-3 py-1 text-xs text-slate-300">May 29</span>
      </div>

      <div className="space-y-3">
        {events.map((event) => (
          <div
            className="flex items-center gap-3 rounded-2xl border border-white/10 bg-[#0d131a]/70 p-3"
            key={event.title}
          >
            <span className={`h-10 w-1.5 rounded-full ${event.tone}`} />
            <div className="min-w-0 flex-1">
              <p className="text-xs font-medium text-slate-400">{event.time}</p>
              <p className="truncate text-sm font-semibold text-white">{event.title}</p>
            </div>
          </div>
        ))}
      </div>
    </section>
  )
}

export default CalendarPreview
