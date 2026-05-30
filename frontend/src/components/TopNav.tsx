const currentMonth = new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric',
  month: 'long',
}).format(new Date())

function TopNav() {
  return (
    <header className="sticky top-0 z-30 border-b border-[#dadce0] bg-white/95 backdrop-blur">
      <div className="flex h-16 w-full items-center justify-between gap-4 px-5 lg:px-7">
        <div className="flex min-w-0 items-center gap-4">
          <button
            aria-label="打开侧边栏"
            className="grid size-9 place-items-center rounded-full text-xl text-slate-600 transition hover:bg-slate-100"
            type="button"
          >
            ≡
          </button>
          <div className="grid size-10 shrink-0 place-items-center rounded-xl bg-[#1a73e8] text-sm font-semibold text-white shadow-sm">
            VC
          </div>
          <div className="min-w-0">
            <p className="truncate text-base font-semibold text-[#202124]">VoiceCal Agent</p>
            <p className="text-xs text-[#5f6368]">AI Calendar Assistant</p>
          </div>
        </div>

        <div className="hidden min-w-0 items-center gap-3 md:flex">
          <button
            className="rounded-lg border border-[#dadce0] bg-white px-4 py-2 text-sm font-medium text-[#3c4043] transition hover:bg-[#f8fafc]"
            type="button"
          >
            今天
          </button>
          <div className="flex items-center gap-1 text-[#5f6368]">
            <span className="grid size-8 place-items-center rounded-full hover:bg-slate-100">‹</span>
            <span className="grid size-8 place-items-center rounded-full hover:bg-slate-100">›</span>
          </div>
          <h1 className="truncate text-xl font-medium text-[#202124]">{currentMonth}</h1>
        </div>

        <div className="flex items-center justify-end gap-2">
          <span className="hidden rounded-full bg-emerald-50 px-3 py-1 text-xs font-medium text-emerald-700 sm:inline-flex">
            Local
          </span>
          <span className="hidden rounded-full bg-blue-50 px-3 py-1 text-xs font-medium text-[#1a73e8] sm:inline-flex">
            Tool Calling Ready
          </span>
          <span className="rounded-full bg-violet-50 px-3 py-1 text-xs font-medium text-violet-700">
            Demo
          </span>
        </div>
      </div>
    </header>
  )
}

export default TopNav
