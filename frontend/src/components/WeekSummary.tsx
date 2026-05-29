import { weekStats } from '../data/demoData'

function WeekSummary() {
  return (
    <section className="rounded-[28px] border border-white/10 bg-white/[0.06] p-5 shadow-2xl shadow-black/25 backdrop-blur-xl">
      <div className="mb-5">
        <p className="text-sm font-semibold text-white">本周摘要</p>
        <p className="mt-1 text-xs text-slate-400">排期、专注和准备事项</p>
      </div>

      <div className="grid grid-cols-2 gap-3">
        {weekStats.map((stat) => (
          <div className="rounded-2xl border border-white/10 bg-[#0d131a]/70 p-4" key={stat.label}>
            <p className="text-2xl font-semibold text-white">{stat.value}</p>
            <p className="mt-1 text-xs text-slate-400">{stat.label}</p>
            <p className="mt-2 text-[11px] text-cyan-100/70">{stat.hint}</p>
          </div>
        ))}
      </div>
    </section>
  )
}

export default WeekSummary
