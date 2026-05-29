function HeroPanel() {
  return (
    <div className="py-8 sm:py-14 lg:py-20">
      <div className="mb-6 inline-flex items-center gap-2 rounded-full border border-cyan-300/20 bg-cyan-300/10 px-3 py-1 text-xs font-medium text-cyan-100 shadow-lg shadow-cyan-950/20">
        <span className="size-2 rounded-full bg-emerald-300 shadow-[0_0_18px_rgba(110,231,183,0.9)]" />
        日程工具已为 AI 模式就绪
      </div>

      <h1 className="max-w-4xl text-5xl font-semibold leading-[1.02] tracking-normal text-white sm:text-6xl lg:text-7xl">
        你的 AI 日程指挥中心
      </h1>

      <p className="mt-6 max-w-2xl text-base leading-8 text-slate-300 sm:text-lg">
        用自然语言管理日程、预览即将到来的安排，并为后续语音优先的日历工作流做好准备。
      </p>

      <div className="mt-8 flex flex-col gap-3 sm:flex-row">
        <button className="rounded-full bg-white px-6 py-3 text-sm font-semibold text-slate-950 shadow-xl shadow-cyan-950/30 transition hover:-translate-y-0.5 hover:bg-cyan-100 focus:outline-none focus:ring-2 focus:ring-cyan-200 focus:ring-offset-2 focus:ring-offset-[#090d12]">
          开始体验
        </button>
        <button className="rounded-full border border-white/15 bg-white/[0.06] px-6 py-3 text-sm font-semibold text-white transition hover:-translate-y-0.5 hover:border-white/25 hover:bg-white/[0.1] focus:outline-none focus:ring-2 focus:ring-white/30 focus:ring-offset-2 focus:ring-offset-[#090d12]">
          查看日历
        </button>
      </div>

      <div className="mt-10 grid max-w-2xl grid-cols-3 gap-3">
        <div className="rounded-3xl border border-white/10 bg-white/[0.05] p-4">
          <p className="text-2xl font-semibold text-white">3</p>
          <p className="mt-1 text-xs text-slate-400">今日日程</p>
        </div>
        <div className="rounded-3xl border border-white/10 bg-white/[0.05] p-4">
          <p className="text-2xl font-semibold text-white">10:00</p>
          <p className="mt-1 text-xs text-slate-400">下个空档</p>
        </div>
        <div className="rounded-3xl border border-white/10 bg-white/[0.05] p-4">
          <p className="text-2xl font-semibold text-white">AI</p>
          <p className="mt-1 text-xs text-slate-400">工具调用</p>
        </div>
      </div>
    </div>
  )
}

export default HeroPanel
