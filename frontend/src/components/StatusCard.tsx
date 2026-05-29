function StatusCard() {
  return (
    <section className="rounded-[28px] border border-white/10 bg-white/[0.06] p-5 shadow-2xl shadow-black/25 backdrop-blur-xl">
      <p className="text-sm font-semibold text-white">后端基础能力</p>
      <p className="mt-1 text-xs text-slate-400">已为模型接入做好准备</p>

      <div className="mt-5 space-y-3">
        <div className="flex items-center justify-between rounded-2xl bg-white/[0.06] px-4 py-3">
          <span className="text-sm text-slate-300">日程 CRUD API</span>
          <span className="text-xs font-semibold text-emerald-200">已就绪</span>
        </div>
        <div className="flex items-center justify-between rounded-2xl bg-white/[0.06] px-4 py-3">
          <span className="text-sm text-slate-300">工具调用</span>
          <span className="text-xs font-semibold text-cyan-200">已接入</span>
        </div>
        <div className="flex items-center justify-between rounded-2xl bg-white/[0.06] px-4 py-3">
          <span className="text-sm text-slate-300">AI 模型服务</span>
          <span className="text-xs font-semibold text-amber-100">待配置</span>
        </div>
      </div>
    </section>
  )
}

export default StatusCard
