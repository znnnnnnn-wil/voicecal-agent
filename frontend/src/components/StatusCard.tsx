function StatusCard() {
  return (
    <section className="rounded-[28px] border border-white/10 bg-white/[0.06] p-5 shadow-2xl shadow-black/25 backdrop-blur-xl">
      <p className="text-sm font-semibold text-white">Backend foundation</p>
      <p className="mt-1 text-xs text-slate-400">Prepared for model integration</p>

      <div className="mt-5 space-y-3">
        <div className="flex items-center justify-between rounded-2xl bg-white/[0.06] px-4 py-3">
          <span className="text-sm text-slate-300">CRUD API</span>
          <span className="text-xs font-semibold text-emerald-200">Ready</span>
        </div>
        <div className="flex items-center justify-between rounded-2xl bg-white/[0.06] px-4 py-3">
          <span className="text-sm text-slate-300">Tool calling</span>
          <span className="text-xs font-semibold text-cyan-200">Wired</span>
        </div>
        <div className="flex items-center justify-between rounded-2xl bg-white/[0.06] px-4 py-3">
          <span className="text-sm text-slate-300">AI provider</span>
          <span className="text-xs font-semibold text-amber-100">Pending</span>
        </div>
      </div>
    </section>
  )
}

export default StatusCard
