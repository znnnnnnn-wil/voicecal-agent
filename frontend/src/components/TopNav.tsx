function TopNav() {
  return (
    <header className="sticky top-0 z-20 border-b border-white/10 bg-[#090d12]/70 backdrop-blur-2xl">
      <div className="mx-auto flex w-full max-w-7xl items-center justify-between gap-4 px-5 py-4 sm:px-8 lg:px-10">
        <div className="flex min-w-0 items-center gap-3">
          <div className="grid size-11 shrink-0 place-items-center rounded-2xl bg-gradient-to-br from-emerald-300 via-cyan-300 to-violet-300 text-sm font-black text-slate-950 shadow-lg shadow-cyan-500/20">
            VC
          </div>
          <div className="min-w-0">
            <p className="truncate text-sm font-semibold text-white sm:text-base">VoiceCal Agent</p>
            <p className="text-xs text-slate-400">AI 日程助手</p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <span className="hidden rounded-full border border-emerald-300/20 bg-emerald-300/10 px-3 py-1 text-xs font-medium text-emerald-100 sm:inline-flex">
            本地演示
          </span>
          <button className="rounded-full border border-white/10 bg-white/[0.06] px-4 py-2 text-xs font-semibold text-slate-200 transition hover:border-white/20 hover:bg-white/[0.1] focus:outline-none focus:ring-2 focus:ring-cyan-200 focus:ring-offset-2 focus:ring-offset-[#090d12]">
            新建日程
          </button>
          <button className="hidden rounded-full border border-white/10 bg-white/[0.06] px-4 py-2 text-xs font-semibold text-slate-300 transition hover:border-white/20 hover:bg-white/[0.1] focus:outline-none focus:ring-2 focus:ring-cyan-200 focus:ring-offset-2 focus:ring-offset-[#090d12] sm:inline-flex">
            设置
          </button>
        </div>
      </div>
    </header>
  )
}

export default TopNav
