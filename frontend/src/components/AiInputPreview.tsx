function AiInputPreview() {
  return (
    <section className="rounded-[32px] border border-white/10 bg-white/[0.08] p-5 shadow-2xl shadow-black/30 backdrop-blur-2xl">
      <div className="mb-5 flex items-center justify-between">
        <div>
          <p className="text-sm font-semibold text-white">Ask VoiceCal</p>
          <p className="mt-1 text-xs text-slate-400">Natural language input preview</p>
        </div>
        <span className="rounded-full border border-amber-200/20 bg-amber-200/10 px-3 py-1 text-xs font-medium text-amber-100">
          Voice input coming soon
        </span>
      </div>

      <div className="rounded-3xl border border-white/10 bg-[#0d131a]/80 p-4">
        <div className="flex items-center gap-3">
          <div className="min-w-0 flex-1 rounded-2xl border border-white/10 bg-white/[0.06] px-4 py-3 text-sm text-slate-200">
            Schedule a design review tomorrow at 10 AM
          </div>
          <button
            aria-label="Microphone preview"
            className="grid size-12 place-items-center rounded-2xl border border-cyan-300/20 bg-cyan-300/10 text-sm font-bold text-cyan-100 transition hover:bg-cyan-300/15 focus:outline-none focus:ring-2 focus:ring-cyan-200 focus:ring-offset-2 focus:ring-offset-[#0d131a]"
          >
            Mic
          </button>
        </div>
      </div>

      <div className="mt-4 rounded-3xl border border-emerald-300/15 bg-emerald-300/10 p-4">
        <p className="text-xs font-medium uppercase tracking-[0.24em] text-emerald-200/80">
          AI reply preview
        </p>
        <p className="mt-3 text-sm leading-6 text-emerald-50">
          I found a free slot tomorrow at 10:00 and prepared a calendar event draft.
        </p>
      </div>
    </section>
  )
}

export default AiInputPreview
