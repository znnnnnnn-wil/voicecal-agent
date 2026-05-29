function HeroPanel() {
  return (
    <div className="py-8 sm:py-14 lg:py-20">
      <div className="mb-6 inline-flex items-center gap-2 rounded-full border border-cyan-300/20 bg-cyan-300/10 px-3 py-1 text-xs font-medium text-cyan-100 shadow-lg shadow-cyan-950/20">
        <span className="size-2 rounded-full bg-emerald-300 shadow-[0_0_18px_rgba(110,231,183,0.9)]" />
        Calendar tools ready for AI mode
      </div>

      <h1 className="max-w-4xl text-5xl font-semibold leading-[1.02] tracking-normal text-white sm:text-6xl lg:text-7xl">
        Your AI-powered calendar command center
      </h1>

      <p className="mt-6 max-w-2xl text-base leading-8 text-slate-300 sm:text-lg">
        Manage schedules with natural language, preview upcoming events, and prepare the workspace for
        voice-first calendar workflows.
      </p>

      <div className="mt-8 flex flex-col gap-3 sm:flex-row">
        <button className="rounded-full bg-white px-6 py-3 text-sm font-semibold text-slate-950 shadow-xl shadow-cyan-950/30 transition hover:-translate-y-0.5 hover:bg-cyan-100 focus:outline-none focus:ring-2 focus:ring-cyan-200 focus:ring-offset-2 focus:ring-offset-[#090d12]">
          Start demo
        </button>
        <button className="rounded-full border border-white/15 bg-white/[0.06] px-6 py-3 text-sm font-semibold text-white transition hover:-translate-y-0.5 hover:border-white/25 hover:bg-white/[0.1] focus:outline-none focus:ring-2 focus:ring-white/30 focus:ring-offset-2 focus:ring-offset-[#090d12]">
          View calendar
        </button>
      </div>

      <div className="mt-10 grid max-w-2xl grid-cols-3 gap-3">
        <div className="rounded-3xl border border-white/10 bg-white/[0.05] p-4">
          <p className="text-2xl font-semibold text-white">3</p>
          <p className="mt-1 text-xs text-slate-400">today events</p>
        </div>
        <div className="rounded-3xl border border-white/10 bg-white/[0.05] p-4">
          <p className="text-2xl font-semibold text-white">10:00</p>
          <p className="mt-1 text-xs text-slate-400">next free slot</p>
        </div>
        <div className="rounded-3xl border border-white/10 bg-white/[0.05] p-4">
          <p className="text-2xl font-semibold text-white">AI</p>
          <p className="mt-1 text-xs text-slate-400">tool calling</p>
        </div>
      </div>
    </div>
  )
}

export default HeroPanel
