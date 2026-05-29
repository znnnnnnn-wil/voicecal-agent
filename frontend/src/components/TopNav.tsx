function TopNav() {
  return (
    <header className="sticky top-0 z-20 border-b border-white/10 bg-[#070b10]/75 backdrop-blur-2xl">
      <div className="mx-auto flex w-full max-w-7xl flex-col gap-4 px-5 py-4 sm:px-8 md:flex-row md:items-center md:justify-between lg:px-10">
        <div className="flex min-w-0 items-center gap-3">
          <div className="grid size-12 shrink-0 place-items-center rounded-2xl bg-gradient-to-br from-emerald-200 via-cyan-200 to-sky-300 text-sm font-black text-slate-950 shadow-lg shadow-cyan-500/20 ring-1 ring-white/30">
            VC
          </div>
          <div className="min-w-0">
            <p className="truncate text-base font-semibold text-white">VoiceCal Agent</p>
            <p className="text-xs text-slate-400">AI Calendar Assistant</p>
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <span className="rounded-full border border-emerald-300/20 bg-emerald-300/10 px-3 py-1 text-xs font-medium text-emerald-100">
            Demo
          </span>
          <span className="rounded-full border border-cyan-300/20 bg-cyan-300/10 px-3 py-1 text-xs font-medium text-cyan-100">
            Local
          </span>
          <span className="rounded-full border border-violet-300/20 bg-violet-300/10 px-3 py-1 text-xs font-medium text-violet-100">
            Tool Calling Ready
          </span>
          <span className="rounded-full border border-white/10 bg-white/[0.06] px-3 py-1 text-xs font-medium text-slate-300">
            Sync Design Documented
          </span>
        </div>
      </div>
    </header>
  )
}

export default TopNav
